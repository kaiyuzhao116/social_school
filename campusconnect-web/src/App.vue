<template>
  <div
    class="min-h-screen bg-gray-50 dark:bg-gray-950 text-gray-900 dark:text-gray-100 font-sans transition-colors duration-300 flex flex-col min-w-[1024px]">
    <Navbar v-if="!route.meta.hideNavbar" />
    <main :class="route.meta.hideNavbar ? '' : 'flex-1 max-w-7xl w-full mx-auto px-6 lg:px-8 py-6'">
      <router-view />
    </main>
    <Footer v-if="!route.meta.hideFooter" />
    <ModalManager v-if="!route.meta.hideNavbar" />
    <GlobalComponents v-if="!route.meta.hideNavbar" />
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './components/Navbar.vue'
import Footer from './components/Footer.vue'
import ModalManager from './components/ModalManager.vue'
import GlobalComponents from './components/GlobalComponents.vue'
import { useUIStore, useUserStore, usePostStore, useMessageStore } from './stores'

const route = useRoute()
const uiStore = useUIStore()
const userStore = useUserStore()
const postStore = usePostStore()
const messageStore = useMessageStore()

// 处理分享链接中的帖子ID
function handleSharedPost() {
  const postId = route.query.post
  if (postId) {
    // 等待帖子数据加载完成后打开详情
    const post = postStore.posts.find(p => p.id === postId)
    if (post) {
      uiStore.openModal('POST_DETAIL', post)
      // 清除URL中的query参数
      window.history.replaceState({}, '', window.location.pathname)
    }
  }
}

// 检查是否需要恢复前台用户状态
// 条件：当前不是 admin 页面，且存在保存的前台用户 token，且当前 token 与前台用户 token 不同
function shouldRestoreFrontendUser() {
  const isAdminPage = route.path?.startsWith('/admin')
  const savedFrontendToken = localStorage.getItem('frontendUserToken')
  const currentToken = localStorage.getItem('token')
  return !isAdminPage && savedFrontendToken && currentToken !== savedFrontendToken
}

// 恢复前台用户登录状态
async function restoreFrontendUser() {
  const savedUserToken = localStorage.getItem('frontendUserToken')
  const savedUserRefreshToken = localStorage.getItem('frontendUserRefreshToken')
  const savedUser = localStorage.getItem('frontendUser')

  if (savedUserToken) {
    // 恢复前台用户的 token
    localStorage.setItem('token', savedUserToken)
    if (savedUserRefreshToken) {
      localStorage.setItem('refreshToken', savedUserRefreshToken)
    }

    // 恢复前台用户数据
    if (savedUser) {
      localStorage.setItem('user', savedUser)
    }

    // 重新初始化用户状态
    userStore.initUser()

    // 从后端刷新用户信息
    await userStore.fetchCurrentUser()
  }
}

onMounted(async () => {
  // 初始化 UI Store (主题、网络监听等)
  uiStore.init()

  // 🔑 关键：检查是否需要恢复前台用户状态
  // 这处理了从后台导航回前台或刷新页面的情况
  if (shouldRestoreFrontendUser()) {
    await restoreFrontendUser()
  } else {
    // 正常初始化用户状态
    userStore.initUser()
    // 从后端同步最新用户信息（角色/认证状态等）
    const user = await userStore.fetchCurrentUser()
    
    // 加载通知和消息数据（仅当成功获取用户信息且无错误时）
    if (user && !userStore.error) {
      messageStore.fetchNotifications()
      messageStore.fetchConversations()
    }
  }

  // 预加载帖子数据
  await postStore.fetchPosts()
  await postStore.fetchHotPosts()
  
  // Note: Old message fetching block removed from here as it's now handled above
  handleSharedPost()
})

// 监听路由变化
watch(() => route.path, async (newPath, oldPath) => {
  // 当从 /admin 离开到其他页面时，恢复前台用户状态
  if (oldPath?.startsWith('/admin') && !newPath?.startsWith('/admin')) {
    console.log('离开管理后台，恢复前台用户状态')
    await restoreFrontendUser()
  }
})

// 监听路由变化，处理分享链接
watch(() => route.query.post, (newPostId) => {
  if (newPostId) {
    handleSharedPost()
  }
})
</script>
