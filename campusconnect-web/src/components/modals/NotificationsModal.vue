<template>
  <div class="flex flex-col h-[80vh]">
    <!-- Header -->
    <div class="flex justify-between items-center p-4 border-b border-gray-100 dark:border-gray-700 bg-gradient-to-r from-brand-purple/5 to-blue-500/5 dark:from-brand-purple/10 dark:to-blue-500/10 shrink-0">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-brand-purple to-blue-500 flex items-center justify-center text-white">
          <Bell class="w-5 h-5" />
        </div>
        <div>
          <h2 class="text-lg font-bold text-gray-900 dark:text-white">通知中心</h2>
          <p class="text-xs text-gray-400">{{ unreadCount }} 条未读</p>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <button @click="markAllAsRead" class="text-xs text-brand-purple font-medium hover:underline px-3 py-1.5 bg-brand-purple/10 rounded-full">全部已读</button>
        <button @click="uiStore.closeModal()" class="p-2 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-full text-gray-500"><X class="w-5 h-5" /></button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="flex border-b border-gray-100 dark:border-gray-700 bg-white dark:bg-gray-800 shrink-0 px-2">
      <button v-for="tab in tabs" :key="tab.id" @click="activeTab = tab.id" :class="['flex-1 py-3 text-sm font-medium transition-colors relative flex items-center justify-center gap-1.5', activeTab === tab.id ? 'text-brand-purple' : 'text-gray-500 hover:text-gray-700 dark:hover:text-gray-300']">
        <component :is="tab.icon" class="w-4 h-4" />
        {{ tab.label }}
        <span v-if="getTabCount(tab.id) > 0" :class="['text-[10px] px-1.5 py-0.5 rounded-full', activeTab === tab.id ? 'bg-brand-purple text-white' : 'bg-gray-100 dark:bg-gray-700 text-gray-500']">{{ getTabCount(tab.id) }}</span>
        <div v-if="activeTab === tab.id" class="absolute bottom-0 left-2 right-2 h-0.5 bg-brand-purple rounded-full"></div>
      </button>
    </div>

    <!-- Content -->
    <div class="flex-1 overflow-y-auto custom-scrollbar bg-gray-50 dark:bg-gray-900">
      <div v-for="(item, index) in filteredNotifications" :key="item.id" @click="handleNotificationClick(item)" :class="['p-4 hover:bg-white dark:hover:bg-gray-800 flex gap-3 cursor-pointer transition-all border-b border-gray-100 dark:border-gray-800', !item.read ? 'bg-white dark:bg-gray-800 shadow-sm' : '']" :style="{ animationDelay: `${index * 0.03}s` }">
        <div class="relative">
          <img v-if="item.avatar" :src="item.avatar" class="w-11 h-11 rounded-full object-cover" />
          <div v-else :class="['w-11 h-11 rounded-full flex items-center justify-center', getTypeStyle(item.type).bg]">
            <component :is="getTypeStyle(item.type).icon" :class="['w-5 h-5', getTypeStyle(item.type).color]" />
          </div>
          <div :class="['absolute -bottom-0.5 -right-0.5 w-5 h-5 rounded-full flex items-center justify-center border-2 border-white dark:border-gray-900', getTypeStyle(item.type).badge]">
            <component :is="getTypeStyle(item.type).icon" class="w-2.5 h-2.5 text-white" />
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm text-gray-800 dark:text-gray-200 leading-snug">
            <span class="font-bold">{{ item.title }}</span>
            <span class="text-gray-600 dark:text-gray-400"> {{ item.content }}</span>
          </p>
          <div class="flex items-center gap-2 mt-1.5">
            <span class="text-xs text-gray-400">{{ item.time }}</span>
            <span v-if="item.postTitle" class="text-xs text-gray-400 truncate max-w-[150px]">· {{ item.postTitle }}</span>
          </div>
        </div>
        <div class="flex flex-col items-end gap-2">
          <div v-if="!item.read" class="w-2.5 h-2.5 bg-brand-purple rounded-full animate-pulse"></div>
          <button v-if="item.type === 'follow' && !item.isFollowing" @click.stop="handleFollowBack(item)" class="text-xs px-3 py-1 bg-brand-purple text-white rounded-full hover:bg-indigo-600 transition-colors">回关</button>
        </div>
      </div>

      <div v-if="filteredNotifications.length === 0" class="text-center py-16">
        <div class="w-16 h-16 rounded-full bg-gray-100 dark:bg-gray-800 flex items-center justify-center mx-auto mb-4">
          <Bell class="w-8 h-8 text-gray-300 dark:text-gray-600" />
        </div>
        <p class="text-gray-400 text-sm">暂无{{ activeTab === 'all' ? '' : tabs.find(t => t.id === activeTab)?.label }}通知</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUIStore, useUserStore, useMessageStore } from '../../stores'
