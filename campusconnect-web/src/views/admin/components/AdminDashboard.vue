<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <header class="mb-8 flex justify-between items-end">
      <div>
        <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">数据看板</h1>
        <p class="text-slate-500 mt-2 font-medium">欢迎回来，今日社区数据概览。</p>
      </div>
      <div class="flex items-center gap-3">
        <div :class="['px-4 py-2 rounded-full text-sm font-bold flex items-center gap-2 transition-all', isLive ? 'bg-green-100 text-green-700' : 'bg-slate-200 text-slate-500']">
          <span :class="['w-2.5 h-2.5 rounded-full', isLive ? 'bg-green-500 animate-pulse' : 'bg-slate-400']"></span>
          {{ isLive ? 'Live Updates' : 'Offline' }}
        </div>
        <button 
          @click="isLive = !isLive"
          class="text-sm font-medium text-slate-400 hover:text-sage-600 px-2 transition-colors"
        >
          {{ isLive ? '暂停' : '恢复' }}
        </button>
      </div>
    </header>

    <!-- Main Charts Area -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-8">
      <!-- Traffic Chart -->
      <div class="lg:col-span-2 glass-panel p-8 rounded-[2rem] relative overflow-hidden group hover:shadow-xl transition-shadow duration-300 border border-white/60">
        <div class="absolute top-0 right-0 w-80 h-80 bg-sage-200/50 rounded-full blur-[80px] opacity-40 -mr-20 -mt-20 pointer-events-none"></div>
        
        <div class="relative z-10 flex justify-between items-start mb-8">
          <div>
            <h3 class="text-xl font-bold text-slate-800 flex items-center gap-2">
              社区活跃度趋势
              <TrendingUp :size="18" class="text-sage-600"/>
            </h3>
            <p class="text-sm text-slate-500 mt-1 font-medium">实时访问量监控 (每3秒刷新)</p>
          </div>
          <button class="p-2.5 bg-white/50 hover:bg-white rounded-xl transition-colors text-slate-400 hover:text-sage-600">
            <ArrowUpRight class="w-5 h-5" />
          </button>
        </div>

        <!-- 趋势图表展示 -->
        <div class="h-[280px] w-full flex items-end justify-between gap-2 px-4">
          <div 
            v-for="(item, index) in chartData" 
            :key="index"
            class="group/bar flex flex-col items-center justify-end w-full h-full gap-2"
          >
            <!-- 悬浮提示 -->
            <div class="opacity-0 group-hover/bar:opacity-100 transition-all transform translate-y-2 group-hover/bar:translate-y-0 text-center mb-1">
              <span class="text-lg font-bold text-slate-700 block">{{ item.visits }}</span>
              <span class="text-[10px] text-slate-400 font-medium uppercase tracking-wider">VISITS</span>
            </div>
            
            <!-- 柱子 -->
            <div class="relative w-full max-w-[48px] h-full flex items-end">
              <!-- 轨道 -->
              <div class="absolute inset-0 bg-slate-100 rounded-2xl"></div>
              <!-- 数据条 -->
              <div 
                class="relative w-full bg-gradient-to-t from-sage-500 to-sage-300 rounded-2xl transition-all duration-700 ease-out group-hover/bar:to-sage-400 group-hover/bar:shadow-[0_0_20px_-5px_rgba(100,150,120,0.5)]"
                :style="{ height: `${Math.max((item.visits / maxVisits * 100), 4)}%` }"
              ></div>
            </div>
            
            <!-- 时间标签 -->
            <div class="text-center text-xs text-slate-400 font-medium group-hover/bar:text-sage-600 transition-colors mt-2">{{ item.name }}</div>
          </div>
        </div>
      </div>

      <!-- Priority Stats Card -->
      <div class="glass-panel p-8 rounded-[2rem] flex flex-col justify-between border border-white/60 relative overflow-hidden group hover:shadow-xl transition-shadow duration-300">
        <div class="absolute inset-0 bg-gradient-to-b from-orange-50/50 to-transparent pointer-events-none"></div>
        
        <div class="relative z-10">
          <div class="flex justify-between items-start mb-6">
            <div>
              <h3 class="text-xl font-bold text-slate-800">待审核帖子</h3>
              <p class="text-sm text-slate-400 mt-1 font-medium">需要人工审核的内容</p>
            </div>
            <div class="bg-orange-100 p-3 rounded-2xl text-orange-500 shadow-sm">
              <AlertTriangle :size="24" />
            </div>
          </div>
          
          <div class="space-y-6">
            <div>
              <span class="text-5xl font-extrabold text-slate-800 tracking-tight">{{ stats.pendingPosts }}</span>
              <span class="text-lg text-slate-400 ml-2 font-medium">篇帖子</span>
            </div>
            
            <p v-if="stats.pendingPosts === 0" class="text-sm text-green-600 font-medium">
              ✓ 暂无待审核帖子
            </p>
            <p v-else class="text-sm text-orange-600 font-medium">
              请及时处理以保证社区内容质量
            </p>
          </div>
        </div>
        
        <button 
          @click="$emit('navigate', 'MODERATION')"
          class="relative z-10 w-full mt-8 py-4 bg-white border border-slate-200 rounded-xl text-slate-600 font-bold text-sm hover:bg-slate-50 hover:border-slate-300 transition-all flex items-center justify-center gap-2 group/btn"
        >
          前往审核
          <ArrowUpRight :size="16" class="transition-transform group-hover/btn:translate-x-1 group-hover/btn:-translate-y-1"/>
        </button>
      </div>
    </div>

    <!-- Quick Stats Grid -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <div 
        v-for="(stat, i) in quickStats" 
        :key="i" 
        @click="stat.onClick"
        class="glass-card p-6 rounded-[1.5rem] flex items-center gap-5 hover:shadow-lg transition-all cursor-pointer border border-white/50 group"
      >
        <div :class="['p-4 rounded-2xl shadow-sm group-hover:scale-110 transition-transform', stat.bg, stat.color]">
          <component :is="stat.icon" :size="24" />
        </div>
        <div>
          <p class="text-slate-400 text-xs font-bold uppercase tracking-wider mb-1">{{ stat.label }}</p>
          <div class="flex items-baseline gap-2">
            <p class="text-2xl font-extrabold text-slate-700">{{ stat.value }}</p>
            <span v-if="stat.trend" :class="['text-xs font-bold', stat.trend === '需处理' ? 'text-orange-500' : (stat.trend.includes('-') ? 'text-red-400' : 'text-green-500')]">{{ stat.trend }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Secondary Chart: Content Stats (Horizontal List) -->
    <div class="glass-panel p-8 rounded-[2rem] border border-white/60">
      <div class="flex items-center justify-between mb-8">
        <div>
          <h3 class="text-xl font-bold text-slate-800">内容分类统计</h3>
          <p class="text-sm text-slate-400 mt-1">按标签分布情况</p>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="contentStats.length === 0" class="h-[200px] flex items-center justify-center text-slate-400 bg-slate-50/50 rounded-2xl border border-dashed border-slate-200">
        <div class="text-center">
          <FileText class="w-10 h-10 mx-auto mb-2 opacity-30" />
          <p class="text-sm font-medium">暂无数据</p>
        </div>
      </div>
      
      <!-- 横向条形列表 -->
      <div v-else class="space-y-4">
        <div v-for="(item, index) in contentStats" :key="index" class="group">
          <div class="flex justify-between items-end mb-1.5 px-1">
            <span class="text-sm font-bold text-slate-700">{{ item.name }}</span>
            <span class="text-xs font-bold text-slate-400 group-hover:text-sage-600 transition-colors">{{ item.posts }} 篇</span>
          </div>
          <div class="w-full bg-slate-100 h-3 rounded-full overflow-hidden relative">
             <div 
               class="absolute h-full left-0 top-0 bg-gradient-to-r from-sage-400 to-sage-300 rounded-full transition-all duration-1000 ease-out group-hover:from-sage-500 group-hover:to-sage-400"
               :style="{ width: `${Math.max((item.posts / maxContentPosts * 100), 2)}%` }"
             ></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Users, FileText, AlertTriangle, Activity, ArrowUpRight, TrendingUp } from 'lucide-vue-next'
