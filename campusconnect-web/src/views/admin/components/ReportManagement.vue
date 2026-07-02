<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <div class="mb-8">
      <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">举报管理</h1>
      <p class="text-slate-500 mt-2 font-medium">处理用户投诉与违规内容举报。</p>
    </div>
    
    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>
    
    <div v-else class="grid grid-cols-1 xl:grid-cols-3 gap-8">
      <!-- Pending Column -->
      <div class="xl:col-span-2 space-y-6">
        <div class="flex items-center gap-3 mb-2">
          <div class="p-2 bg-orange-100 text-orange-600 rounded-lg">
            <ShieldAlert :size="20" />
          </div>
          <h2 class="text-lg font-bold text-slate-700">待处理举报 ({{ pendingReports.length }})</h2>
        </div>

        <div v-if="pendingReports.length === 0" class="p-12 text-center text-slate-400 bg-white/40 rounded-[2rem] border border-dashed border-slate-200 flex flex-col items-center">
          <ShieldAlert :size="48" class="mx-auto mb-4 opacity-20" />
          <p class="font-medium">暂无新的举报信息</p>
        </div>

        <div 
          v-for="report in pendingReports" 
          :key="report.id" 
          class="glass-panel p-6 rounded-[2rem] transition-all hover:shadow-lg border border-white/60 group"
        >
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-3">
                <span :class="['text-[10px] uppercase tracking-wider px-2.5 py-1 rounded-lg font-bold', report.targetType === '帖子' ? 'bg-blue-50 text-blue-600 border border-blue-100' : 'bg-purple-50 text-purple-600 border border-purple-100']">
                  {{ report.targetType }}
                </span>
                <span class="text-slate-400 text-xs font-bold flex items-center gap-1">
                  来自 <span class="text-slate-600">{{ report.reporterName }}</span>
                </span>
              </div>
              <h3 class="font-bold text-slate-800 text-lg mb-2">{{ report.reason }}</h3>
              <div class="bg-slate-50 p-3 rounded-xl border border-slate-100 flex items-center justify-between group-hover:bg-white transition-colors">
                <span class="text-xs font-mono text-slate-400">Target ID: {{ report.targetId }}</span>
                <button class="text-xs font-bold text-sage-600 hover:underline flex items-center gap-1">
                  查看原文 <ExternalLink :size="10" />
                </button>
              </div>
            </div>

            <div class="flex gap-3 w-full md:w-auto">
              <button 
                @click="handleResolve(report.id, 'ignore')"
                class="flex-1 md:flex-none px-5 py-3 bg-white border border-slate-200 text-slate-500 rounded-xl text-sm font-bold hover:bg-slate-50 hover:text-slate-700 transition-all"
              >
                忽略
              </button>
              <button 
                @click="handleResolve(report.id, 'punish')"
                class="flex-1 md:flex-none px-5 py-3 bg-red-50 text-red-600 border border-red-100 rounded-xl text-sm font-bold shadow-sm hover:bg-red-500 hover:text-white hover:border-red-500 transition-all flex items-center justify-center gap-2"
              >
                <Trash2 :size="16"/> 删除并封禁
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Resolved Column -->
      <div>
        <div class="flex items-center gap-3 mb-6">
          <div class="p-2 bg-green-100 text-green-600 rounded-lg">
            <CheckCircle :size="20" />
          </div>
          <h2 class="text-lg font-bold text-slate-700">最近已解决</h2>
        </div>

        <div class="glass-card rounded-[2rem] p-6 min-h-[500px] border border-white/50 bg-white/40">
          <div class="space-y-4">
            <div 
              v-for="report in resolvedReports" 
              :key="report.id" 
              class="p-4 bg-white rounded-2xl border border-slate-100 shadow-sm opacity-75 hover:opacity-100 transition-opacity"
            >
              <div class="flex justify-between items-center mb-2">
                <span class="text-xs font-bold text-slate-700">{{ report.reason }}</span>
                <span class="text-[10px] font-bold text-green-600 bg-green-50 border border-green-100 px-2 py-0.5 rounded-full uppercase tracking-wide">已处理</span>
              </div>
              <div class="flex justify-between items-end">
                <p class="text-xs text-slate-400 font-medium">ID: {{ report.targetId }}</p>
                <span class="text-[10px] text-slate-300">刚刚</span>
              </div>
            </div>
            <p v-if="resolvedReports.length === 0" class="text-center text-sm text-slate-400 py-10 font-medium">暂无历史处理记录</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ShieldAlert, Trash2, CheckCircle, ExternalLink, Loader2 } from 'lucide-vue-next'
import { dataService } from '../services/dataService'

const reports = ref([])
const isLoading = ref(true)

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchReports()
    reports.value = (data || []).map(r => ({
      id: r.id,
      reason: r.reason || '未知原因',
      targetType: r.targetType === 'POST' ? '帖子' : r.targetType,
      targetId: r.targetId,
      reporterName: r.reporter?.nickname || '匿名用户',
      description: r.description || '',
      status: r.status === 'PENDING' ? '待处理' : r.status === 'RESOLVED' ? '已解决' : r.status,
      createdAt: r.createdAt
    }))
  } catch (e) {
    console.error('加载举报失败:', e)
    reports.value = []
  } finally {
    isLoading.value = false
  }
})

const pendingReports = computed(() => reports.value.filter(r => r.status === '待处理'))
const resolvedReports = computed(() => reports.value.filter(r => r.status === '已解决' || r.status === 'RESOLVED' || r.status === 'IGNORED'))

const handleResolve = async (id, action) => {
  reports.value = reports.value.map(r => {
    if (r.id === id) {
      return { ...r, status: '已解决' }
    }
    return r
  })
  await dataService.resolveReport(id, action)
}
</script>
