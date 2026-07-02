<template>
  <div class="flex-1 min-w-0">
    <!-- Tabs -->
    <div
      class="bg-white dark:bg-gray-800 rounded-2xl p-1.5 mb-6 flex space-x-1 shadow-sm border border-gray-100 dark:border-gray-700 transition-colors overflow-x-auto no-scrollbar"
    >
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="activeTab = tab.id"
        :class="[
          'flex-1 py-2.5 px-3 md:px-4 text-xs md:text-sm font-bold rounded-xl transition-all duration-200 whitespace-nowrap',
          activeTab === tab.id
            ? 'bg-brand-purple text-white shadow-md transform scale-[1.02]'
            : 'text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-700/50'
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- Search Result Info -->
    <div
      v-if="postStore.searchQuery"
      class="mb-6 flex items-center justify-between bg-blue-50 dark:bg-blue-900/20 p-3 rounded-xl text-blue-700 dark:text-blue-300 border border-blue-100 dark:border-blue-800/30"
    >
      <span class="text-sm">
        搜索: <strong>"{{ postStore.searchQuery }}"</strong> ({{
          filteredPosts.length
        }})
      </span>
      <button
        @click="postStore.setSearchQuery('')"
        class="p-1 hover:bg-blue-100 dark:hover:bg-blue-900/40 rounded-full transition-colors"
      >
        <XIcon class="w-4 h-4" />
      </button>
    </div>

    <!-- Posts Masonry/Waterfall -->
    <div class="columns-1 xl:columns-2 gap-4 pb-8 space-y-4">
      <template v-if="displayPosts.length > 0">
        <div
          v-for="(post, index) in displayPosts"
          :key="post.id"
          @click="handlePostClick(post)"
          :class="[
            'flex flex-col bg-white dark:bg-gray-800 rounded-2xl p-5 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-xl hover:-translate-y-1 transition-all duration-300 ease-out cursor-pointer group relative break-inside-avoid',
            activeMenuPostId === post.id ? 'z-30' : 'z-0',
            !hasImages(post)
              ? 'bg-gradient-to-br from-white to-gray-50 dark:from-gray-800 dark:to-gray-800/90'
              : ''
          ]"
          :style="{
            animation: `fadeInUp 0.5s ease-out ${index * 0.05}s backwards`
          }"
        >
          <!-- Header -->
          <div class="flex items-start justify-between mb-3 shrink-0">
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <img
                :src="getAvatarUrl(post.user.avatar, post.user.name)"
                @error="handleAvatarError"
                :alt="post.user.name"
                class="w-10 h-10 rounded-full object-cover ring-2 ring-transparent group-hover:ring-brand-purple/20 transition-all flex-shrink-0"
                @click.stop="handleUserClick(post.user)"
              />
              <div class="min-w-0">
                <div class="flex items-center gap-1.5">
                  <h3
                    class="font-bold text-gray-900 dark:text-white text-sm hover:text-brand-purple transition-colors truncate"
                    @click.stop="handleUserClick(post.user)"
                  >
                    {{ post.user.name }}
                  </h3>
                  <VerifiedBadge 
                    :type="post.user.verifyType || post.user.role" 
                  />
                </div>
                <p class="text-xs text-gray-400 font-medium mt-0.5">
                  {{ post.timeAgo }}
                </p>
              </div>
            </div>

            <div class="relative flex-shrink-0 ml-2">
              <button
                @click.stop="toggleMenu(post.id)"
                class="p-1.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full transition-colors"
              >
                <MoreHorizontal class="w-5 h-5" />
              </button>

              <div
                v-if="activeMenuPostId === post.id"
                class="absolute right-0 top-8 w-32 bg-white dark:bg-gray-700 rounded-xl shadow-xl border border-gray-100 dark:border-gray-600 py-1 z-20 animate-in fade-in zoom-in-95 duration-200"
              >
                <template v-if="isOwner(post)">
                  <button
                    v-if="canEdit(post)"
                    @click.stop="handleEditPost(post)"
                    class="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-600 flex items-center gap-2"
                  >
                    <Edit2 class="w-4 h-4" /> 编辑
                  </button>
                  <button
                    @click.stop="handleDeletePost(post.id)"
                    class="w-full px-4 py-2 text-left text-sm text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 flex items-center gap-2"
                  >
                    <Trash2 class="w-4 h-4" /> 删除
                  </button>
                </template>
                <template v-else>
                  <button
                    @click.stop="handleReport(post.id)"
                    class="w-full px-4 py-2 text-left text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-gray-600"
                  >
                    举报
                  </button>
                </template>
              </div>
            </div>
          </div>

          <!-- Content Body -->
          <div class="flex-1 min-w-0">
            <h2
              v-if="post.title"
              class="text-base font-bold text-gray-900 dark:text-white mb-1.5 group-hover:text-brand-purple transition-colors line-clamp-1"
            >
              {{ post.title }}
            </h2>

            <div
              :class="[
                'text-gray-600 dark:text-gray-300 text-sm leading-relaxed mb-3 prose dark:prose-invert max-w-none',
                hasImages(post) ? 'line-clamp-3' : 'line-clamp-6'
              ]"
              v-html="post.content"
            ></div>

            <!-- Image Grid -->
            <div
              v-if="hasImages(post)"
              class="mt-3 h-52 w-full rounded-xl overflow-hidden bg-gray-100 dark:bg-gray-700/50 relative group/image"
            >
              <!-- Single Image -->
              <template v-if="getImages(post).length === 1">
                <img
                  :src="getImages(post)[0]"
                  class="w-full h-full object-cover transition-transform duration-500 hover:scale-105 cursor-pointer"
                  @click.stop="openLightbox(getImages(post)[0])"
                />
              </template>

              <!-- Two Images -->
              <template v-else-if="getImages(post).length === 2">
                <div class="grid grid-cols-2 h-full gap-0.5">
                  <img
                    v-for="(img, i) in getImages(post)"
                    :key="i"
                    :src="img"
                    class="w-full h-full object-cover cursor-pointer hover:opacity-90 transition-opacity"
                    @click.stop="openLightbox(img)"
                  />
                </div>
              </template>

              <!-- Three Images -->
              <template v-else-if="getImages(post).length === 3">
                <div class="grid grid-cols-2 h-full gap-0.5">
                  <img
                    :src="getImages(post)[0]"
                    class="w-full h-full object-cover cursor-pointer hover:opacity-90 transition-opacity"
                    @click.stop="openLightbox(getImages(post)[0])"
                  />
                  <div class="grid grid-rows-2 h-full gap-0.5">
                    <img
                      :src="getImages(post)[1]"
                      class="w-full h-full object-cover cursor-pointer hover:opacity-90 transition-opacity"
                      @click.stop="openLightbox(getImages(post)[1])"
                    />
                    <img
                      :src="getImages(post)[2]"
                      class="w-full h-full object-cover cursor-pointer hover:opacity-90 transition-opacity"
                      @click.stop="openLightbox(getImages(post)[2])"
                    />
                  </div>
                </div>
              </template>

              <!-- Four+ Images -->
              <template v-else>
                <div class="grid grid-cols-2 grid-rows-2 h-full gap-0.5">
                  <div
                    v-for="(img, i) in getImages(post).slice(0, 4)"
                    :key="i"
                    class="relative w-full h-full"
                  >
                    <img
                      :src="img"
                      class="w-full h-full object-cover cursor-pointer hover:opacity-90 transition-opacity"
                      @click.stop="openLightbox(img)"
                    />
                    <div
                      v-if="i === 3 && getImages(post).length > 4"
                      class="absolute inset-0 bg-black/50 flex items-center justify-center cursor-pointer hover:bg-black/60 transition-colors"
                      @click.stop="openLightbox(img)"
                    >
                      <span class="text-white font-bold text-lg"
                        >+{{ getImages(post).length - 4 }}</span
                      >
                    </div>
                  </div>
                </div>
              </template>
            </div>

            <!-- Tags -->
            <div class="flex flex-wrap gap-1.5 mt-3">
              <span
                v-for="tag in post.tags.slice(0, 3)"
                :key="tag"
                @click.stop="handleTagClick(tag)"
                class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-medium bg-gray-100 dark:bg-gray-700/50 text-gray-500 dark:text-gray-400 hover:bg-brand-purple hover:text-white transition-all cursor-pointer"
              >
                #{{ tag }}
              </span>
              <span
                v-if="post.tags.length > 3"
                class="inline-flex items-center px-2 py-0.5 rounded-md text-[10px] font-medium text-gray-400 bg-gray-50 dark:bg-gray-800"
                >+{{ post.tags.length - 3 }}</span
              >
            </div>
          </div>

          <!-- Footer Actions -->
          <div
            class="flex items-center justify-between pt-4 mt-2 border-t border-gray-50 dark:border-gray-700/50 shrink-0"
          >
            <button
              @click.stop="handleLike(post.id)"
              :class="[
                'flex items-center gap-1.5 text-xs md:text-sm font-medium px-2 py-1.5 rounded-lg transition-all',
                post.isLiked
                  ? 'text-brand-purple bg-brand-purple/5'
                  : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700'
              ]"
            >
              <ThumbsUp
                :class="[
                  'w-4 h-4',
                  post.isLiked ? 'fill-current animate-like-bounce' : ''
                ]"
              />
              <span>{{ post.likes }}</span>
            </button>

            <button
              class="flex items-center gap-1.5 text-xs md:text-sm font-medium text-gray-500 dark:text-gray-400 px-2 py-1.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              <MessageSquare class="w-4 h-4" />
              <span>{{ post.comments }}</span>
            </button>

            <div class="relative">
              <button
                class="flex items-center gap-1.5 text-xs md:text-sm font-medium text-gray-500 dark:text-gray-400 px-2 py-1.5 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
                @click.stop="toggleShareMenu(post.id)"
              >
                <Share2 class="w-4 h-4" />
                <span>{{ post.shares }}</span>
              </button>
              <!-- Share Menu -->
              <div v-if="activeShareMenuPostId === post.id"
                class="absolute bottom-full right-0 mb-2 w-40 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 p-2 z-[100] animate-in fade-in zoom-in-95 duration-200">
                <button @click.stop="shareToWeChat(post)"
                  class="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                  <div
                    class="w-7 h-7 rounded-full bg-green-500 flex items-center justify-center text-white font-bold text-[10px]">
                    微信</div>
                  <span class="text-sm text-gray-700 dark:text-gray-300">分享到微信</span>
                </button>
                <button @click.stop="shareToQQ(post)"
                  class="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                  <div
                    class="w-7 h-7 rounded-full bg-blue-500 flex items-center justify-center text-white font-bold text-[10px]">
                    QQ</div>
                  <span class="text-sm text-gray-700 dark:text-gray-300">分享到QQ</span>
                </button>
                <button @click.stop="copyLink(post)"
                  class="w-full flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                  <div class="w-7 h-7 rounded-full bg-gray-200 dark:bg-gray-600 flex items-center justify-center">
                    <Link2Icon class="w-3.5 h-3.5 text-gray-600 dark:text-gray-300" />
                  </div>
                  <span class="text-sm text-gray-700 dark:text-gray-300">复制链接</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- Empty State -->
      <div
        v-else
        class="col-span-full text-center py-12 md:py-16 bg-white dark:bg-gray-800 rounded-3xl shadow-sm border border-gray-100 dark:border-gray-700 break-inside-avoid"
      >
        <div
          class="bg-gray-50 dark:bg-gray-700/50 w-16 h-16 md:w-20 md:h-20 rounded-full flex items-center justify-center mx-auto mb-4"
        >
          <Search
            class="w-6 h-6 md:w-8 md:h-8 text-gray-300 dark:text-gray-500"
          />
        </div>
        <p class="text-gray-500 dark:text-gray-400 font-medium text-sm">
          没有找到相关内容
        </p>
        <button
          v-if="postStore.searchQuery"
          @click="postStore.setSearchQuery('')"
          class="mt-4 text-brand-purple font-medium hover:underline text-sm"
        >
          清除搜索
        </button>
      </div>
    </div>

    <!-- Load More -->
    <div v-if="hasMore" class="text-center pb-8 pt-2">
      <button
        @click="handleLoadMore"
        :disabled="loadingMore"
        class="group px-8 py-3 bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 text-gray-600 dark:text-gray-300 font-medium text-sm rounded-full shadow-sm hover:shadow-md hover:border-brand-purple hover:text-brand-purple dark:hover:text-brand-purple transition-all disabled:opacity-70 flex items-center gap-2 mx-auto"
      >
        <template v-if="loadingMore">
          <div
            class="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"
          ></div>
          正在加载...
        </template>
        <template v-else>
          加载更多内容
          <ChevronDown
            class="w-4 h-4 group-hover:translate-y-0.5 transition-transform"
          />
        </template>
      </button>
    </div>

    <!-- Lightbox Modal -->
    <div
      v-if="lightboxImage"
      class="fixed inset-0 z-[110] bg-black/95 flex items-center justify-center p-4 backdrop-blur-sm animate-in fade-in duration-200"
      @click="lightboxImage = null"
    >
      <button
        class="absolute top-6 right-6 text-white/70 hover:text-white p-2 bg-white/10 rounded-full"
      >
        <XIcon class="w-8 h-8" />
      </button>
      <img
        :src="lightboxImage"
        class="max-w-full max-h-[90vh] object-contain rounded-lg shadow-2xl"
        @click.stop
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { usePostStore, useUserStore, useUIStore } from '../stores'
import {
  ThumbsUp,
  MessageSquare,
  Share2,
  MoreHorizontal,
  X as XIcon,
  Search,
  ChevronDown,
  Trash2,
  Edit2,
  Briefcase,
  Building2,
  BadgeCheck,
  Link2 as Link2Icon
} from 'lucide-vue-next'

