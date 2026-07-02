<template>
  <div
    class="h-[calc(100vh-140px)] w-full flex flex-col md:flex-row gap-4 animate-in fade-in duration-500"
  >
    <!-- Sidebar / Filter Panel -->
    <div
      class="w-full md:w-80 bg-white dark:bg-gray-800 rounded-2xl shadow-sm border border-gray-100 dark:border-gray-700 p-4 flex flex-col z-10"
    >
      <div class="relative mb-4">
        <input
          type="text"
          placeholder="搜索地点..."
          class="w-full bg-gray-50 dark:bg-gray-700 border-none rounded-xl py-3 pl-10 pr-4 text-sm focus:ring-2 focus:ring-brand-purple"
        />
        <Search class="absolute left-3 top-3 w-4 h-4 text-gray-400" />
      </div>

      <div
        class="flex flex-row md:flex-col gap-2 overflow-x-auto md:overflow-visible pb-2 md:pb-0 no-scrollbar"
      >
        <button
          v-for="filter in filters"
          :key="filter.id"
          @click="activeFilter = filter.id"
          :class="[
            'flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-sm font-medium whitespace-nowrap',
            activeFilter === filter.id
              ? 'bg-brand-purple text-white shadow-md'
              : 'bg-gray-50 dark:bg-gray-700/50 text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700'
          ]"
        >
          <component :is="filter.icon" class="w-4 h-4" />
          {{ filter.label }}
        </button>
      </div>

      <div
        class="mt-4 flex-1 overflow-y-auto hidden md:block custom-scrollbar"
      >
        <h3
          class="text-xs font-bold text-gray-400 uppercase tracking-wider mb-2"
        >
          热门地点
        </h3>
        <div class="space-y-2">
          <div
            v-for="loc in filteredLocations"
            :key="loc.id"
            class="flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer border border-transparent hover:border-gray-100 dark:hover:border-gray-700 transition-colors group"
          >
            <div class="flex items-center gap-3">
              <div
                :class="[
                  'w-8 h-8 rounded-full flex items-center justify-center text-white',
                  loc.color
                ]"
              >
                <component :is="loc.icon" class="w-4 h-4" />
              </div>
              <span
                class="text-sm font-medium text-gray-700 dark:text-gray-200"
                >{{ loc.name }}</span
              >
            </div>
            <Navigation
              class="w-4 h-4 text-gray-300 group-hover:text-brand-purple"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Map Area -->
    <div
      class="flex-1 bg-blue-50 dark:bg-gray-900 rounded-2xl relative overflow-hidden shadow-inner border border-gray-200 dark:border-gray-700 group"
    >
      <!-- Mock Map Background -->
      <div
        class="absolute inset-0 opacity-20 dark:opacity-10 bg-[url('https://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Campus_map.jpg/800px-Campus_map.jpg')] bg-cover bg-center"
      ></div>
      <div
        class="absolute inset-0 bg-[radial-gradient(#cbd5e1_1px,transparent_1px)] [background-size:16px_16px] opacity-50"
      ></div>

      <!-- Pins -->
      <div
        v-for="loc in filteredLocations"
        :key="loc.id"
        class="absolute transform -translate-x-1/2 -translate-y-1/2 cursor-pointer group/pin hover:z-20 transition-all duration-300"
        :style="{ left: `${loc.x}%`, top: `${loc.y}%` }"
      >
        <div class="relative flex flex-col items-center">
          <div
            :class="[
              'w-10 h-10 rounded-full shadow-lg flex items-center justify-center text-white group-hover/pin:scale-125 transition-transform duration-300 ring-4 ring-white dark:ring-gray-800',
              loc.color
            ]"
          >
            <component :is="loc.icon" class="w-5 h-5" />
          </div>
          <div
            class="absolute top-12 bg-white dark:bg-gray-800 text-gray-900 dark:text-white px-3 py-1.5 rounded-lg shadow-xl text-xs font-bold whitespace-nowrap opacity-0 group-hover/pin:opacity-100 transition-opacity transform translate-y-2 group-hover/pin:translate-y-0 pointer-events-none"
          >
            {{ loc.name }}
            <div
              class="absolute -top-1.5 left-1/2 -translate-x-1/2 w-3 h-3 bg-white dark:bg-gray-800 transform rotate-45"
            ></div>
          </div>
        </div>
      </div>

      <!-- Controls -->
      <div class="absolute bottom-6 right-6 flex flex-col gap-2">
        <button
          class="w-10 h-10 bg-white dark:bg-gray-800 rounded-lg shadow-lg flex items-center justify-center text-gray-600 dark:text-gray-300 hover:text-brand-purple"
        >
          <Navigation class="w-5 h-5 transform rotate-45" />
        </button>
        <button
          class="w-10 h-10 bg-white dark:bg-gray-800 rounded-lg shadow-lg flex items-center justify-center text-xl font-bold text-gray-600 dark:text-gray-300 hover:text-brand-purple"
        >
          +
        </button>
        <button
          class="w-10 h-10 bg-white dark:bg-gray-800 rounded-lg shadow-lg flex items-center justify-center text-xl font-bold text-gray-600 dark:text-gray-300 hover:text-brand-purple"
        >
          -
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, markRaw } from 'vue'
import {
  MapPin,
  Navigation,
  Search,
  Coffee,
  Book,
  Utensils,
  Home
} from 'lucide-vue-next'

const activeFilter = ref('all')

const locations = [
  {
    id: 1,
    name: '中心图书馆',
    type: 'study',
    x: 40,
    y: 30,
    icon: markRaw(Book),
    color: 'bg-blue-500'
  },
  {
    id: 2,
    name: '第一食堂',
    type: 'food',
    x: 60,
    y: 50,
    icon: markRaw(Utensils),
    color: 'bg-orange-500'
  },
  {
    id: 3,
    name: '瑞幸咖啡',
    type: 'cafe',
    x: 25,
    y: 60,
    icon: markRaw(Coffee),
    color: 'bg-amber-700'
  },
  {
    id: 4,
    name: '东区宿舍 A栋',
    type: 'dorm',
    x: 75,
    y: 20,
    icon: markRaw(Home),
    color: 'bg-purple-500'
  },
  {
    id: 5,
    name: '行政楼',
    type: 'office',
    x: 50,
    y: 45,
    icon: markRaw(MapPin),
    color: 'bg-gray-500'
  }
]

const filters = [
  { id: 'all', label: '全部', icon: markRaw(MapPin) },
  { id: 'study', label: '学习', icon: markRaw(Book) },
  { id: 'food', label: '餐饮', icon: markRaw(Utensils) },
  { id: 'dorm', label: '宿舍', icon: markRaw(Home) }
]

const filteredLocations = computed(() => {
  if (activeFilter.value === 'all') return locations
  return locations.filter((l) => l.type === activeFilter.value)
})
</script>
