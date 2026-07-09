<template>
  <aside class="fixed left-4 top-4 bottom-4 w-20 lg:w-64 glass-panel rounded-[2rem] flex flex-col justify-between py-8 z-50 transition-all duration-300">
    <div class="px-0 lg:px-6 flex flex-col items-center lg:items-start h-full overflow-y-auto no-scrollbar">
      <div class="mb-8 flex items-center gap-3 justify-center lg:justify-start w-full px-2 shrink-0">
        <div class="w-10 h-10 rounded-xl bg-sage-500 flex items-center justify-center shadow-lg shadow-sage-200">
          <span class="text-white font-bold text-xl">P</span>
        </div>
        <span class="hidden lg:block font-bold text-xl text-slate-800 tracking-tight">校园脉动</span>
      </div>

      <nav class="space-y-3 w-full">
        <button
          v-for="item in menuItems"
          :key="item.id"
          @click="$emit('navigate', item.id)"
          :class="[
            'w-full flex items-center justify-center lg:justify-start gap-4 p-3 lg:px-4 rounded-2xl transition-all duration-300 group relative',
            currentView === item.id 
              ? 'bg-sage-500 text-white shadow-lg shadow-sage-200' 
              : 'text-slate-500 hover:bg-white hover:text-sage-600'
          ]"
        >
          <component :is="item.icon" :size="22" :class="currentView === item.id ? 'text-white' : ''" />
          <span :class="['hidden lg:block font-medium', currentView !== item.id && 'group-hover:translate-x-1 transition-transform']">
            {{ item.label }}
          </span>
          <div 
            v-if="currentView === item.id"
            class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-sage-600 rounded-r-full lg:hidden"
          ></div>
        </button>
      </nav>
    </div>

    <div class="px-0 lg:px-6 shrink-0 mt-4">
      <button 
        @click="$emit('logout')"
        class="w-full flex items-center justify-center lg:justify-start gap-4 p-3 lg:px-4 rounded-2xl text-red-400 hover:bg-red-50 hover:text-red-500 transition-colors"
      >
        <LogOut :size="22" />
        <span class="hidden lg:block font-medium">退出登录</span>
      </button>
    </div>
  </aside>
</template>

<script setup>
import {
  LayoutGrid,
  Gauge,
  CheckCircle,
  ShieldAlert,
  Users,
  Flag,
  LogOut,
  Megaphone,
  FileText,
  Calendar,
  Search,
  Database
} from 'lucide-vue-next'
import { ViewState } from '../types'

defineProps({
  currentView: {
    type: String,
    required: true
  }
})

defineEmits(['navigate', 'logout'])

const menuItems = [
  { id: ViewState.DASHBOARD, icon: LayoutGrid, label: '数据看板' },
  { id: ViewState.TRAFFIC_CONTROL, icon: Gauge, label: '系统流控' },
  { id: ViewState.VERIFICATION, icon: CheckCircle, label: '身份认证' },
  { id: ViewState.POST_MANAGEMENT, icon: FileText, label: '帖子管理' },
  { id: ViewState.MODERATION, icon: ShieldAlert, label: '内容审核' },
  { id: ViewState.ACTIVITIES, icon: Calendar, label: '活动发布' },
  { id: ViewState.ANNOUNCEMENTS, icon: Megaphone, label: '公告管理' },
  { id: ViewState.LOST_FOUND, icon: Search, label: '失物招领' },
  { id: ViewState.USERS, icon: Users, label: '用户管理' },
  { id: ViewState.REPORTS, icon: Flag, label: '举报管理' },
  { id: ViewState.KNOWLEDGE_IMPORT, icon: Database, label: '知识库导入' }
]
</script>
