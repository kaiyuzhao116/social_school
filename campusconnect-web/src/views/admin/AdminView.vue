<template>
  <!-- 登录页面 -->
  <div v-if="!isLoggedIn"
    class="min-h-screen w-full flex items-center justify-center relative overflow-hidden bg-slate-50">
    <div class="absolute top-[-10%] left-[-10%] w-[40vw] h-[40vw] rounded-full bg-sage-300 blur-[100px] opacity-20">
    </div>
    <div class="absolute bottom-[-10%] right-[-10%] w-[30vw] h-[30vw] rounded-full bg-blue-300 blur-[80px] opacity-20">
    </div>

    <div class="glass-panel p-12 rounded-[2.5rem] w-full max-w-md relative z-10 shadow-2xl border border-white/50">
      <div class="text-center mb-10">
        <div
          class="w-20 h-20 bg-gradient-to-br from-sage-500 to-sage-600 rounded-2xl mx-auto mb-6 flex items-center justify-center shadow-xl shadow-sage-200">
          <span class="text-white text-4xl font-bold">P</span>
        </div>
        <h1 class="text-3xl font-bold text-slate-800 tracking-tight">校园脉动</h1>
        <p class="text-slate-500 mt-2 text-sm font-medium">智能后台管理系统</p>
      </div>

      <div class="space-y-5">
        <div>
          <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">用户名</label>
          <input type="text" v-model="loginForm.username" @keyup.enter="handleLogin"
            class="w-full bg-white/60 border border-slate-200 rounded-xl px-5 py-3.5 outline-none focus:ring-2 focus:ring-sage-400/50 text-slate-700 transition-all focus:bg-white"
            placeholder="admin" />
        </div>
        <div>
          <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">密码</label>
          <input type="password" v-model="loginForm.password" @keyup.enter="handleLogin"
            class="w-full bg-white/60 border border-slate-200 rounded-xl px-5 py-3.5 outline-none focus:ring-2 focus:ring-sage-400/50 text-slate-700 transition-all focus:bg-white"
            placeholder="••••••••" />
        </div>
        <p v-if="loginError" class="text-red-500 text-sm text-center">{{ loginError }}</p>
        <button @click="handleLogin" :disabled="isLoggingIn"
          class="w-full bg-sage-600 text-white font-bold py-4 rounded-xl shadow-lg shadow-sage-200 hover:bg-sage-700 hover:scale-[1.02] active:scale-[0.98] transition-all mt-4 text-base disabled:opacity-70 disabled:cursor-not-allowed flex items-center justify-center gap-2">
          <Loader2 v-if="isLoggingIn" class="animate-spin w-5 h-5" />
          {{ isLoggingIn ? '登录中...' : '安全登录' }}
        </button>
      </div>
    </div>
  </div>

  <!-- 主界面 -->
  <div v-else class="flex h-screen w-full relative bg-[#f4f6f4]">
    <!-- Background Decor -->
    <div class="fixed inset-0 pointer-events-none">
      <div class="absolute top-0 right-0 w-[500px] h-[500px] bg-sage-200/40 rounded-full blur-[120px] -mr-32 -mt-32">
      </div>
      <div class="absolute bottom-0 left-0 w-[600px] h-[600px] bg-blue-100/40 rounded-full blur-[150px] -ml-20 -mb-20">
      </div>
    </div>

    <AdminSidebar :current-view="currentView" @navigate="handleNavigate" @logout="handleLogout" />

    <!-- Main Content Area -->
    <main
      class="flex-1 ml-24 lg:ml-72 mr-4 md:mr-8 pt-6 h-full relative z-10 transition-all duration-300 flex flex-col">

      <!-- Top Bar -->
      <div class="flex justify-between items-center mb-6 pl-2">
        <div></div>

        <div class="flex items-center gap-5">
          <!-- Search Trigger -->
          <div class="relative hidden md:block group">
            <Search
              class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 group-hover:text-sage-500 transition-colors w-4 h-4" />
            <input readonly @click="showSearch = true" type="text" placeholder="全局搜索 (Ctrl+K)"
              class="pl-10 pr-4 py-3 bg-white border border-transparent rounded-2xl text-sm outline-none w-64 shadow-sm hover:shadow-md hover:border-sage-100 transition-all cursor-text text-slate-600 placeholder:text-slate-400" />
          </div>

          <!-- Notification Trigger -->
          <div class="relative">
            <button @click="showNotifications = !showNotifications"
              :class="['p-3 rounded-2xl transition-all shadow-sm', showNotifications ? 'bg-sage-100 text-sage-600' : 'bg-white text-slate-500 hover:text-sage-600 hover:shadow-md']">
              <Bell :size="20" />
              <span v-if="unreadCount > 0"
                class="absolute top-2.5 right-2.5 w-2 h-2 bg-red-500 rounded-full border border-white animate-pulse"></span>
            </button>

            <!-- Notification Panel -->
            <div v-if="showNotifications"
              class="absolute right-0 top-16 w-96 bg-white rounded-2xl shadow-xl border border-slate-100 z-50 overflow-hidden">
              <div class="px-5 py-4 border-b border-slate-50 flex justify-between items-center bg-white">
                <h3 class="font-bold text-slate-800">通知中心</h3>
                <button @click="showNotifications = false"
                  class="text-xs font-medium text-sage-600 hover:text-sage-700 hover:bg-sage-50 px-2 py-1 rounded-md transition-colors">全部已读</button>
              </div>
              <div class="max-h-[400px] overflow-y-auto">
                <div v-if="notifications.length === 0" class="p-8 text-center text-slate-400 text-sm">暂无新通知</div>
                <div v-for="n in notifications" :key="n.id"
                  :class="['p-5 border-b border-slate-50 hover:bg-slate-50/80 transition-colors cursor-pointer group', !n.read ? 'bg-blue-50/40' : '']">
                  <div class="flex justify-between items-start mb-2">
                    <p :class="['text-sm', !n.read ? 'font-bold text-slate-800' : 'font-medium text-slate-600']">{{
                      n.title }}</p>
                    <span
                      class="text-[10px] text-slate-400 bg-slate-100 px-1.5 py-0.5 rounded group-hover:bg-white transition-colors">{{
                        n.time }}</span>
                  </div>
                  <p class="text-xs text-slate-500 leading-relaxed">{{ n.message }}</p>
                </div>
              </div>
              <div class="p-3 bg-slate-50 text-center border-t border-slate-100">
                <button
                  class="text-xs font-medium text-slate-500 hover:text-sage-600 transition-colors w-full py-1">查看历史记录</button>
              </div>
            </div>
          </div>

          <!-- Admin Profile Trigger -->
          <div v-if="adminProfile" @click="showProfileModal = true"
            class="flex items-center gap-3 pl-5 border-l border-slate-200/60 cursor-pointer group">
            <div class="text-right hidden sm:block">
              <p class="text-sm font-bold text-slate-700 group-hover:text-sage-700 transition-colors">{{
                adminProfile.name }}</p>
              <p class="text-xs text-slate-400 font-medium">{{ adminProfile.role }}</p>
            </div>
            <div class="relative">
              <img :src="adminProfile.avatar"
                class="w-11 h-11 rounded-full border-[3px] border-white shadow-sm object-cover group-hover:scale-105 transition-transform"
                alt="Admin" />
              <div class="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-white rounded-full"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Scrollable Content -->
      <div class="flex-1 overflow-hidden relative rounded-t-[2.5rem] p-1">
        <AdminDashboard v-if="currentView === ViewState.DASHBOARD" @navigate="handleNavigate" />
        <PostModeration v-else-if="currentView === ViewState.MODERATION" />
        <Verification v-else-if="currentView === ViewState.VERIFICATION" />
        <UserManagement v-else-if="currentView === ViewState.USERS" />
        <ReportManagement v-else-if="currentView === ViewState.REPORTS" />
        <AnnouncementManagement v-else-if="currentView === ViewState.ANNOUNCEMENTS" />
        <PostManagement v-else-if="currentView === ViewState.POST_MANAGEMENT" />
        <ActivityManagement v-else-if="currentView === ViewState.ACTIVITIES" />
        <LostFoundManagement v-else-if="currentView === ViewState.LOST_FOUND" />
        <AdminDashboard v-else @navigate="handleNavigate" />
      </div>
    </main>

    <!-- Admin Profile Modal -->
    <div v-if="showProfileModal && adminProfile"
      class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/20 backdrop-blur-sm">
      <div
        class="bg-white rounded-[2rem] p-8 w-full max-w-md shadow-2xl border border-white/50 transform scale-100 transition-all">
        <div class="flex justify-between items-center mb-8">
          <h2 class="text-2xl font-bold text-slate-800">编辑资料</h2>
          <button @click="showProfileModal = false"
            class="p-2 hover:bg-slate-100 rounded-full text-slate-400 hover:text-slate-600 transition-colors">
            <X :size="20" />
          </button>
        </div>

        <div class="flex flex-col items-center mb-8">
          <div class="relative group cursor-pointer">
            <img :src="profileForm.avatar" alt="Avatar"
              class="w-28 h-28 rounded-full object-cover border-4 border-slate-50 shadow-md" />
            <div
              class="absolute inset-0 bg-black/40 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all backdrop-blur-[2px]">
              <Camera class="text-white w-8 h-8" />
            </div>
          </div>
        </div>

        <div class="space-y-5">
          <div>
            <label class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5 block">昵称</label>
            <input v-model="profileForm.name"
              class="w-full p-3.5 bg-slate-50 rounded-xl border-none outline-none focus:ring-2 focus:ring-sage-300 transition-all font-medium text-slate-700" />
          </div>
          <div>
            <label class="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1.5 block">职位</label>
            <div class="relative">
              <input :value="profileForm.role" disabled
                class="w-full p-3.5 bg-slate-100 rounded-xl border-none outline-none font-medium text-slate-500 cursor-not-allowed" />
              <Lock :size="16" class="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400" />
            </div>
            <p class="text-[10px] text-slate-400 mt-1 pl-1">如需修改职位权限，请联系超级管理员</p>
          </div>
          <button @click="handleSaveProfile" :disabled="isSaving"
            class="w-full py-3.5 bg-sage-600 text-white font-bold rounded-xl mt-6 hover:bg-sage-700 transition-all shadow-lg shadow-sage-200 flex items-center justify-center gap-2 disabled:opacity-70">
            <Loader2 v-if="isSaving" class="animate-spin" />
            <Save v-else :size="18" />
            保存修改
          </button>
        </div>
      </div>
    </div>

    <!-- Search Overlay -->
    <div v-if="showSearch"
      class="fixed inset-0 bg-slate-900/20 backdrop-blur-md z-[60] flex flex-col items-center pt-32"
      @click="showSearch = false">
      <div class="w-full max-w-2xl px-6" @click.stop>
        <div class="relative transform transition-all hover:scale-[1.01]">
          <Search class="absolute left-6 top-1/2 -translate-y-1/2 text-sage-500 w-6 h-6" />
          <input autofocus type="text" placeholder="搜索全站内容 (例如: 用户ID, 帖子关键词...)"
            class="w-full pl-16 pr-6 py-6 bg-white rounded-3xl shadow-2xl text-lg outline-none border border-white/50 placeholder:text-slate-300 font-medium text-slate-700" />
          <button @click="showSearch = false"
            class="absolute right-6 top-1/2 -translate-y-1/2 p-2 bg-slate-100 rounded-full text-slate-400 hover:bg-slate-200 hover:text-slate-600 transition-all">
            <X :size="20" />
          </button>
        </div>
        <div class="mt-10 text-center">
          <p class="text-white/80 text-sm font-medium mb-4 tracking-wide">快速跳转</p>
          <div class="flex justify-center gap-3 flex-wrap">
            <button v-for="item in quickNavItems" :key="item.label" @click="handleQuickNav(item.view)"
              class="px-5 py-2.5 bg-white/90 backdrop-blur border border-white/50 rounded-full text-slate-600 text-sm font-medium hover:bg-white hover:text-sage-600 hover:shadow-lg hover:-translate-y-0.5 transition-all">
              {{ item.label }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { Search, Bell, X, Camera, Save, Loader2, Lock } from 'lucide-vue-next'
import { ViewState } from './types'
import { dataService } from './services/dataService'

import AdminSidebar from './components/AdminSidebar.vue'
import AdminDashboard from './components/AdminDashboard.vue'
import PostModeration from './components/PostModeration.vue'
import Verification from './components/Verification.vue'
import UserManagement from './components/UserManagement.vue'
import ReportManagement from './components/ReportManagement.vue'
import AnnouncementManagement from './components/AnnouncementManagement.vue'
import PostManagement from './components/PostManagement.vue'
import ActivityManagement from './components/ActivityManagement.vue'
import LostFoundManagement from './components/LostFoundManagement.vue'

// 状态
// 同步检查 localStorage，避免刷新时闪现登录页
const isLoggedIn = ref(!!localStorage.getItem('adminToken'))
const currentView = ref(ViewState.DASHBOARD)
const loginForm = ref({ username: '', password: '' })
const loginError = ref('')
const isLoggingIn = ref(false)

const adminProfile = ref(null)
const showProfileModal = ref(false)
const profileForm = ref({})
const isSaving = ref(false)

const showNotifications = ref(false)
const showSearch = ref(false)
const notifications = ref([])
const unreadCount = ref(0)

const quickNavItems = [
  { label: '待审核帖子', view: ViewState.MODERATION },
  { label: '今日活跃用户', view: ViewState.DASHBOARD },
  { label: '最近举报', view: ViewState.REPORTS },
  { label: '发布公告', view: ViewState.ANNOUNCEMENTS }
]

// 初始化
onMounted(async () => {
  dataService.initDB()
  
  // 检查是否有保存的管理员 token，尝试恢复登录状态
  const savedAdminToken = localStorage.getItem('adminToken')
  if (savedAdminToken) {
    // 确保 token 被设置（request 拦截器需要用）
    localStorage.setItem('token', savedAdminToken)
    
    try {
      // 尝试获取管理员资料，验证 token 是否有效
      const profile = await dataService.fetchAdminProfile()
      if (profile && profile.name) {
        // token 有效，恢复登录状态
        isLoggedIn.value = true
        adminProfile.value = profile
        profileForm.value = { ...profile }
      }
    } catch (e) {
      console.error('恢复管理员登录状态失败:', e)
      // token 无效，清除
      localStorage.removeItem('adminToken')
      localStorage.removeItem('adminRefreshToken')
    }
  }
})

// 监听登录状态和视图变化
watch([isLoggedIn, currentView], async () => {
  if (isLoggedIn.value) {
    adminProfile.value = await dataService.fetchAdminProfile()
    profileForm.value = { ...adminProfile.value }
    const notes = await dataService.fetchNotifications()
    notifications.value = notes
    unreadCount.value = notes.filter(n => !n.read).length
  }
}, { immediate: true })

// 方法
const handleLogin = async () => {
  loginError.value = ''

  // 验证输入
  if (!loginForm.value.username.trim()) {
    loginError.value = '请输入用户名'
    return
  }
  if (!loginForm.value.password.trim()) {
    loginError.value = '请输入密码'
    return
  }

  isLoggingIn.value = true
  try {
    const res = await dataService.login(loginForm.value.username, loginForm.value.password)
    if (res && res.user) {
      // 检查是否有管理员权限
      if (res.user.role !== 'ADMIN' && res.user.role !== 'MODERATOR') {
        loginError.value = '您没有管理员权限'
        localStorage.removeItem('token')
        localStorage.removeItem('refreshToken')
        return
      }
      isLoggedIn.value = true
    }
  } catch (error) {
    loginError.value = error.message || '登录失败，请检查用户名和密码'
  } finally {
    isLoggingIn.value = false
  }
}

const handleLogout = () => {
  isLoggedIn.value = false
  currentView.value = ViewState.DASHBOARD
  loginForm.value = { username: '', password: '' }
  loginError.value = ''

  // 清除管理员 token
  localStorage.removeItem('adminToken')
  localStorage.removeItem('adminRefreshToken')

  // 尝试恢复前台用户完整状态（如果之前有保存的话）
  const savedUserToken = localStorage.getItem('frontendUserToken')
  const savedUserRefreshToken = localStorage.getItem('frontendUserRefreshToken')
  const savedUser = localStorage.getItem('frontendUser')
  if (savedUserToken) {
    localStorage.setItem('token', savedUserToken)
    localStorage.setItem('refreshToken', savedUserRefreshToken || '')
    if (savedUser) {
      localStorage.setItem('user', savedUser)
    }
  } else {
    // 如果没有保存的前台用户 token，则清除当前 token
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }
}

const handleNavigate = (view) => {
  currentView.value = view
  showSearch.value = false
}

const handleQuickNav = (view) => {
  handleNavigate(view)
}

const handleSaveProfile = async () => {
  isSaving.value = true
  await dataService.updateAdminProfile(profileForm.value)
  adminProfile.value = { ...profileForm.value }
  isSaving.value = false
  showProfileModal.value = false
}
</script>

<style scoped>
.glass-panel {
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.glass-card {
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.5);
}

.no-scrollbar::-webkit-scrollbar {
  display: none;
}

.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
