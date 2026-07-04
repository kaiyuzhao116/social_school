<template>
  <nav
    class="sticky top-0 z-50 bg-white/80 dark:bg-gray-900/80 backdrop-blur-md border-b border-gray-100 dark:border-gray-800 shadow-sm transition-colors duration-300"
  >
    <div class="max-w-7xl mx-auto px-6 lg:px-8">
      <div class="flex justify-between h-16 items-center">
        <!-- Logo & Desktop Menu -->
        <div class="flex items-center gap-8">
          <div
            class="flex-shrink-0 flex items-center gap-2 cursor-pointer"
            @click="navigateTo('/')"
          >
            <div
                class="w-9 h-9 bg-gradient-to-br from-sky-500 to-cyan-500 rounded-xl flex items-center justify-center text-white font-black text-lg shadow-md shadow-sky-200"
            >
              渤
            </div>
            <div class="flex flex-col leading-tight">
  <span class="font-bold text-lg tracking-tight text-gray-900 dark:text-white block">
    渤大校园脉动
  </span>
              <span class="text-[10px] text-sky-500 font-semibold hidden lg:block">
    Bohai University Life Hub
  </span>
            </div>
          </div>

          <!-- Desktop Menu -->
          <div class="flex items-center space-x-1 text-sm font-medium">
            <button
              v-for="item in navItems"
              :key="item.id"
              @click="navigateTo(item.path)"
              :class="[
                'px-3 py-2 rounded-lg transition-colors',
                isActive(item.path)
                  ? 'text-brand-purple bg-brand-purple/5'
                  : 'text-gray-600 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:bg-gray-50 dark:hover:bg-gray-800'
              ]"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <!-- Search & Actions -->
        <div class="flex items-center gap-4 flex-none justify-end">
          <div class="relative w-64">
            <div
              class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none"
            >
              <Search class="h-4 w-4 text-gray-400" />
            </div>
            <input
              type="text"
              class="block w-full pl-9 pr-3 py-2 border border-gray-200 dark:border-gray-700 rounded-full leading-5 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-white placeholder-gray-400 focus:outline-none focus:bg-white dark:focus:bg-gray-700 focus:ring-2 focus:ring-brand-purple focus:border-brand-purple text-sm transition-all"
              placeholder="搜索渤大动态、拼团、活动..."
              v-model="searchTerm"
              @keydown.enter="handleSearch"
            />
          </div>

          <div class="flex items-center gap-2">
            <button
              @click="uiStore.toggleTheme()"
              class="block p-2 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full transition-colors"
            >
              <Sun v-if="uiStore.isDarkMode" class="h-5 w-5" />
              <Moon v-else class="h-5 w-5" />
            </button>

            <!-- Notification Dropdown -->
            <div class="relative group">
              <button
                @click="handleNotificationClick"
                class="p-2 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full transition-colors relative"
              >
                <Bell class="h-5 w-5" />
                <span
                  v-if="userStore.isLoggedIn && unreadNotifications > 0"
                  class="absolute top-1 right-1 min-w-[16px] h-4 bg-red-500 rounded-full text-white text-[10px] font-bold flex items-center justify-center px-1"
                >{{ unreadNotifications > 9 ? '9+' : unreadNotifications }}</span>
              </button>

              <div
                v-if="userStore.isLoggedIn"
                class="absolute right-0 top-full pt-2 invisible group-hover:visible opacity-0 group-hover:opacity-100 transition-all duration-200 transform origin-top-right z-50"
              >
                <div
                  class="w-80 bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl rounded-2xl shadow-xl border border-gray-100 dark:border-gray-700 overflow-hidden ring-1 ring-black/5"
                >
                  <div
                    class="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50"
                  >
                    <h3 class="font-bold text-gray-900 dark:text-white text-sm">
                      通知
                    </h3>
                    <span
                      @click="markAllAsRead"
                      class="text-xs text-brand-purple cursor-pointer hover:underline"
                      >全部标为已读</span
                    >
                  </div>
                  <div class="max-h-[320px] overflow-y-auto custom-scrollbar">
                    <div
                      v-for="item in notifications"
                      :key="item.id"
                      @click="handleNotificationItemClick(item)"
                      class="p-3 hover:bg-white/50 dark:hover:bg-gray-700/50 flex gap-3 cursor-pointer transition-colors border-b border-gray-50 dark:border-gray-700/30 last:border-0 relative"
                    >
                      <div
                        v-if="!item.read"
                        class="absolute left-1.5 top-5 w-1.5 h-1.5 bg-red-500 rounded-full"
                      ></div>
                      <div
                        :class="['w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0', getNotificationStyle(item.type).bg]"
                      >
                        <component :is="getNotificationStyle(item.type).icon" :class="['w-5 h-5', getNotificationStyle(item.type).color]" />
                      </div>
                      <div class="flex-1 min-w-0">
                        <p
                          class="text-sm text-gray-800 dark:text-gray-200 leading-snug"
                        >
                          <span class="font-semibold">{{ item.title }}</span>
                          {{ item.content }}
                        </p>
                        <p class="text-xs text-gray-400 mt-1">{{ item.time }}</p>
                      </div>
                    </div>
                  </div>
                  <div
                    class="p-3 border-t border-gray-100 dark:border-gray-700 text-center bg-gray-50/50 dark:bg-gray-900/50"
                  >
                    <button
                      @click="uiStore.openModal('NOTIFICATIONS_FULL')"
                      class="text-xs font-medium text-gray-500 hover:text-brand-purple transition-colors flex items-center justify-center gap-1 w-full"
                    >
                      查看所有通知 <ChevronRight class="w-3 h-3" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Messages Dropdown -->
            <div class="relative group block">
              <button
                @click="handleMessageClick"
                class="p-2 text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-full transition-colors relative"
              >
                <MessageCircle class="h-5 w-5" />
                <span
                  v-if="userStore.isLoggedIn && unreadMessages > 0"
                  class="absolute top-1 right-1 min-w-[16px] h-4 bg-red-500 rounded-full text-white text-[10px] font-bold flex items-center justify-center px-1"
                >{{ unreadMessages > 9 ? '9+' : unreadMessages }}</span>
              </button>

              <div
                v-if="userStore.isLoggedIn"
                class="absolute right-0 top-full pt-2 invisible group-hover:visible opacity-0 group-hover:opacity-100 transition-all duration-200 transform origin-top-right z-50"
              >
                <div
                  class="w-80 bg-white/80 dark:bg-gray-800/80 backdrop-blur-xl rounded-2xl shadow-xl border border-gray-100 dark:border-gray-700 overflow-hidden ring-1 ring-black/5"
                >
                  <div
                    class="p-4 border-b border-gray-100 dark:border-gray-700 flex justify-between items-center bg-gray-50/50 dark:bg-gray-900/50"
                  >
                    <h3 class="font-bold text-gray-900 dark:text-white text-sm">
                      私信
                    </h3>
                    <span
                      v-if="unreadMessages > 0"
                      class="bg-brand-purple text-white text-[10px] px-1.5 py-0.5 rounded-full"
                      >{{ unreadMessages }}</span
                    >
                  </div>
                  <div class="max-h-[320px] overflow-y-auto custom-scrollbar">
                    <div
                      v-for="item in messages"
                      :key="item.id"
                      @click="openMessageChat(item)"
                      class="p-3 hover:bg-white/50 dark:hover:bg-gray-700/50 flex gap-3 cursor-pointer transition-colors border-b border-gray-50 dark:border-gray-700/30 last:border-0 relative"
                    >
                      <div v-if="item.unread" class="absolute left-1.5 top-5 w-1.5 h-1.5 bg-red-500 rounded-full"></div>
                      <img
                        :src="item.avatar"
                        class="w-10 h-10 rounded-full object-cover ring-2 ring-white dark:ring-gray-700"
                        :alt="item.name"
                      />
                      <div class="flex-1 min-w-0">
                        <div class="flex justify-between items-baseline mb-0.5">
                          <span
                            class="font-semibold text-sm text-gray-900 dark:text-white truncate"
                            >{{ item.name }}</span
                          >
                          <span class="text-[10px] text-gray-400 flex-shrink-0">{{
                            item.time
                          }}</span>
                        </div>
                        <p
                          class="text-xs text-gray-500 dark:text-gray-400 truncate leading-relaxed"
                        >
                          {{ formatLastMessage(item.lastMessage) }}
                        </p>
                      </div>
                    </div>
                  </div>
                  <div
                    class="p-3 border-t border-gray-100 dark:border-gray-700 text-center bg-gray-50/50 dark:bg-gray-900/50"
                  >
                    <button
                      @click="uiStore.openModal('MESSAGES_FULL')"
                      class="text-xs font-medium text-brand-purple hover:text-indigo-600 transition-colors w-full"
                    >
                      进入消息中心
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <template v-if="userStore.isLoggedIn && userStore.currentUser">
              <button
                @click="uiStore.openModal('PROFILE')"
                class="flex items-center gap-2 pl-1 pr-2 py-1 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors border border-transparent hover:border-gray-200 dark:hover:border-gray-700 ml-1"
              >
                <img
                  :src="getAvatarUrl(userStore.currentUser.avatar, userStore.currentUser.name)"
                  @error="handleAvatarError"
                  :alt="userStore.currentUser.name"
                  class="w-8 h-8 rounded-full object-cover ring-2 ring-brand-purple/20"
                />
                <span
                  class="text-sm font-medium text-gray-700 dark:text-gray-200 block"
                  >{{ userStore.currentUser.name }}</span
                >
              </button>
            </template>
            <template v-else>
              <button
                @click="uiStore.openModal('LOGIN')"
                class="flex items-center gap-2 bg-brand-purple hover:bg-indigo-600 text-white px-5 py-2 rounded-full text-sm font-medium transition-colors shadow-md hover:shadow-lg ml-2"
              >
                <LogIn class="h-4 w-4" />
                <span>登录</span>
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore, useUIStore, usePostStore, useMessageStore } from '../stores'
import {
  Search,
  LogIn,
  Bell,
  MessageCircle,
  Moon,
  Sun,
  ChevronRight,
  Heart,
  MessageSquare,
  UserPlus,
  Calendar,
  AlertCircle,
  BookOpen
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const uiStore = useUIStore()
const postStore = usePostStore()
const messageStore = useMessageStore()

const searchTerm = ref('')

const navItems = [
  { id: 'HOME', label: '首页', path: '/' },
  { id: 'EVENTS', label: '活动', path: '/events' },
  { id: 'GROUP_BUY', label: '拼团', path: '/group-buy' },
  { id: 'STUDY', label: '学习', path: '/study' },
  { id: 'MAP', label: '地图', path: '/map' }
]

const notifications = computed(() => messageStore.notifications)
const messages = computed(() => messageStore.contacts.filter(c => !c.isGroup).slice(0, 5))

const unreadNotifications = computed(() => messageStore.totalUnreadNotifications)
const unreadMessages = computed(() => messageStore.totalUnreadMessages)

function getNotificationStyle(type) {
  const styles = {
    like: { icon: Heart, bg: 'bg-pink-50 dark:bg-pink-900/20', color: 'text-pink-500' },
    comment: { icon: MessageSquare, bg: 'bg-blue-50 dark:bg-blue-900/20', color: 'text-blue-500' },
    follow: { icon: UserPlus, bg: 'bg-green-50 dark:bg-green-900/20', color: 'text-green-500' },
    system: { icon: Bell, bg: 'bg-purple-50 dark:bg-purple-900/20', color: 'text-purple-500' },
    event: { icon: Calendar, bg: 'bg-orange-50 dark:bg-orange-900/20', color: 'text-orange-500' },
    alert: { icon: AlertCircle, bg: 'bg-red-50 dark:bg-red-900/20', color: 'text-red-500' },
    course: { icon: BookOpen, bg: 'bg-indigo-50 dark:bg-indigo-900/20', color: 'text-indigo-500' }
  }
  return styles[type] || styles.system
}

function handleNotificationClick() {
  if (!userStore.isLoggedIn) {
    uiStore.openModal('LOGIN')
  }
}

function handleNotificationItemClick(item) {
  messageStore.markNotificationAsRead(item.id)
}

function markAllAsRead() {
  messageStore.markAllNotificationsAsRead()
}

function handleMessageClick() {
  if (!userStore.isLoggedIn) {
    uiStore.openModal('LOGIN')
  }
}

function openMessageChat(item) {
  messageStore.markContactAsRead(item.id)
  // 传递 conversationId 表示这是已存在的会话，不需要创建新会话
  uiStore.openModal('MESSAGES_FULL', { conversationId: item.id, name: item.name, avatar: item.avatar })
}

function navigateTo(path) {
  router.push(path)
}

function isActive(path) {
  return route.path === path
}

function handleSearch() {
  postStore.setSearchQuery(searchTerm.value)
  if (route.path !== '/') {
    router.push('/')
  }
}

// 格式化最后一条消息（图片显示为[图片]）
function formatLastMessage(message) {
  if (!message) return ''
  if (message.startsWith('/uploads/') || message.startsWith('http') || message.includes('/images/')) {
    return '[图片]'
  }
  return message
}

// 使用公共头像工具函数
import { getAvatarUrl, handleAvatarError } from '../utils/avatar'
</script>
