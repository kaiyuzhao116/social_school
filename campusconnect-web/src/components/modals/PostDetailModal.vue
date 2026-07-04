<template>
  <div class="flex flex-col h-[80vh]">
    <!-- Header -->
    <div
        class="flex justify-between items-center p-4 border-b border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0"
    >
      <div
          class="flex items-center gap-3 cursor-pointer p-1.5 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors"
          @click="handleUserClick(author)"
      >
        <img
            :src="author.avatar"
            class="w-10 h-10 rounded-full object-cover"
        />
        <div>
          <h4 class="font-bold text-sm text-gray-900 dark:text-white flex items-center gap-1">
            {{ author.name }}
            <VerifiedBadge :type="author.verifyType || author.role" />
          </h4>
          <p class="text-xs text-gray-400">
            {{ currentPost.timeAgo || '刚刚' }}
            <span v-if="isEdited" class="ml-1 text-gray-300 dark:text-gray-600">
              (已编辑)
            </span>
          </p>
        </div>
      </div>

      <button
          @click="uiStore.closeModal()"
          class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500"
      >
        <X class="w-5 h-5" />
      </button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto custom-scrollbar p-0 bg-white dark:bg-gray-800">
      <div class="p-6 border-b border-gray-100 dark:border-gray-800">
        <h2
            v-if="currentPost.title"
            class="text-xl font-bold text-gray-900 dark:text-white mb-3"
        >
          {{ currentPost.title }}
        </h2>

        <div
            class="text-gray-700 dark:text-gray-300 leading-relaxed mb-4 prose dark:prose-invert"
            v-html="currentPost.content"
        ></div>

        <!-- Images -->
        <div
            v-if="displayImages.length > 0"
            :class="[
            'grid gap-2 mb-4',
            displayImages.length === 1 ? 'grid-cols-1' : 'grid-cols-2'
          ]"
        >
          <img
              v-for="(img, i) in displayImages"
              :key="i"
              :src="img"
              class="rounded-xl w-full object-cover max-h-96"
          />
        </div>

        <!-- Tags -->
        <div v-if="displayTags.length > 0" class="flex flex-wrap gap-2 mb-4">
          <span
              v-for="tag in displayTags"
              :key="tag"
              @click="handleTagClick(tag)"
              class="text-brand-purple bg-brand-purple/5 px-2.5 py-1 rounded-md text-xs font-bold cursor-pointer hover:bg-brand-purple hover:text-white transition-colors"
          >
            #{{ tag }}
          </span>
        </div>

        <!-- Action Buttons -->
        <div class="flex items-center gap-2 border-t border-gray-50 dark:border-gray-700 pt-4">
          <button
              @click="handleLike"
              :class="[
              'flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-medium text-sm transition-all',
              isLiked
                ? 'bg-pink-50 dark:bg-pink-900/20 text-pink-500'
                : 'bg-gray-50 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600'
            ]"
          >
            <Heart :class="['w-5 h-5', isLiked ? 'fill-current' : '']" />
            <span>{{ localLikes }} 赞</span>
          </button>

          <button
              class="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl font-medium text-sm bg-gray-50 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600 transition-all"
          >
            <MessageSquare class="w-5 h-5" />
            <span>{{ comments.length }} 评论</span>
          </button>

          <div class="relative flex-1">
            <button
                @click="showShareMenu = !showShareMenu"
                class="w-full flex items-center justify-center gap-2 py-2.5 rounded-xl font-medium text-sm bg-gray-50 dark:bg-gray-700 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-600 transition-all"
            >
              <Share2 class="w-5 h-5" />
              <span>{{ localShares }} 分享</span>
            </button>

            <!-- Share Menu -->
            <div
                v-if="showShareMenu"
                class="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 p-2 z-[100]"
            >
              <button
                  @click="shareToWeChat"
                  class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <div class="w-8 h-8 rounded-full bg-green-500 flex items-center justify-center text-white font-bold text-xs">
                  微信
                </div>
                <span class="text-sm text-gray-700 dark:text-gray-300">分享到微信</span>
              </button>

              <button
                  @click="shareToQQ"
                  class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
                <div class="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white font-bold text-xs">
                  QQ
                </div>
                <span class="text-sm text-gray-700 dark:text-gray-300">分享到QQ</span>
              </button>

              <button
                  @click="copyLink"
                  class="w-full flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
              >
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
          <div
              v-for="c in comments"
              :key="c.id"
              class="flex gap-3 animate-in fade-in slide-in-from-bottom-2"
          >
            <img
                :src="c.user?.avatar || defaultAvatar"
                class="w-8 h-8 rounded-full cursor-pointer hover:opacity-80"
                @click="handleUserClick(c.user)"
            />

            <div class="flex-1">
              <div class="bg-gray-50 dark:bg-gray-700/50 p-3 rounded-xl rounded-tl-none">
                <div class="flex justify-between items-baseline mb-1">
                  <span
                      class="font-bold text-xs text-gray-900 dark:text-white cursor-pointer hover:underline flex items-center gap-1"
                      @click="handleUserClick(c.user)"
                  >
                    {{ c.user?.name || '匿名用户' }}
                    <VerifiedBadge :type="c.user?.verifyType || c.user?.role" />
                  </span>

                  <span class="text-[10px] text-gray-400">
                    {{ c.timeAgo || '刚刚' }}
                  </span>
                </div>

                <p class="text-sm text-gray-600 dark:text-gray-300">
                  {{ c.content }}
                </p>
              </div>
            </div>
          </div>

          <div
              v-if="comments.length === 0"
              class="text-center py-8 text-gray-400 text-sm"
          >
            暂无评论，来发表第一条吧！
          </div>
        </div>
      </div>
    </div>

    <!-- Comment Input -->
    <div class="p-4 border-t border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0">
      <div v-if="userStore.isLoggedIn" class="flex items-center gap-3">
        <img
            :src="userStore.currentUser?.avatar || defaultAvatar"
            class="w-8 h-8 rounded-full"
        />

        <input
            v-model="commentText"
            type="text"
            placeholder="写下你的评论..."
            class="flex-1 bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-full px-4 py-2 text-sm focus:ring-2 focus:ring-brand-purple focus:border-transparent outline-none transition-all"
            @keydown.enter="handleSendComment"
        />

        <button
            @click="handleSendComment"
            :disabled="!commentText.trim()"
            class="p-2 bg-brand-purple text-white rounded-full hover:bg-indigo-600 disabled:opacity-50 transition-colors"
        >
          <Send class="w-4 h-4" />
        </button>
      </div>

      <div v-else class="text-center py-2">
        <button
            @click="handleLoginToComment"
            class="text-brand-purple hover:underline text-sm font-medium"
        >
          登录后参与评论
        </button>
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