const postStore = usePostStore()
const userStore = useUserStore()
const uiStore = useUIStore()

const activeTab = ref('latest')
const visibleCount = ref(6)
const loadingMore = ref(false)
const lightboxImage = ref(null)
const activeMenuPostId = ref(null)
const activeShareMenuPostId = ref(null)
const recommendedPosts = ref([]) // 个性化推荐帖子
const loadingRecommended = ref(false)

const tabs = [
  { id: 'latest', label: '最新' },
  { id: 'recommended', label: '推荐' },
  { id: 'study', label: '学习' },
  { id: 'life', label: '生活' }
]

const filteredPosts = computed(() => {
  let filtered = [...postStore.posts]

  // Search Filter
  if (postStore.searchQuery) {
    const query = postStore.searchQuery.toLowerCase()
    filtered = filtered.filter(
      (post) =>
        post.content.toLowerCase().includes(query) ||
        post.title?.toLowerCase().includes(query) ||
        post.tags.some((tag) => tag.toLowerCase().includes(query))
    )
  }

  // Tab Filter
  switch (activeTab.value) {
    case 'recommended':
      // 返回个性化推荐帖子
      return recommendedPosts.value
    case 'study': {
      // 学习相关关键词（模糊匹配）
      const studyKeywords = ['学习', '编程', '数学', '计算机', '资料', '考试', '课程', '作业', '图书馆', '自习', '复习', '笔记', '论文']
      return filtered.filter((post) =>
        post.tags.some((tag) => studyKeywords.some(kw => tag.includes(kw))) ||
        studyKeywords.some(kw => post.content.includes(kw))
      )
    }
    case 'life': {
      // 生活相关关键词（模糊匹配）
      const lifeKeywords = ['生活', '运动', '美食', '活动', '摄影', '艺术', '二手', '失物招领', '食堂', '宿舍', '校园', '樱花', '篮球', '足球', '聚餐', '旅游']
      return filtered.filter((post) =>
        post.tags.some((tag) => lifeKeywords.some(kw => tag.includes(kw))) ||
        lifeKeywords.some(kw => post.content.includes(kw))
      )
    }
    default:
      // 最新：按时间降序排列
      return filtered.sort((a, b) => {
        const timeA = a.timestamp || new Date(a.createdAt).getTime() || 0
        const timeB = b.timestamp || new Date(b.createdAt).getTime() || 0
        return timeB - timeA
      })
  }
})