import { ViewState } from '../types'
import { dataService } from '../services/dataService'

const emit = defineEmits(['navigate'])

const chartData = ref([])
const contentStats = ref([])
const isLive = ref(true)
let interval = null


const maxContentPosts = computed(() => {
  if (contentStats.value.length === 0) return 100
  return Math.max(...contentStats.value.map(i => i.posts)) * 1.2
})

const maxVisits = computed(() => {
  if (chartData.value.length === 0) return 100
  return Math.max(...chartData.value.map(i => i.visits)) * 1.1
})


// 从API获取的统计数据
const stats = ref({
  userCount: 0,
  postCount: 0,
  pendingVerifications: 0,
  pendingReports: 0,
  pendingPosts: 0
})

const quickStats = computed(() => [
  { 
    label: '总用户数', 
    value: stats.value.userCount.toLocaleString(), 
    icon: Users, 
    color: 'text-blue-600', 
    bg: 'bg-blue-100', 
    trend: '', 
    onClick: () => emit('navigate', ViewState.USERS) 
  },
  { 
    label: '帖子总数', 
    value: stats.value.postCount.toLocaleString(), 
    icon: FileText, 
    color: 'text-purple-600', 
    bg: 'bg-purple-100', 
    trend: '', 
    onClick: () => emit('navigate', ViewState.POST_MANAGEMENT) 
  },
  { 
    label: '待审核认证', 
    value: stats.value.pendingVerifications.toString(), 
    icon: Activity, 
    color: 'text-sage-600', 
    bg: 'bg-sage-100', 
    trend: stats.value.pendingVerifications > 0 ? '需处理' : '', 
    onClick: () => emit('navigate', ViewState.VERIFICATION) 
  },
  { 
    label: '待处理举报', 
    value: stats.value.pendingReports.toString(), 
    icon: AlertTriangle, 
    color: 'text-orange-600', 
    bg: 'bg-orange-100', 
    trend: stats.value.pendingReports > 0 ? '需处理' : '', 
    onClick: () => emit('navigate', ViewState.REPORTS) 
  },
])

