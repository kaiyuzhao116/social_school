<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">内容审核</h1>
        <p class="text-slate-500 mt-2 font-medium">AI 辅助人工审核，确保社区安全。</p>
      </div>
      <button 
        @click="handleBatchAIAnalysis"
        :disabled="isBatchProcessing || posts.length === 0"
        class="group flex items-center gap-3 bg-slate-800 text-white px-6 py-3.5 rounded-2xl shadow-xl shadow-slate-200 hover:bg-slate-700 hover:-translate-y-0.5 transition-all disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
      >
        <RefreshCw v-if="isBatchProcessing" class="animate-spin w-5 h-5 text-indigo-400" />
        <Zap v-else class="w-5 h-5 fill-indigo-400 text-indigo-400 group-hover:scale-110 transition-transform" />
        <span class="font-bold tracking-wide">{{ isBatchProcessing ? '智能审计运行中...' : '一键 AI 智审' }}</span>
      </button>
    </div>
    
    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>

    <div v-else class="space-y-8">
      <!-- Empty State -->
      <div v-if="posts.length === 0" class="flex flex-col items-center justify-center py-24 opacity-60">
        <div class="w-20 h-20 bg-sage-100 rounded-full flex items-center justify-center mb-6">
          <ShieldCheck :size="40" class="text-sage-600" />
        </div>
        <p class="text-2xl font-bold text-slate-700">太棒了！</p>
        <p class="text-slate-500 mt-2 font-medium">所有待审核内容已处理完毕</p>
      </div>

      <!-- Posts List -->
      <div 
        v-for="post in posts" 
        :key="post.id" 
        :class="[
          'glass-panel p-8 rounded-[2rem] transition-all hover:shadow-xl duration-300 border border-white/60',
          analyzingId === post.id ? 'ring-2 ring-indigo-400 ring-offset-4 ring-offset-slate-50' : ''
        ]"
      >
        <!-- Header -->
        <div class="flex justify-between items-start mb-6">
          <div class="flex items-center gap-4">
            <img :src="post.userAvatar" :alt="post.userName" class="w-12 h-12 rounded-full object-cover ring-4 ring-white shadow-sm" />
            <div>
              <h4 class="font-bold text-lg text-slate-800">{{ post.userName }}</h4>
              <p class="text-sm font-medium text-slate-400">{{ post.timestamp }}</p>
            </div>
          </div>
          <span class="px-4 py-1.5 rounded-full text-xs font-bold border flex items-center gap-1.5 bg-orange-50 text-orange-600 border-orange-100">
            <span class="w-2 h-2 rounded-full bg-orange-500 animate-pulse"></span>
            待审核
          </span>
        </div>

        <!-- Content -->
        <div class="mb-8 pl-16">
          <div class="bg-white/60 p-6 rounded-2xl text-slate-700 leading-relaxed text-lg shadow-sm border border-white/40">
            {{ post.content }}
          </div>
        </div>

        <!-- AI Analysis Result -->
        <div 
          v-if="post.aiAnalysis"
          :class="[
            'mb-8 ml-16 p-5 rounded-2xl border flex items-start gap-4',
            post.aiAnalysis.safe 
              ? 'bg-emerald-50/50 border-emerald-100' 
              : 'bg-red-50/50 border-red-100'
          ]"
        >
          <div :class="['p-2.5 rounded-full mt-0.5 shrink-0 shadow-sm', post.aiAnalysis.safe ? 'bg-emerald-100 text-emerald-600' : 'bg-red-100 text-red-600']">
            <Check v-if="post.aiAnalysis.safe" class="w-5 h-5" />
            <AlertOctagon v-else class="w-5 h-5" />
          </div>
          <div class="flex-1">
            <div class="flex justify-between items-center mb-1">
              <p :class="['font-bold text-base', post.aiAnalysis.safe ? 'text-emerald-800' : 'text-red-800']">
                {{ post.aiAnalysis.safe ? 'AI 评估: 内容安全' : 'AI 评估: 存在潜在风险' }}
              </p>
              <span :class="['text-xs font-bold px-2 py-0.5 rounded border', post.aiAnalysis.safe ? 'bg-emerald-100/50 border-emerald-200 text-emerald-700' : 'bg-red-100/50 border-red-200 text-red-700']">
                {{ post.aiAnalysis.confidence }}% 置信度
              </span>
            </div>
            <p :class="['text-sm font-medium', post.aiAnalysis.safe ? 'text-emerald-700/80' : 'text-red-700/80']">
              原因: {{ post.aiAnalysis.reason }}
            </p>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center justify-between border-t border-slate-100 pt-6 ml-16">
          <button 
            @click="handleAIAnalysis(post)"
            :disabled="analyzingId === post.id || isBatchProcessing"
            class="group flex items-center gap-2.5 text-sm font-bold text-indigo-600 bg-indigo-50 px-5 py-3 rounded-xl hover:bg-indigo-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <RefreshCw v-if="analyzingId === post.id" class="animate-spin w-4 h-4" />
            <Sparkles v-else class="w-4 h-4 transition-transform group-hover:scale-110" />
            {{ analyzingId === post.id ? '深度分析中...' : '重新 AI 审计' }}
          </button>

          <div class="flex gap-4">
            <button 
              @click="handleAction(post.id, 'reject')"
              :disabled="isBatchProcessing"
              class="flex items-center gap-2 px-8 py-3 rounded-xl bg-white border border-slate-200 text-slate-600 hover:bg-red-50 hover:text-red-600 hover:border-red-200 font-bold transition-all disabled:opacity-50"
            >
              <X :size="20" /> 拒绝
            </button>
            <button 
              @click="handleAction(post.id, 'approve')"
              :disabled="isBatchProcessing"
              class="flex items-center gap-2 px-8 py-3 rounded-xl bg-sage-600 text-white hover:bg-sage-700 font-bold shadow-lg shadow-sage-200 hover:shadow-xl hover:-translate-y-0.5 transition-all disabled:opacity-50"
            >
              <Check :size="20" /> 通过发布
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Check, X, Sparkles, AlertOctagon, RefreshCw, ShieldCheck, Zap, Loader2 } from 'lucide-vue-next'
import { dataService } from '../services/dataService'
import { analyzeContentSafety } from '../services/aiService'