const displayPosts = computed(() => filteredPosts.value.slice(0, visibleCount.value))
const hasMore = computed(() => visibleCount.value < filteredPosts.value.length)

// Reset pagination when filter changes
watch([activeTab, () => postStore.searchQuery], async () => {
  visibleCount.value = 6
  window.scrollTo({ top: 0, behavior: 'smooth' })
  
  // 当切换到推荐标签时，获取个性化推荐
  if (activeTab.value === 'recommended' && recommendedPosts.value.length === 0) {
    loadingRecommended.value = true
    try {
      const res = await postStore.fetchRecommendedPosts(20)
      if (res.success) {
        recommendedPosts.value = res.data
      }
    } finally {
      loadingRecommended.value = false
    }
  }
})

// Close menu when clicking outside
const handleClickOutside = () => {
  activeMenuPostId.value = null
  activeShareMenuPostId.value = null
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})

function handleLoadMore() {
  loadingMore.value = true
  setTimeout(() => {
    visibleCount.value += 6
    loadingMore.value = false
  }, 600)
}

function handleLike(postId) {
  postStore.toggleLike(postId)
}

// 分享菜单
function toggleShareMenu(postId) {
  activeShareMenuPostId.value = activeShareMenuPostId.value === postId ? null : postId
}

function getShareUrl(post) {
  return `${window.location.origin}?post=${post.id}`
}

