<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">失物招领</h1>
        <p class="text-slate-500 mt-2 font-medium">管理校园内的寻物和认领信息。</p>
      </div>
      <button 
        @click="openModal()"
        class="flex items-center gap-2 bg-sage-600 text-white px-6 py-3.5 rounded-2xl shadow-lg shadow-sage-200 hover:bg-sage-700 hover:-translate-y-0.5 transition-all font-bold"
      >
        <Plus :size="20" /> 发布信息
      </button>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>

    <div v-else class="grid grid-cols-1 gap-6">
      <div 
        v-for="item in items" 
        :key="item.id" 
        :class="['glass-panel p-6 rounded-[2rem] border transition-all hover:shadow-lg group', item.priority === 'PINNED' ? 'border-sage-200 bg-sage-50/30' : 'border-white/60']"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1 pr-6">
            <div class="flex items-center gap-3 mb-2">
              <span v-if="item.priority === 'PINNED'" class="bg-red-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full flex items-center gap-1">
                <Pin :size="10" class="fill-current" /> 置顶
              </span>
              <span :class="['text-[10px] font-bold px-2 py-0.5 rounded-full border', item.type === 'LOST' ? 'bg-orange-100 text-orange-700 border-orange-200' : 'bg-green-100 text-green-700 border-green-200']">
                {{ item.type === 'LOST' ? '寻物启事' : '失物招领' }}
              </span>
              <span :class="['text-[10px] font-bold px-2 py-0.5 rounded-full border', item.status === 'OPEN' ? 'bg-blue-100 text-blue-700 border-blue-200' : 'bg-slate-100 text-slate-500 border-slate-200']">
                {{ item.status === 'OPEN' ? '进行中' : '已解决' }}
              </span>
              <span class="text-xs text-slate-400 font-medium flex items-center gap-1">
                <Calendar :size="12" /> {{ item.date }}
              </span>
            </div>
            <h3 class="text-xl font-bold text-slate-800 mb-2 group-hover:text-sage-700 transition-colors">{{ item.title }}</h3>
            <p class="text-slate-500 text-sm line-clamp-2 leading-relaxed mb-2">{{ item.description }}</p>
            <p v-if="item.location" class="text-xs text-slate-400 mb-1">📍 {{ item.location }}</p>
            <p v-if="item.contactInfo" class="text-xs text-slate-400">📞 {{ item.contactInfo }}</p>
            
            <div class="mt-4 flex items-center gap-4 text-xs font-medium text-slate-400">
               <span class="flex items-center gap-1 bg-white px-2 py-1 rounded-md border border-slate-100" v-if="item.user">
                发布者: {{ item.user.nickname || item.userId || '管理员' }}
              </span>
            </div>
          </div>

          <div class="flex flex-col gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
            <button @click="openModal(item)" class="p-2 bg-white border border-slate-200 rounded-xl text-slate-500 hover:text-blue-600 hover:bg-blue-50 transition-colors" title="编辑">
              <Edit2 :size="18" />
            </button>
             <button 
              @click="togglePin(item.id)" 
              :class="['p-2 border rounded-xl transition-colors', item.priority === 'PINNED' ? 'bg-red-50 text-red-500 border-red-100' : 'bg-white text-slate-400 border-slate-200 hover:text-red-500']" 
              :title="item.priority === 'PINNED' ? '取消置顶' : '置顶'"
            >
              <Pin :size="18" />
            </button>
            <button @click="handleDelete(item.id)" class="p-2 bg-white border border-slate-200 rounded-xl text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors" title="删除">
              <Trash2 :size="18" />
            </button>
          </div>
        </div>
      </div>
      
      <div v-if="items.length === 0" class="text-center py-20 opacity-50">
        <Search :size="48" class="mx-auto mb-4 text-slate-300" />
        <p class="text-slate-500 font-medium">暂无失物招领信息。</p>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="isModalOpen" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/20 backdrop-blur-sm p-4">
      <div class="bg-white rounded-[2rem] w-full max-w-2xl shadow-2xl border border-white/50 flex flex-col max-h-[90vh]">
        <div class="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 class="text-2xl font-bold text-slate-800 flex items-center gap-2">
            <Edit2 :size="24" class="text-sage-600"/>
            编辑信息
          </h2>
          <button @click="closeModal" class="p-2 hover:bg-slate-100 rounded-full text-slate-400 hover:text-slate-600"><X :size="20"/></button>
        </div>
        
        <div class="p-8 overflow-y-auto space-y-6">
          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">标题</label>
            <input 
              type="text" 
              v-model="formData.title"
              class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200 text-slate-700 font-bold text-lg placeholder:font-normal"
            />
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
             <div>
                <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">类型</label>
                <div class="flex bg-slate-50 p-1 rounded-xl border border-slate-100">
                  <button 
                    v-for="t in [{val:'LOST', label:'寻物启事'}, {val:'FOUND', label:'失物招领'}]"
                    :key="t.val"
                    @click="formData.type = t.val"
                    :class="['flex-1 py-2 rounded-lg text-sm font-bold transition-all', formData.type === t.val ? 'bg-white shadow-sm text-sage-700' : 'text-slate-400 hover:text-slate-600']"
                  >
                    {{ t.label }}
                  </button>
                </div>
            </div>
            <div>
                <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">状态</label>
                <div class="flex bg-slate-50 p-1 rounded-xl border border-slate-100">
                  <button 
                    v-for="s in [{val:'OPEN', label:'进行中'}, {val:'CLOSED', label:'已解决'}]"
                    :key="s.val"
                    @click="formData.status = s.val"
                    :class="['flex-1 py-2 rounded-lg text-sm font-bold transition-all', formData.status === s.val ? 'bg-white shadow-sm text-sage-700' : 'text-slate-400 hover:text-slate-600']"
                  >
                    {{ s.label }}
                  </button>
                </div>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">地点</label>
            <input 
              type="text" 
              v-model="formData.location"
              class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200 text-slate-600"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">联系方式</label>
            <input 
              type="text" 
              v-model="formData.contactInfo"
              class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200 text-slate-600"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">详细描述</label>
            <textarea 
              v-model="formData.description"
              class="w-full h-32 bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200 text-slate-600 leading-relaxed resize-none"
            ></textarea>
          </div>
        </div>

        <div class="p-6 border-t border-slate-100 flex justify-end gap-3 bg-slate-50/50 rounded-b-[2rem]">
          <button @click="closeModal" class="px-6 py-3 rounded-xl font-bold text-slate-500 hover:bg-slate-100 transition-colors">取消</button>
          <button @click="handleSave" class="px-8 py-3 bg-sage-600 text-white rounded-xl font-bold hover:bg-sage-700 shadow-lg shadow-sage-200 hover:-translate-y-0.5 transition-all flex items-center gap-2">
            <Send :size="18" /> 保存更改
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Trash2, Edit2, Calendar, X, Search, Send, Loader2, Pin } from 'lucide-vue-next'
import { dataService } from '../services/dataService'