const defaultAvatar = 'https://via.placeholder.com/40'

const currentPost = computed(() => uiStore.modalData?.data || {})

const author = computed(() => {
  return currentPost.value.user || {
    id: null,
    name: '匿名用户',
    avatar: defaultAvatar,
    role: 'student',
    verifyType: null
  }
})

const comments = ref([])
const commentText = ref('')
const isLiked = ref(false)
const localLikes = ref(0)
const localShares = ref(0)
const showShareMenu = ref(false)

const displayTags = computed(() => {
  const tags = currentPost.value.tags

  if (!tags) return []

  if (Array.isArray(tags)) {
    return tags
  }

  if (typeof tags === 'string') {
    try {
      const parsed = JSON.parse(tags)
      return Array.isArray(parsed) ? parsed : [tags]
    } catch (e) {
      return tags
          .replace('[', '')
          .replace(']', '')
          .replaceAll('"', '')
          .split(',')
          .map(item => item.trim())
          .filter(Boolean)
    }
  }

  return []
})

const displayImages = computed(() => {
  const images = currentPost.value.images

  if (!images) return []

  if (Array.isArray(images)) {
    return images
  }

  if (typeof images === 'string') {
    try {
      const parsed = JSON.parse(images)
      return Array.isArray(parsed) ? parsed : [images]
    } catch (e) {
      return images
          .split(',')
          .map(item => item.trim())
          .filter(Boolean)
    }
  }

  return []
})

const isEdited = computed(() => {
  return currentPost.value.updatedTimestamp &&
      currentPost.value.timestamp &&
      currentPost.value.updatedTimestamp > currentPost.value.timestamp + 60000
})