function incrementShare(post) {
  postStore.sharePost(post.id)
  activeShareMenuPostId.value = null
}

function shareToWeChat(post) {
  const shareUrl = getShareUrl(post)
  navigator.clipboard.writeText(shareUrl)
  alert('链接已复制！请打开微信粘贴分享给好友')
  incrementShare(post)
}

function shareToQQ(post) {
  const shareUrl = getShareUrl(post)
  const title = post.title || '分享一篇精彩帖子'
  const summary = post.content?.replace(/<[^>]+>/g, '').substring(0, 100) || ''
  window.open(`https://connect.qq.com/widget/shareqq/index.html?url=${encodeURIComponent(shareUrl)}&title=${encodeURIComponent(title)}&summary=${encodeURIComponent(summary)}`, '_blank', 'width=600,height=400')
  incrementShare(post)
}

function copyLink(post) {
  const shareUrl = getShareUrl(post)
  navigator.clipboard.writeText(shareUrl)
  alert('链接已复制到剪贴板')
  incrementShare(post)
}

function handleReport(postId) {
  activeMenuPostId.value = null
  console.log(`[Report API] Post ${postId} has been reported.`)
  alert('举报已提交，我们会尽快审核处理。')
}

function handleDeletePost(postId) {
  activeMenuPostId.value = null
  if (window.confirm('确定要删除这条动态吗？')) {
    postStore.deletePost(postId)
  }
}

