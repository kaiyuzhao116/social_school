<template>
  <div class="flex flex-col h-[80vh]">
    <!-- Header -->
    <div
      class="flex justify-between items-center p-4 border-b border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0">
      <div
        class="flex items-center gap-3 cursor-pointer p-1.5 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors"
        @click="handleUserClick(post.user)">
        <img :src="post.user.avatar" class="w-10 h-10 rounded-full object-cover" />
        <div>
          <h4 class="font-bold text-sm text-gray-900 dark:text-white flex items-center gap-1">
            {{ post.user.name }}
            <VerifiedBadge :type="post.user.verifyType || post.user.role" />
          </h4>
          <p class="text-xs text-gray-400">
            {{ post.timeAgo }}
            <span v-if="isEdited" class="ml-1 text-gray-300 dark:text-gray-600">(已编辑)</span>
          </p>
        </div>
      </div>
      <button @click="uiStore.closeModal()"
        class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500">
        <X class="w-5 h-5" />
      </button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto custom-scrollbar p-0 bg-white dark:bg-gray-800">
      <div class="p-6 border-b border-gray-100 dark:border-gray-800">
        <h2 v-if="post.title" class="text-xl font-bold text-gray-900 dark:text-white mb-3">
          {{ post.title }}
        </h2>
        <div class="text-gray-700 dark:text-gray-300 leading-relaxed mb-4 prose dark:prose-invert"
          v-html="post.content"></div>

        <!-- Images -->
        <div v-if="post.images && post.images.length > 0" :class="[
          'grid gap-2 mb-4',
          post.images.length === 1 ? 'grid-cols-1' : 'grid-cols-2'
        ]">
          <img v-for="(img, i) in post.images" :key="i" :src="img" class="rounded-xl w-full object-cover max-h-96" />
        </div>

        <!-- Tags -->
        <div class="flex flex-wrap gap-2 mb-4">
          <span v-for="tag in post.tags" :key="tag" @click="handleTagClick(tag)"
            class="text-brand-purple bg-brand-purple/5 px-2.5 py-1 rounded-md text-xs font-bold cursor-pointer hover:bg-brand-purple hover:text-white transition-colors">
            #{{ tag }}
          </span>
        </div>

        <!-- Action Buttons -->
        <div class="flex items-center gap-2 border-t border-gray-50 dark:border-gray-700 pt-4">
          <button @click="handleLike"
            :class="['flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-medium text-sm transition-all', isLiked ? 'bg-pink-50 dark:bg-pink-900/20 text-pink-500' : 'bg-gray-50 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600']">
            <Heart :class="['w-5 h-5', isLiked ? 'fill-current' : '']" />
            <span>{{ localLikes }} 赞</span>
          </button>
          <button
            class="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-medium text-sm bg-gray-50 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600 transition-all">
            <MessageSquare class="w-5 h-5" />
            <span>{{ comments.length }} 评论</span>
          </button>
          <div class="relative flex-1">
            <button @click="showShareMenu = !showShareMenu"
              class="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl font-medium text-sm bg-gray-50 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600 transition-all">
              <Share2 class="w-5 h-5" />
              <span>{{ localShares }} 分享</span>
            </button>
            <!-- Share Menu -->
            <div v-if="showShareMenu"
              class="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 p-2 z-[100]">
              <button @click="shareToWeChat"
                class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                <div
                  class="w-8 h-8 rounded-full bg-green-500 flex items-center justify-center text-white font-bold text-xs">
                  微信</div>
                <span class="text-sm text-gray-700 dark:text-gray-300">分享到微信</span>
              </button>
              <button @click="shareToQQ"
                class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                <div
                  class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white font-bold text-xs">
                  QQ</div>
                <span class="text-sm text-gray-700 dark:text-gray-300">分享到QQ</span>
              </button>
              <button @click="copyLink"
                class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                <div class="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                  <Link2 class="w-4 h-4 text-gray-600 dark:text-gray-300" />
                </div>
                <span class="text-sm text-gray-700 dark:text-gray-300">复制链接</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Comments Section -->
      <div class="p-6">
        <h3 class="font-bold text-gray-900 dark:text-white mb-4">
          评论 ({{ comments.length }})
        </h3>
        <div class="space-y-4">
          <div v-for="c in comments" :key="c.id" class="flex gap-3 animate-in fade-in slide-in-from-bottom-2">
            <img :src="c.user.avatar" class="w-8 h-8 rounded-full cursor-pointer hover:opacity-80"
              @click="handleUserClick(c.user)" />
            <div class="flex-1">
              <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-xl rounded-tl-none">
                <div class="flex justify-between items-baseline mb-1">
                  <span class="font-bold text-xs text-gray-900 dark:text-white cursor-pointer hover:underline flex items-center gap-1"
                    @click="handleUserClick(c.user)">
                    {{ c.user.name }}
                    <VerifiedBadge :type="c.user.verifyType || c.user.role" />
                  </span>
                  <span class="text-[10px] text-gray-400">{{ c.timeAgo }}</span>
                </div>
                <p class="text-sm text-gray-600 dark:text-gray-300">
                  {{ c.content }}
                </p>
              </div>
            </div>
          </div>
          <div v-if="comments.length === 0" class="text-center py-8 text-gray-400 text-sm">
            暂无评论，来发表第一条吧！
          </div>
        </div>
      </div>
    </div>

    <!-- Comment Input -->
    <div class="p-4 border-t border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0">
      <div v-if="userStore.isLoggedIn" class="flex items-center gap-3">
        <img :src="userStore.currentUser?.avatar || 'https://via.placeholder.com/40'" class="w-8 h-8 rounded-full" />
        <input v-model="commentText" type="text" placeholder="写下你的评论..."
          class="flex-1 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-full px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple focus:border-transparent outline-none transition-all"
          @keydown.enter="handleSendComment" />
        <button @click="handleSendComment" :disabled="!commentText.trim()"
          class="p-2 bg-brand-purple text-white rounded-full hover:bg-indigo-600 disabled:opacity-50 transition-colors">
          <Send class="w-4 h-4" />
        </button>
      </div>
      <div v-else class="text-center py-2">
        <button @click="handleLoginToComment"
          class="text-brand-purple hover:underline text-sm font-medium">登录后参与评论</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore, useUIStore, usePostStore } from '../../stores'