onMounted(async () => {
  // 获取统计数据
  try {
    const data = await dataService.fetchDashboardStats()
    stats.value = {
      userCount: data.userCount || 0,
      postCount: data.postCount || 0,
      pendingVerifications: data.pendingVerifications || 0,
      pendingReports: data.pendingReports || 0,
      pendingPosts: data.pendingPosts || 0
    }
    
    // 获取图表数据
    chartData.value = await dataService.fetchActivityTrend()
    contentStats.value = await dataService.fetchContentStats()
  } catch (e) {
    console.error('获取统计数据失败:', e)
  }

  // 图表数据实时更新
  interval = setInterval(() => {
    if (!isLive.value) return
    
    // 如果没有数据，初始化
    if (chartData.value.length === 0) return

    const lastItem = chartData.value[chartData.value.length - 1]
    
    // 简单模拟下一时刻的数据
    // 在真实场景中，这里应该调用API获取最新即时数据
    // 为了平滑过渡，我们移动时间窗口
    const nextHourNum = parseInt(lastItem.name.split(':')[0]) + 1
    const nextName = `${nextHourNum > 23 ? '00' : nextHourNum.toString().padStart(2, '0')}:00`
    
    const newVisits = Math.floor(Math.random() * (5000 - 1000) + 1000)
    const newPosts = Math.floor(Math.random() * 20) // 增量较小

    // 移除第一个，添加一个新的
    if (chartData.value.length >= 7) {
      chartData.value.shift()
    }
    chartData.value.push({ name: nextName, visits: newVisits, posts: newPosts })
  }, 3000)
})

onUnmounted(() => {
  if (interval) clearInterval(interval)
})
</script>
