<template>
  <div class="space-y-8 animate-in fade-in duration-500">
    <!-- Empty Classroom Finder -->
    <section>
      <div class="flex justify-between items-center mb-4">
        <h2
          class="text-xl font-bold text-gray-900 dark:text-white flex items-center gap-2"
        >
          <Clock class="w-5 h-5 text-brand-purple" /> 自习室状态
        </h2>
        <span class="text-xs text-gray-500">实时更新: 刚刚</span>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-for="room in classrooms"
          :key="room.id"
          class="bg-white dark:bg-gray-800 p-4 rounded-xl border border-gray-100 dark:border-gray-700 shadow-sm text-center"
        >
          <h3 class="font-bold text-gray-900 dark:text-white mb-1">
            {{ room.id }}
          </h3>
          <div
            :class="[
              'inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium mb-2',
              room.status === 'empty'
                ? 'bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400'
                : room.status === 'busy'
                  ? 'bg-orange-100 text-orange-700 dark:bg-orange-900/30 dark:text-orange-400'
                  : 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400'
            ]"
          >
            <span
              :class="[
                'w-1.5 h-1.5 rounded-full',
                room.status === 'empty'
                  ? 'bg-green-500'
                  : room.status === 'busy'
                    ? 'bg-orange-500'
                    : 'bg-red-500'
              ]"
            ></span>
            {{
              room.status === 'empty'
                ? '空闲'
                : room.status === 'busy'
                  ? '有课'
                  : '拥挤'
            }}
          </div>
          <p class="text-[10px] text-gray-400">下节课: {{ room.nextClass }}</p>
        </div>
      </div>
    </section>

    <!-- Learning Resources -->
    <section>
      <div class="flex justify-between items-center mb-4">
        <h2
          class="text-xl font-bold text-gray-900 dark:text-white flex items-center gap-2"
        >
          <BookOpen class="w-5 h-5 text-blue-500" /> 学习资料库
        </h2>
        <div class="relative">
          <input
            type="text"
            placeholder="搜索资料..."
            class="pl-8 pr-4 py-1.5 text-sm bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-full focus:ring-1 focus:ring-brand-purple w-48"
          />
          <Search class="w-3.5 h-3.5 text-gray-400 absolute left-3 top-2.5" />
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4">
        <div
          v-for="res in resources"
          :key="res.id"
          class="bg-white dark:bg-gray-800 p-4 rounded-xl border border-gray-100 dark:border-gray-700 shadow-sm flex items-center justify-between group hover:border-brand-purple/30 transition-colors"
        >
          <div class="flex items-center gap-4">
            <div
              :class="[
                'w-10 h-10 rounded-lg flex items-center justify-center font-bold text-xs',
                res.type === 'PDF'
                  ? 'bg-red-50 text-red-600 dark:bg-red-900/20'
                  : res.type === 'DOCX'
                    ? 'bg-blue-50 text-blue-600 dark:bg-blue-900/20'
                    : 'bg-green-50 text-green-600 dark:bg-green-900/20'
              ]"
            >
              {{ res.type }}
            </div>
            <div>
              <h3
                class="font-bold text-gray-900 dark:text-white text-sm mb-0.5 group-hover:text-brand-purple transition-colors"
              >
                {{ res.title }}
              </h3>
              <div class="flex items-center gap-3 text-xs text-gray-400">
                <span>{{ res.size }}</span>
                <span>•</span>
                <span>贡献者: {{ res.author }}</span>
              </div>
            </div>
          </div>

          <button
            class="flex items-center gap-1 text-xs font-medium text-gray-500 hover:text-brand-purple bg-gray-50 dark:bg-gray-700 hover:bg-brand-purple/10 px-3 py-1.5 rounded-lg transition-colors"
          >
            <Download class="w-3.5 h-3.5" /> {{ res.downloads }}
          </button>
        </div>
      </div>
    </section>

    <!-- Online Courses / Groups -->
    <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <div
        class="bg-gradient-to-br from-indigo-500 to-purple-600 rounded-2xl p-6 text-white relative overflow-hidden"
      >
        <div
          class="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-8 -mt-8 blur-2xl"
        ></div>
        <div class="relative z-10">
          <div
            class="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center mb-4"
          >
            <Video class="w-5 h-5 text-white" />
          </div>
          <h3 class="font-bold text-lg mb-1">精品网课推荐</h3>
          <p class="text-indigo-100 text-sm mb-4">
            校内名师录播课程，涵盖通识课与专业核心课。
          </p>
          <button
            class="bg-white text-brand-purple px-4 py-2 rounded-lg text-sm font-bold shadow-lg hover:bg-gray-50 transition-colors"
          >
            去观看
          </button>
        </div>
      </div>

      <div
        class="bg-gradient-to-br from-orange-400 to-pink-500 rounded-2xl p-6 text-white relative overflow-hidden"
      >
        <div
          class="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-8 -mt-8 blur-2xl"
        ></div>
        <div class="relative z-10">
          <div
            class="w-10 h-10 bg-white/20 rounded-lg flex items-center justify-center mb-4"
          >
            <UsersIcon class="w-5 h-5 text-white" />
          </div>
          <h3 class="font-bold text-lg mb-1">寻找学习搭子</h3>
          <p class="text-orange-100 text-sm mb-4">
            加入考研、雅思或编程学习小组，共同进步。
          </p>
          <button
            class="bg-white text-orange-600 px-4 py-2 rounded-lg text-sm font-bold shadow-lg hover:bg-gray-50 transition-colors"
          >
            加入小组
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import {
  BookOpen,
  Download,
  Clock,
  Search,
  Users as UsersIcon,
  Video
} from 'lucide-vue-next'

const resources = [
  {
    id: 1,
    title: '高等数学(下) 期末复习重点',
    type: 'PDF',
    size: '2.4MB',
    downloads: 1205,
    author: '数学系学生会'
  },
  {
    id: 2,
    title: 'C++ 程序设计 - 往年真题解析',
    type: 'DOCX',
    size: '1.1MB',
    downloads: 890,
    author: '张明'
  },
  {
    id: 3,
    title: '宏观经济学笔记整理 (全)',
    type: 'PDF',
    size: '5.6MB',
    downloads: 450,
    author: '李伊桑'
  },
  {
    id: 4,
    title: '大学物理实验数据处理模板',
    type: 'XLSX',
    size: '0.5MB',
    downloads: 2300,
    author: '物理社'
  }
]

const classrooms = [
  { id: '3A-101', status: 'empty', capacity: 120, nextClass: '14:00' },
  { id: '3A-102', status: 'busy', capacity: 80, nextClass: '15:30' },
  { id: '3B-204', status: 'empty', capacity: 40, nextClass: '16:00' },
  { id: 'Lib-4F', status: 'crowded', capacity: 200, nextClass: '全天开放' }
]
</script>
