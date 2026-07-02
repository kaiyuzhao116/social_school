<template>
  <div class="space-y-6 animate-in fade-in duration-500">
    <div class="flex justify-between items-end mb-2">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">
          校园活动
        </h2>
        <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
          发现精彩校园生活，参与你感兴趣的活动
        </p>
      </div>
      <button class="text-sm text-brand-purple font-medium hover:underline">
        查看日历视图
      </button>
    </div>

    <div class="grid gap-6">
      <div
        v-for="event in events"
        :key="event.id"
        class="bg-white dark:bg-gray-800 rounded-2xl p-4 md:p-0 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-md transition-all flex flex-col md:flex-row overflow-hidden group"
      >
        <!-- Image Section -->
        <div
          class="w-full md:w-64 h-48 md:h-auto shrink-0 relative overflow-hidden rounded-xl md:rounded-none"
        >
          <img
            :src="event.image"
            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
            :alt="event.title"
          />
          <div
            class="absolute top-3 left-3 bg-white/90 dark:bg-gray-900/90 backdrop-blur-sm px-3 py-1 rounded-lg text-center shadow-sm"
          >
            <span class="block text-xs text-gray-500 uppercase tracking-wide">{{
              event.date.split('-')[1]
            }}月</span>
            <span
              class="block text-xl font-bold text-gray-900 dark:text-white"
              >{{ event.date.split('-')[2] }}</span
            >
          </div>
        </div>

        <!-- Content Section -->
        <div
          class="flex-1 p-0 md:p-6 flex flex-col justify-between mt-4 md:mt-0"
        >
          <div>
            <div class="flex justify-between items-start">
              <span
                :class="[
                  'px-2.5 py-0.5 rounded text-xs font-bold mb-2 inline-block',
                  event.color
                ]"
              >
                {{ event.category }}
              </span>
              <button
                class="text-gray-400 hover:text-brand-purple transition-colors"
              >
                <Calendar class="w-5 h-5" />
              </button>
            </div>

            <h3
              class="text-xl font-bold text-gray-900 dark:text-white mb-2 group-hover:text-brand-purple transition-colors"
            >
              {{ event.title }}
            </h3>

            <div class="space-y-2 text-sm text-gray-500 dark:text-gray-400">
              <div class="flex items-center gap-2">
                <Clock class="w-4 h-4" />
                <span>{{ event.time }}</span>
              </div>
              <div class="flex items-center gap-2">
                <MapPin class="w-4 h-4" />
                <span>{{ event.location }}</span>
              </div>
              <div class="flex items-center gap-2">
                <Users class="w-4 h-4" />
                <span>{{ event.attendees }} 人已报名</span>
              </div>
            </div>
          </div>

          <div
            class="flex items-center justify-between mt-6 pt-4 border-t border-gray-100 dark:border-gray-700"
          >
            <div class="flex items-center gap-2 text-sm">
              <span class="text-gray-400">主办方:</span>
              <span class="font-medium text-gray-900 dark:text-white">{{
                event.organizer
              }}</span>
            </div>
            <button
              class="flex items-center gap-1 text-sm font-bold text-brand-purple hover:text-indigo-600 transition-colors"
            >
              立即报名 <ArrowRight class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Calendar, MapPin, Users, Clock, ArrowRight } from 'lucide-vue-next'

const events = [
  {
    id: 1,
    title: '2025 春季校园歌手大赛 - 决赛',
    category: '文娱',
    date: '2025-05-20',
    time: '19:00',
    location: '大学生活动中心大礼堂',
    organizer: '校学生会',
    image: 'https://picsum.photos/seed/event1/800/400',
    attendees: 1240,
    color: 'bg-purple-100 text-purple-600'
  },
  {
    id: 2,
    title: '人工智能与未来 - 专家讲座',
    category: '学术',
    date: '2025-05-22',
    time: '14:30',
    location: '图书馆报告厅',
    organizer: '计算机学院',
    image: 'https://picsum.photos/seed/event2/800/400',
    attendees: 350,
    color: 'bg-blue-100 text-blue-600'
  },
  {
    id: 3,
    title: '社团招新百团大战',
    category: '社团',
    date: '2025-05-25',
    time: '09:00 - 17:00',
    location: '南区体育场',
    organizer: '社团联合会',
    image: 'https://picsum.photos/seed/event3/800/400',
    attendees: 5000,
    color: 'bg-orange-100 text-orange-600'
  },
  {
    id: 4,
    title: '大学生职业规划沙龙',
    category: '就业',
    date: '2025-05-28',
    time: '15:00',
    location: '就业指导中心 201',
    organizer: '招生就业处',
    image: 'https://picsum.photos/seed/event4/800/400',
    attendees: 80,
    color: 'bg-green-100 text-green-600'
  }
]
</script>