function getPostId() {
  const rawId = currentPost.value.id

  if (!rawId) {
    return null
  }

  if (typeof rawId === 'object') {
    return rawId.id || rawId.postId || null
  }

  return rawId
}

function initLocalState() {
  comments.value = currentPost.value.commentsList || []
  isLiked.value = Boolean(currentPost.value.isLiked)
  localLikes.value = Number(currentPost.value.likes || currentPost.value.likeCount || 0)
  localShares.value = Number(currentPost.value.shares || currentPost.value.shareCount || 0)
}

onMounted(async () => {
  initLocalState()

  const postId = getPostId()

  if (!postId) {
    console.warn('帖子详情缺少 postId：', currentPost.value)
    return
  }

  const res = await postStore.fetchComments(postId)

  if (res?.success) {
    comments.value = res.data || []
  }
})

function handleUserClick(user) {
  if (!user) return

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

async function handleSendComment() {
  const content = commentText.value.trim()

  if (!content) return

  const postId = getPostId()

  if (!postId) {
    console.error('评论失败：postId 为空', currentPost.value)
    return
  }

  const newComment = {
    id: Date.now().toString(),
    user: userStore.currentUser || {
      id: 'guest',
      name: '访客',
      avatar: defaultAvatar,
      role: 'student',
      isVerified: false
    },
    content,
    timeAgo: '刚刚'
  }

  comments.value.unshift(newComment)
  commentText.value = ''

  /**
   * 注意：
   * 这里不要再调用 postStore.updatePost(updatedPost)
   * 原来的 updatePost 会触发 PUT /posts/[object Object]
   * 所以这里仅本地更新评论数量。
   */
  currentPost.value.comments = comments.value.length
  currentPost.value.commentCount = comments.value.length

  /**
   * 如果你的 store 后面写了真正的评论接口，可以自动调用。
   * 没有这个方法也不会报错。
   */
  try {
    if (typeof postStore.addComment === 'function') {
      const res = await postStore.addComment(postId, content)

      if (res?.success && res.data) {
        comments.value[0] = res.data
      }
    } else if (typeof postStore.createComment === 'function') {
      const res = await postStore.createComment(postId, content)

      if (res?.success && res.data) {
        comments.value[0] = res.data
      }
    }
  } catch (e) {
    console.error('评论提交失败，只保留本地评论：', e)
  }
}

async function handleLike() {
  const postId = getPostId()

  if (!postId) {
    console.error('点赞失败：postId 为空', currentPost.value)
    return
  }

  const oldLiked = isLiked.value
  const oldLikes = localLikes.value

  isLiked.value = !isLiked.value
  localLikes.value += isLiked.value ? 1 : -1

  try {
    const res = await postStore.toggleLike(postId)

    if (res && res.success === false) {
      isLiked.value = oldLiked
      localLikes.value = oldLikes
    }
  } catch (e) {
    isLiked.value = oldLiked
    localLikes.value = oldLikes
    console.error('点赞失败：', e)
  }
}

function getShareUrl() {
  const postId = getPostId()
  return `${window.location.origin}/?post=${postId || ''}`
}

function shareToWeChat() {
  const shareUrl = getShareUrl()
  navigator.clipboard.writeText(shareUrl)
  alert('链接已复制！请打开微信粘贴分享给好友')
  incrementShare()
}

function shareToQQ() {
  const shareUrl = getShareUrl()
  const title = currentPost.value.title || '分享一篇精彩帖子'
  const summary = currentPost.value.content
      ?.replace(/<[^>]+>/g, '')
      .substring(0, 100) || ''

  window.open(
      `https://connect.qq.com/widget/shareqq/index.html?url=${encodeURIComponent(shareUrl)}&title=${encodeURIComponent(title)}&summary=${encodeURIComponent(summary)}`,
      '_blank',
      'width=600,height=400'
  )

  incrementShare()
}

function copyLink() {
  const shareUrl = getShareUrl()
  navigator.clipboard.writeText(shareUrl)
  alert('链接已复制到剪贴板')
  incrementShare()
}

async function incrementShare() {
  const postId = getPostId()

  localShares.value++
  showShareMenu.value = false

  if (!postId) {
    return
  }

  try {
    await postStore.sharePost(postId)
  } catch (e) {
    console.error('分享计数失败：', e)
  }
}
</script>