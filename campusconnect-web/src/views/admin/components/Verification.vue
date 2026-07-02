<template>
  <div class="h-full overflow-y-auto pr-2 pb-20 no-scrollbar">
    <h1 class="text-3xl font-bold text-slate-800 mb-6">身份认证</h1>
    
    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>
    
    <div v-else class="grid grid-cols-1 xl:grid-cols-2 gap-6">
      <div 
        v-for="req in requests" 
        :key="req.id" 
        class="glass-panel p-0 rounded-3xl overflow-hidden flex flex-col border border-white/60"
      >
        <div class="h-48 bg-slate-200 relative group">
          <img :src="req.idCardImage" alt="ID Card" class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105" />
          <div class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent flex items-end p-6">
            <div class="text-white">
              <p class="text-xs opacity-80 uppercase tracking-widest font-medium">上传证件</p>
            </div>
          </div>
          <div class="absolute top-4 right-4 bg-white/90 backdrop-blur px-3 py-1 rounded-full text-xs font-bold text-slate-700 flex items-center gap-1.5 shadow-sm">
            <component :is="getTypeIcon(req.identityType)" :size="16" />
            {{ req.identityType }}认证
          </div>
        </div>
        
        <div class="p-6 flex-1 flex flex-col">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-xl font-bold text-slate-800">{{ req.userName }}</h3>
              <p class="text-slate-500">{{ req.department }}</p>
            </div>
            <div class="bg-sage-100 text-sage-700 px-3 py-1 rounded-lg text-sm font-mono">
              #{{ req.idNumber }}
            </div>
          </div>

          <div class="flex items-center gap-2 mb-6 text-sm text-slate-400">
            <CreditCard :size="16" />
            <span>提交时间: {{ req.submittedAt }}</span>
          </div>
          
          <div class="mt-auto flex gap-3">
            <button 
              @click="handleDecision(req.id, '已拒绝')"
              class="flex-1 py-3 rounded-xl border border-slate-200 text-slate-600 font-medium hover:bg-red-50 hover:text-red-600 hover:border-red-200 transition-colors flex items-center justify-center gap-2"
            >
              <X :size="18" /> 拒绝
            </button>
            <button 
              @click="handleDecision(req.id, '已通过')"
              class="flex-1 py-3 rounded-xl bg-sage-500 text-white font-medium hover:bg-sage-600 shadow-lg shadow-sage-200 transition-all flex items-center justify-center gap-2"
            >
              <Check :size="18" /> 通过认证
            </button>
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="requests.length === 0" class="col-span-full py-20 text-center opacity-50 flex flex-col items-center">
        <div class="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mb-4">
          <Check :size="32" class="text-slate-400" />
        </div>
        <p class="font-bold text-slate-600">全部处理完成</p>
        <p class="text-sm text-slate-400 mt-1">暂无待处理的认证请求</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Check, X, CreditCard, Building2, GraduationCap, UserSquare2, Loader2 } from 'lucide-vue-next'
import { dataService } from '../services/dataService'

const requests = ref([])
const isLoading = ref(true)

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchVerifications()
    // 后端返回 PENDING，转换为前端格式
    requests.value = (data || []).map(r => ({
      id: r.id,
      userName: r.user?.nickname || r.realName || '未知用户',
      identityType: r.identityType === 'STUDENT' ? '学生' : r.identityType === 'TEACHER' ? '教师' : '部门',
      department: r.department || '未填写',
      idNumber: r.idNumber || '',
      idCardImage: r.idCardImage || 'https://picsum.photos/400/250?random=1',
      submittedAt: r.createdAt ? new Date(r.createdAt).toLocaleDateString('zh-CN') : '未知',
      status: r.status === 'PENDING' ? '待审核' : r.status
    })).filter(r => r.status === '待审核')
  } catch (e) {
    console.error('加载认证数据失败:', e)
    requests.value = []
  } finally {
    isLoading.value = false
  }
})

const handleDecision = async (id, status) => {
  requests.value = requests.value.filter(r => r.id !== id)
  // 转换状态值为后端期望的格式
  const backendStatus = status === '已通过' ? 'APPROVED' : 'REJECTED'
  await dataService.reviewVerification(id, backendStatus)
}

const getTypeIcon = (type) => {
  switch(type) {
    case '学生': return GraduationCap
    case '教师': return UserSquare2
    case '部门': return Building2
    default: return CreditCard
  }
}
</script>