const items = ref([])
const isLoading = ref(true)
const isModalOpen = ref(false)
const editingItem = ref(null)

const formData = ref({
  title: '',
  description: '',
  type: 'LOST',
  status: 'OPEN',
  location: '',
  contactInfo: ''
})

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchLostFoundItems()
    items.value = (data || []).map(item => ({
      ...item,
      date: item.createdAt ? new Date(item.createdAt).toLocaleDateString('zh-CN') : new Date().toLocaleDateString('zh-CN')
    }))
  } catch (e) {
    console.error('加载失物招领失败:', e)
    items.value = []
  } finally {
    isLoading.value = false
  }
})

const openModal = (item) => {
  if (item) {
    editingItem.value = item
    // 只复制需要编辑的字段，避免发送 date/user 等额外字段到后端
    formData.value = {
      title: item.title || '',
      description: item.description || '',
      type: item.type || 'LOST',
      status: item.status || 'OPEN',
      location: item.location || '',
      contactInfo: item.contactInfo || ''
    }
  } else {
    editingItem.value = null
    formData.value = {
      title: '',
      description: '',
      type: 'LOST',
      status: 'OPEN',
      location: '',
      contactInfo: ''
    }
  }
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  editingItem.value = null
}

const handleSave = async () => {
  try {
    const newItem = { ...formData.value }
    if (editingItem.value) {
      newItem.id = editingItem.value.id
    }

    console.log('准备保存失物招领:', newItem)

    // API Call
    const saved = await dataService.saveLostFoundItem(newItem)
    
    console.log('保存结果:', saved)
    
    if (editingItem.value) {
      // Update local
      items.value = items.value.map(i => i.id === editingItem.value.id ? { ...i, ...newItem } : i)
    } else {
      // Insert new
      const addedItem = saved || { ...newItem, id: Date.now() }
      items.value.unshift({
        ...addedItem,
        date: new Date().toLocaleDateString('zh-CN'),
        user: { nickname: '管理员' },
        priority: 'NORMAL'
      })
    }

    closeModal()
  } catch (error) {
    console.error('保存失物招领失败:', error)
    alert('保存失败: ' + (error.message || '未知错误'))
  }
}

const togglePin = async (id) => {
  const item = items.value.find(i => i.id === id)
  if (item) {
    const newPriority = item.priority === 'PINNED' ? 'NORMAL' : 'PINNED'
    // Optimistic update
    item.priority = newPriority
    await dataService.toggleLostFoundPin(id)
  }
}

const handleDelete = async (id) => {
  if (confirm('确定要删除这条信息吗？')) {
    await dataService.deleteLostFoundItem(id)
    items.value = items.value.filter(i => i.id !== id)
  }
}
</script>
