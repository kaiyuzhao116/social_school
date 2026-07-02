<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <div class="flex flex-col md:flex-row justify-between items-start md:items-end mb-8 gap-6">
      <div>
        <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">帖子管理</h1>
        <p class="text-slate-500 mt-2 font-medium">管理所有已发布的社区帖子。</p>
      </div>
      
      <div class="relative w-full md:w-72 group">
        <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-hover:text-sage-500 transition-colors w-4 h-4" />
        <input 
          type="text" 
          placeholder="搜索内容或作者..." 
          v-model="searchTerm"
          class="w-full pl-11 pr-4 py-3 bg-white rounded-2xl border border-slate-200 text-sm font-medium outline-none focus:ring-2 focus:ring-sage-300 transition-shadow shadow-sm hover:shadow-md"
        />
      </div>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>

    <div v-else class="grid grid-cols-1 gap-4">
      <div 
        v-for="post in filteredPosts" 
        :key="post.id" 
        :class="['bg-white/60 p-6 rounded-[1.5rem] border transition-all hover:bg-white hover:shadow-lg flex gap-6 group', post.isPinned ? 'border-sage-300 ring-1 ring-sage-100' : 'border-slate-100']"
      >
        <div class="flex-shrink-0">
          <img :src="post.userAvatar" alt="" class="w-12 h-12 rounded-full object-cover" />
        </div>
        <div class="flex-1">
          <div class="flex justify-between items-start">
            <div>
              <div class="flex items-center gap-2 mb-1">
                <span class="font-bold text-slate-800">{{ post.userName }}</span>
                <span v-if="post.isPinned" class="text-[10px] font-bold bg-sage-100 text-sage-700 px-2 py-0.5 rounded-full flex items-center gap-1">
                  <Pin :size="10" class="fill-current" /> 置顶
                </span>
                <span class="text-xs text-slate-400">· {{ post.timestamp }}</span>
              </div>
              <p class="text-slate-600 leading-relaxed mb-3">{{ post.content }}</p>
            </div>
          </div>
          
          <div class="flex items-center gap-4 border-t border-slate-50 pt-3">
            <span :class="['text-xs font-bold px-2 py-0.5 rounded-full border', post.status === '已发布' ? 'bg-green-50 text-green-600 border-green-100' : 'bg-orange-50 text-orange-600 border-orange-100']">
              {{ post.status }}
            </span>
            
            <div class="flex-1"></div>

            <div class="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <button 
                @click="handlePin(post.id)"
                :class="['p-2 rounded-xl border transition-all', post.isPinned ? 'bg-sage-50 text-sage-600 border-sage-200' : 'bg-white text-slate-400 border-slate-200 hover:text-sage-600 hover:border-sage-200']"
                :title="post.isPinned ? '取消置顶' : '置顶'"
              >
                <PinOff v-if="post.isPinned" :size="16" />
                <Pin v-else :size="16" />
              </button>
              <button 
                @click="handleDelete(post.id)"
                class="p-2 bg-white border border-slate-200 rounded-xl text-slate-400 hover:text-red-600 hover:bg-red-50 hover:border-red-200 transition-all"
                title="删除帖子"
              >
                <Trash2 :size="16" />
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="filteredPosts.length === 0" class="text-center py-20 text-slate-400 font-medium">
        未找到相关帖子
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Trash2, Pin, PinOff, Search, Loader2 } from 'lucide-vue-next'
import { dataService } from '../services/dataService'

const posts = ref([])
const isLoading = ref(true)
const searchTerm = ref('')

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchPosts()
    // 转换后端数据格式
    posts.value = (data || []).map(p => ({
      id: p.id,
      userName: p.author?.nickname || p.user?.nickname || '用户' + p.userId,
      userAvatar: p.author?.avatar || p.user?.avatar || `https://ui-avatars.com/api/?name=U&background=6366f1&color=fff`,
      content: p.content || '',
      status: p.status === 'PUBLISHED' ? '已发布' : p.status === 'PENDING' ? '待审核' : p.status,
      isPinned: p.isPinned || p.is_pinned || false,
      timestamp: p.createdAt ? new Date(p.createdAt).toLocaleDateString('zh-CN') : '未知'
    }))
  } catch (e) {
    console.error('加载帖子失败:', e)
    posts.value = []
  } finally {
    isLoading.value = false
  }
})

const filteredPosts = computed(() => {
  return posts.value.filter(p => 
    p.content.includes(searchTerm.value) || p.userName.includes(searchTerm.value)
  )
})

const handleDelete = async (id) => {
  if (confirm('确认删除此帖子吗？此操作不可恢复。')) {
    await dataService.deletePost(id)
    posts.value = posts.value.filter(p => p.id !== id)
  }
}

const handlePin = async (id) => {
  await dataService.togglePostPin(id)
  posts.value = posts.value.map(p => {
    if (p.id === id) {
      return { ...p, isPinned: !p.isPinned }
    }
    return p
  })
}
</script>