const posts = ref([])
const isLoading = ref(true)
const analyzingId = ref(null)
const isBatchProcessing = ref(false)

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchPosts()
    // 筛选待审核的帖子并转换格式
    posts.value = (data || [])
      .filter(p => p.status === 'PENDING' || p.status === '审核中')
      .map(p => ({
        id: p.id,
        userName: p.author?.nickname || p.user?.nickname || '用户' + p.userId,
        userAvatar: p.author?.avatar || p.user?.avatar || `https://ui-avatars.com/api/?name=U&background=6366f1&color=fff`,
        content: p.content || '',
        status: '审核中',
        aiSafe: p.aiSafe,
        aiReason: p.aiReason || '',
        aiConfidence: p.aiConfidence || 0,
        timestamp: p.createdAt ? new Date(p.createdAt).toLocaleDateString('zh-CN') : '未知'
      }))
  } catch (e) {
    console.error('加载待审核帖子失败:', e)
    posts.value = []
  } finally {
    isLoading.value = false
  }
})

const handleAIAnalysis = async (post) => {
  analyzingId.value = post.id
  try {
    const result = await analyzeContentSafety(post.content)
    posts.value = posts.value.map(p => {
      if (p.id === post.id) {
        return { ...p, aiAnalysis: result }
      }
      return p
    })
  } catch (e) {
    console.error(e)
  } finally {
    analyzingId.value = null
  }
}

const handleBatchAIAnalysis = async () => {
  isBatchProcessing.value = true
  const unanalyzedPosts = posts.value.filter(p => !p.aiAnalysis)
  for (const post of unanalyzedPosts) {
    analyzingId.value = post.id
    try {
      const result = await analyzeContentSafety(post.content)
      posts.value = posts.value.map(p => {
        if (p.id === post.id) return { ...p, aiAnalysis: result }
        return p
      })
      await new Promise(r => setTimeout(r, 800))
    } catch (e) {
      console.error(e)
    }
  }
  analyzingId.value = null
  isBatchProcessing.value = false
}

const handleAction = async (id, action) => {
  await dataService.moderatePost(id, action)
  posts.value = posts.value.filter(p => p.id !== id)
}
</script>
