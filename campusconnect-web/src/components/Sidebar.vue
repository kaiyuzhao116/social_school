<template>
  <div class="w-full lg:w-80 flex-shrink-0 space-y-6">
    <!-- 1. Create Post Button -->
    <button
        @click="handleCreatePost"
        class="w-full bg-gradient-to-r from-brand-purple to-brand-blue text-white py-3 rounded-xl font-medium shadow-lg hover:shadow-xl hover:opacity-90 transition-all flex items-center justify-center gap-2 group"
    >
      <PlusCircle
          class="w-5 h-5 group-hover:rotate-90 transition-transform"
      />
      创建帖子
    </button>

    <!-- 2. Chat Entry -->
    <ChatEntryCard />

    <!-- 3. Top 10 Hot Posts -->
    <div
        class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700 transition-colors"
    >
      <h3
          class="text-brand-purple text-sm font-medium mb-4 flex items-center gap-2"
      >
        <Flame class="w-4 h-4 text-orange-500" />
        热门动态 Top 10
      </h3>
      <div class="space-y-4">
        <div
            v-for="(post, index) in postStore.topPosts"
            :key="post.id"
            @click="handlePostClick(post)"
            class="flex gap-3 items-start group cursor-pointer"
        >
          <span
              :class="[
              'text-sm font-bold w-5 text-center flex-shrink-0 pt-0.5',
              index < 3 ? 'text-orange-500' : 'text-gray-400'
            ]"
          >
            {{ index + 1 }}
          </span>
          <div class="flex-1 min-w-0">
            <p
                class="text-sm text-gray-700 dark:text-gray-200 font-medium truncate group-hover:text-brand-purple transition-colors"
            >
              {{ post.title || stripHtml(post.content) }}
            </p>
            <div class="flex items-center gap-3 mt-1.5">
              <div
                  v-if="post.user"
                  class="flex items-center gap-1 group/user"
                  @click.stop="handleUserClick(post.user)"
              >
                <img
                    :src="getAvatarUrl(post.user.avatar, post.user.name)"
                    @error="handleAvatarError"
                    class="w-3 h-3 rounded-full"
                />
                <span
                    class="text-xs text-gray-400 group-hover/user:text-brand-purple"
                >{{ post.user.name }}</span
                >
              </div>
              <span class="text-xs text-gray-400 flex items-center gap-0.5">
                <ThumbsUp class="w-3 h-3" /> {{ post.likes }}
              </span>
            </div>
          </div>
        </div>
        <p
            v-if="!postStore.topPosts || postStore.topPosts.length === 0"
            class="text-xs text-gray-400 text-center py-2"
        >
          暂无热门内容
        </p>
      </div>
    </div>

    <!-- 4. Calendar -->
    <div
        class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700 transition-colors"
    >
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-brand-purple text-sm font-medium">校园日历</h3>
        <div class="flex gap-1">
          <button
              class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded text-gray-600 dark:text-gray-400"
          >
            <ChevronLeft class="w-4 h-4" />
          </button>
          <span
              class="text-xs font-semibold text-gray-700 dark:text-gray-200 pt-1"
          >{{ currentMonthName }}</span
          >
          <button
              class="p-1 hover:bg-gray-100 dark:hover:bg-gray-700 rounded text-gray-600 dark:text-gray-400"
          >
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>

      <div class="grid grid-cols-7 gap-1 text-center mb-2">
        <span
            v-for="d in ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa']"
            :key="d"
            class="text-[10px] text-gray-400 uppercase"
        >{{ d }}</span
        >
      </div>
      <div class="grid grid-cols-7 gap-1 text-center mb-4">
        <div
            v-for="i in startDayOffset"
            :key="`empty-${i}`"
        ></div>
        <div
            v-for="d in daysInMonth"
            :key="d"
            @click="selectedDay = d"
            :class="[
            'relative text-xs h-8 w-8 flex items-center justify-center rounded-full cursor-pointer transition-all',
            selectedDay === d
              ? 'bg-brand-purple text-white shadow-md scale-105'
              : d === todayDate
                ? 'bg-blue-50 text-brand-purple border border-brand-purple/20 font-bold'
                : 'text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
          ]"
        >
          {{ d }}
          <span
              v-if="getEventsForDate(d).length > 0 && selectedDay !== d"
              :class="[
              'absolute bottom-1 w-1 h-1 rounded-full',
              getEventsForDate(d).some((e) => e.type === 'holiday')
                ? 'bg-red-500'
                : 'bg-blue-400'
            ]"
          ></span>
        </div>
      </div>

      <div class="border-t border-gray-100 dark:border-gray-700 pt-3">
        <h4 class="text-xs font-semibold text-gray-500 dark:text-gray-400 mb-2">
          {{ selectedDay }}日 安排
        </h4>
        <div v-if="selectedEvents.length > 0" class="space-y-2">
          <div
              v-for="event in selectedEvents"
              :key="event.id"
              class="flex items-center gap-2 text-sm"
          >
            <span
                :class="[
                'w-1.5 h-1.5 rounded-full',
                event.type === 'holiday'
                  ? 'bg-red-500'
                  : event.type === 'exam'
                    ? 'bg-orange-500'
                    : 'bg-blue-500'
              ]"
            ></span>
            <span
                :class="[
                'flex-1 truncate',
                event.type === 'holiday'
                  ? 'text-red-600 dark:text-red-400 font-medium'
                  : 'text-gray-700 dark:text-gray-300'
              ]"
            >
              {{ event.title }}
            </span>
            <span
                v-if="event.type === 'holiday'"
                class="text-[10px] px-1.5 py-0.5 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded"
            >休假</span
            >
          </div>
        </div>
        <p v-else class="text-xs text-gray-400 text-center py-2">
          今日无特别安排
        </p>
      </div>
    </div>

    <!-- 5. Announcements & Lost Found -->
    <div
        class="bg-white dark:bg-gray-800 rounded-2xl p-6 shadow-sm border border-gray-100 dark:border-gray-700 transition-colors"
    >
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-brand-purple text-sm font-medium">公告 & 失物招领</h3>
        <span
            @click="showAllItems = !showAllItems"
            class="text-xs text-gray-400 cursor-pointer hover:text-gray-600 dark:hover:text-gray-200 transition-colors"
        >{{ showAllItems ? '▲' : '▼' }}</span
        >
      </div>

      <!-- 加载状态 -->
      <div v-if="loadingItems" class="text-center py-4">
        <div class="w-5 h-5 border-2 border-brand-purple border-t-transparent rounded-full animate-spin mx-auto"></div>
        <p class="text-xs text-gray-400 mt-2">加载中...</p>
      </div>

      <div v-else class="space-y-4">
        <!-- 公告列表 -->
        <div v-for="item in displayedAnnouncements" :key="'ann-' + item.id">
          <div class="flex items-center gap-2 mb-1">
            <span class="w-2 h-2 rounded-full bg-blue-400"></span>
            <h4 class="text-sm font-medium text-blue-600 dark:text-blue-400">公告</h4>
            <span v-if="item.priority === 'PINNED'" class="text-[10px] px-1.5 py-0.5 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded">置顶</span>
          </div>
          <h5 class="text-sm font-semibold text-gray-800 dark:text-gray-200 mb-1 line-clamp-1">
            {{ item.title }}
          </h5>
          <p class="text-xs text-gray-500 dark:text-gray-400 leading-relaxed line-clamp-2">
            {{ item.content }}
          </p>
          <p class="text-[10px] text-gray-400 mt-1">{{ item.publisher }}</p>
        </div>

        <!-- 失物招领列表 -->
        <div v-for="item in displayedLostFound" :key="'lf-' + item.id">
          <div class="flex items-center gap-2 mb-1">
            <span
                :class="[
                'w-2 h-2 rounded-full',
                item.type === 'LOST' ? 'bg-orange-400' : 'bg-green-400'
              ]"
            ></span>
            <h4
                :class="[
                'text-sm font-medium',
                item.type === 'LOST'
                  ? 'text-orange-600 dark:text-orange-400'
                  : 'text-green-600 dark:text-green-400'
              ]"
            >
              {{ item.type === 'LOST' ? '寻物启事' : '失物招领' }}
            </h4>
            <span v-if="item.priority === 'PINNED'" class="text-[10px] px-1.5 py-0.5 bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400 rounded">置顶</span>
          </div>
          <h5 class="text-sm font-semibold text-gray-800 dark:text-gray-200 mb-1">
            {{ item.title }}
          </h5>
          <p class="text-xs text-gray-500 dark:text-gray-400 leading-relaxed line-clamp-2">
            {{ item.description }}
          </p>
          <p v-if="item.location" class="text-[10px] text-gray-400 mt-1">📍 {{ item.location }}</p>
        </div>

        <!-- 空状态 -->
        <p
            v-if="announcements.length === 0 && lostFoundItems.length === 0"
            class="text-xs text-gray-400 text-center py-2"
        >
          暂无公告和失物招领信息
        </p>
      </div>
    </div>

    <!-- 6. Daily Quote -->
    <div
        class="bg-gradient-to-br from-pink-50 to-rose-50 dark:from-pink-900/20 dark:to-rose-900/20 rounded-2xl p-5 border border-pink-100 dark:border-pink-800 transition-colors relative group"
    >
      <div class="flex items-center justify-between mb-3">
        <div
            class="flex items-center gap-2 text-pink-600 dark:text-pink-400 font-semibold text-sm"
        >
          <Quote class="w-4 h-4 fill-current" />
          每日一言
        </div>
        <button
            @click="fetchHitokoto"
            :disabled="loadingQuote"
            class="text-pink-400 hover:text-pink-600 dark:hover:text-pink-300 transition-colors"
        >
          <RefreshCw :class="['w-4 h-4', loadingQuote ? 'animate-spin' : '']" />
        </button>
      </div>
      <div
          class="text-gray-700 dark:text-gray-200 min-h-[40px] flex flex-col justify-center"
      >
        <template v-if="!hitokoto">加载中...</template>
        <template v-else-if="hitokotoParts.length > 1">
          <p
              class="font-serif italic text-gray-800 dark:text-gray-100 text-[15px] leading-normal"
          >
            {{ hitokotoParts[0] }}
          </p>
          <hr class="border-pink-200 dark:border-pink-800/50 my-2" />
          <p class="text-sm text-gray-600 dark:text-gray-300">
            {{ hitokotoParts[1] }}
          </p>
        </template>
        <p v-else class="italic font-serif text-gray-800 dark:text-gray-200">
          {{ hitokoto }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { usePostStore, useUserStore, useUIStore } from '../stores'
import request from '../api/request'
import { getAvatarUrl, handleAvatarError } from '../utils/avatar'
import ChatEntryCard from './home/ChatEntryCard.vue'
import {
  PlusCircle,
  ChevronLeft,
  ChevronRight,
  Flame,
  ThumbsUp,
  Quote,
  RefreshCw
} from 'lucide-vue-next'

const postStore = usePostStore()
const userStore = useUserStore()
const uiStore = useUIStore()

// Calendar
const currentDate = ref(new Date())
const year = computed(() => currentDate.value.getFullYear())
const month = computed(() => currentDate.value.getMonth())
const todayDate = computed(() => currentDate.value.getDate())
const currentMonthName = computed(() =>
    currentDate.value.toLocaleString('en-US', { month: 'long', year: 'numeric' })
)
const selectedDay = ref(new Date().getDate())

const getDaysInMonth = (y, m) => new Date(y, m + 1, 0).getDate()
const getFirstDayOfMonth = (y, m) => new Date(y, m, 1).getDay()

const daysInMonth = computed(() => getDaysInMonth(year.value, month.value))
const startDayOffset = computed(() => getFirstDayOfMonth(year.value, month.value))

const calendarEvents = [
  { id: 'e1', date: 5, title: '期中考试周开始', type: 'exam' },
  { id: 'e2', date: 18, title: '校庆日放假', type: 'holiday' },
  { id: 'e3', date: 18, title: '校园美食节', type: 'event' },
  { id: 'e4', date: 25, title: '职业招聘会', type: 'event' },
  { id: 'e5', date: 10, title: '图书馆闭馆维护', type: 'event' }
]

const getEventsForDate = (date) => {
  return calendarEvents.filter((e) => e.date === date)
}

const selectedEvents = computed(() => getEventsForDate(selectedDay.value))

// Announcements & Lost Found
const announcements = ref([])
const lostFoundItems = ref([])
const loadingItems = ref(false)
const showAllItems = ref(false)

// 显示最多2条公告和2条失物招领，展开后显示全部
const displayedAnnouncements = computed(() => {
  if (showAllItems.value) return announcements.value
  return announcements.value.slice(0, 2)
})

const displayedLostFound = computed(() => {
  if (showAllItems.value) return lostFoundItems.value
  return lostFoundItems.value.slice(0, 2)
})

const fetchAnnouncementsAndLostFound = async () => {
  loadingItems.value = true
  try {
    // 并行获取公告和失物招领
    const [annRes, lfRes] = await Promise.all([
      request.get('/system/announcements'),
      request.get('/system/lost-found', { params: { limit: 5 } })
    ])
    announcements.value = annRes.data || annRes || []
    lostFoundItems.value = lfRes.data || lfRes || []
  } catch (error) {
    console.error('获取公告和失物招领失败:', error)
    announcements.value = []
    lostFoundItems.value = []
  } finally {
    loadingItems.value = false
  }
}

// Hitokoto
const hitokoto = ref('')
const loadingQuote = ref(false)
const hitokotoParts = computed(() => hitokoto.value.split('/&/').map((s) => s.trim()))

const fetchHitokoto = async () => {
  loadingQuote.value = true
  try {
    const res = await fetch('https://api.bugpk.com/api/yiyan')
    if (res.ok) {
      hitokoto.value = await res.text()
    } else {
      hitokoto.value = '生活明朗，万物可爱。'
    }
  } catch (error) {
    console.error('Failed to fetch quote', error)
    hitokoto.value = '星光不问赶路人，时光不负有心人。'
  } finally {
    loadingQuote.value = false
  }
}

onMounted(() => {
  fetchHitokoto()
  fetchAnnouncementsAndLostFound()
})

// Helpers
function stripHtml(html) {
  return html.replace(/<[^>]*>?/gm, '')
}

function handleCreatePost() {
  if (userStore.isLoggedIn) {
    uiStore.openModal('CREATE_POST')
  } else {
    uiStore.openModal('LOGIN')
  }
}

function handleUserClick(user) {
  uiStore.openModal('PROFILE', user)
}

function handlePostClick(post) {
  uiStore.openModal('POST_DETAIL', post)
}
</script>