import { X, Bell, Heart, MessageSquare, UserPlus, Calendar, AlertCircle, BookOpen, Megaphone, Gift } from 'lucide-vue-next'

const uiStore = useUIStore()
const userStore = useUserStore()
const messageStore = useMessageStore()

const activeTab = ref('all')
const tabs = [
  { id: 'all', label: '全部', icon: Bell },
  { id: 'interaction', label: '互动', icon: Heart },
  { id: 'system', label: '系统', icon: Megaphone }
]

const notifications = computed(() => messageStore.notifications)

const unreadCount = computed(() => messageStore.totalUnreadNotifications)

const filteredNotifications = computed(() => {
  if (activeTab.value === 'all') return messageStore.notifications
  if (activeTab.value === 'system') return messageStore.notifications.filter(n => ['system', 'alert', 'event'].includes(n.type))
  return messageStore.notifications.filter(n => ['like', 'comment', 'follow', 'mention'].includes(n.type))
})

function getTabCount(tabId) {
  if (tabId === 'all') return messageStore.notifications.filter(n => !n.read).length
  if (tabId === 'system') return messageStore.notifications.filter(n => !n.read && ['system', 'alert', 'event'].includes(n.type)).length
  return messageStore.notifications.filter(n => !n.read && ['like', 'comment', 'follow', 'mention'].includes(n.type)).length
}

function getTypeStyle(type) {
  const styles = {
    like: { icon: Heart, bg: 'bg-pink-50 dark:bg-pink-900/20', color: 'text-pink-500', badge: 'bg-pink-500' },
    comment: { icon: MessageSquare, bg: 'bg-blue-50 dark:bg-blue-900/20', color: 'text-blue-500', badge: 'bg-blue-500' },
    follow: { icon: UserPlus, bg: 'bg-green-50 dark:bg-green-900/20', color: 'text-green-500', badge: 'bg-green-500' },
    mention: { icon: MessageSquare, bg: 'bg-indigo-50 dark:bg-indigo-900/20', color: 'text-indigo-500', badge: 'bg-indigo-500' },
    system: { icon: Bell, bg: 'bg-purple-50 dark:bg-purple-900/20', color: 'text-purple-500', badge: 'bg-purple-500' },
    event: { icon: Calendar, bg: 'bg-orange-50 dark:bg-orange-900/20', color: 'text-orange-500', badge: 'bg-orange-500' },
    alert: { icon: AlertCircle, bg: 'bg-red-50 dark:bg-red-900/20', color: 'text-red-500', badge: 'bg-red-500' }
  }
  return styles[type] || styles.system
}

function markAllAsRead() {
  messageStore.markAllNotificationsAsRead()
}

function handleNotificationClick(item) {
  messageStore.markNotificationAsRead(item.id)
  if (item.type === 'follow' && item.avatar) {
    uiStore.closeModal()
    uiStore.openModal('PROFILE', { name: item.title, avatar: item.avatar })
  }
}

function handleFollowBack(item) {
  item.isFollowing = true
  userStore.toggleFollow({ name: item.title, avatar: item.avatar })
}
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 4px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #d1d5db; border-radius: 2px; }
</style>