import { X, Heart, MessageSquare, Share2, Send, Link2 } from 'lucide-vue-next'
import VerifiedBadge from '../common/VerifiedBadge.vue'

const userStore = useUserStore()
const uiStore = useUIStore()
const postStore = usePostStore()

const post = computed(() => uiStore.modalData.data || {})
const comments = ref(post.value.commentsList || [])
const commentText = ref('')
const isLiked = ref(post.value.isLiked || false)
const localLikes = ref(post.value.likes || 0)
const localShares = ref(post.value.shares || 0)
const showShareMenu = ref(false)

const isEdited = computed(() => {
  return post.value.updatedTimestamp && post.value.timestamp &&
    post.value.updatedTimestamp > post.value.timestamp + 60000 // 允许1分钟误差
})

onMounted(async () => {
  if (post.value.id) {
    const res = await postStore.fetchComments(post.value.id)
    if (res.success) {
      comments.value = res.data
    }
  }
})

function handleUserClick(user) {
  uiStore.closeModal()
  uiStore.openModal('PROFILE', user)
}

function handleTagClick(tag) {
  uiStore.closeModal()
  postStore.setSearchQuery(tag)
}

function handleLoginToComment() {
  uiStore.closeModal()
  uiStore.openModal('LOGIN')
}

function handleSendComment() {
  if (!commentText.value.trim()) return

  const newComment = {
    id: Date.now().toString(),
    user: userStore.currentUser || {
      id: 'guest',
      name: '访客',
      avatar: 'https://via.placeholder.com/40',
      role: 'student',
      isVerified: false
    },
    content: commentText.value,
    timeAgo: '刚刚'
  }

  comments.value.unshift(newComment)

  // Update the post in store
  const updatedPost = {
    ...post.value,
    comments: post.value.comments + 1,
    commentsList: comments.value
  }
  postStore.updatePost(updatedPost)

  commentText.value = ''
}

function handleLike() {
  isLiked.value = !isLiked.value
  localLikes.value += isLiked.value ? 1 : -1
  postStore.toggleLike(post.value.id)
}

function getShareUrl() {
  return `${window.location.origin}/?post=${post.value.id}`
}

function shareToWeChat() {
  // 微信没有网页分享API，复制链接并提示用户
  const shareUrl = getShareUrl()
  navigator.clipboard.writeText(shareUrl)
  alert('链接已复制！请打开微信粘贴分享给好友')
  incrementShare()
}

function shareToQQ() {
  const shareUrl = getShareUrl()
  const title = post.value.title || '分享一篇精彩帖子'
  const summary = post.value.content?.replace(/<[^>]+>/g, '').substring(0, 100) || ''
  window.open(`https://connect.qq.com/widget/shareqq/index.html?url=${encodeURIComponent(shareUrl)}&title=${encodeURIComponent(title)}&summary=${encodeURIComponent(summary)}`, '_blank', 'width=600,height=400')
  incrementShare()
}

function copyLink() {
  const shareUrl = getShareUrl()
  navigator.clipboard.writeText(shareUrl)
  alert('链接已复制到剪贴板')
  incrementShare()
}

function incrementShare() {
  localShares.value++
  postStore.sharePost(post.value.id)
  showShareMenu.value = false
}
</script>