function handleEditPost(post) {
  activeMenuPostId.value = null
  uiStore.openModal('CREATE_POST', post)
}

function handlePostClick(post) {
  uiStore.openModal('POST_DETAIL', post)
}

function handleUserClick(user) {
  uiStore.openModal('PROFILE', user)
}

function handleTagClick(tag) {
  postStore.setSearchQuery(tag)
}

function openLightbox(src) {
  lightboxImage.value = src
}

function toggleMenu(postId) {
  activeMenuPostId.value = activeMenuPostId.value === postId ? null : postId
}

function isOwner(post) {
  return userStore.currentUser?.id === post.user.id
}

function canEdit(post) {
  return (
    isOwner(post) &&
    post.timestamp &&
    Date.now() - post.timestamp < 12 * 60 * 60 * 1000 // 12 hours check
  )
}

function hasImages(post) {
  return (post.images && post.images.length > 0) || post.image
}

function getImages(post) {
  return post.images || (post.image ? [post.image] : [])
}

// 使用公共头像工具函数
import { getAvatarUrl, handleAvatarError } from '../utils/avatar'
import VerifiedBadge from './common/VerifiedBadge.vue'
</script>

<style>
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
@keyframes likeBounce {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.3);
  }
  100% {
    transform: scale(1);
  }
}
.animate-like-bounce {
  animation: likeBounce 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
</style>
