<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">公告管理</h1>
        <p class="text-slate-500 mt-2 font-medium">发布重要通知，管理校园资讯看板。</p>
      </div>
      <button 
        @click="openModal()"
        class="flex items-center gap-2 bg-sage-600 text-white px-6 py-3.5 rounded-2xl shadow-lg shadow-sage-200 hover:bg-sage-700 hover:-translate-y-0.5 transition-all font-bold"
      >
        <Plus :size="20" /> 新建公告
      </button>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>

    <div v-else class="grid grid-cols-1 gap-6">
      <div 
        v-for="item in announcements" 
        :key="item.id" 
        :class="['glass-panel p-6 rounded-[2rem] border transition-all hover:shadow-lg group', item.priority === '置顶' ? 'border-sage-200 bg-sage-50/30' : 'border-white/60']"
      >
        <div class="flex justify-between items-start">
          <div class="flex-1 pr-6">
            <div class="flex items-center gap-3 mb-2">
              <span v-if="item.priority === '置顶'" class="bg-red-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full flex items-center gap-1">
                <Pin :size="10" class="fill-current" /> 置顶
              </span>
              <span :class="['text-[10px] font-bold px-2 py-0.5 rounded-full border', item.status === '已发布' ? 'bg-green-100 text-green-700 border-green-200' : 'bg-slate-100 text-slate-500 border-slate-200']">
                {{ item.status }}
              </span>
              <span class="text-xs text-slate-400 font-medium flex items-center gap-1">
                <Calendar :size="12" /> {{ item.date }}
              </span>
            </div>
            <h3 class="text-xl font-bold text-slate-800 mb-2 group-hover:text-sage-700 transition-colors">{{ item.title }}</h3>
            <p class="text-slate-500 text-sm line-clamp-2 leading-relaxed">{{ item.content }}</p>
            
            <div class="mt-4 flex items-center gap-4 text-xs font-medium text-slate-400">
              <span class="flex items-center gap-1 bg-white px-2 py-1 rounded-md border border-slate-100">
                发布者: {{ item.publisher }}
              </span>
              <span class="flex items-center gap-1">
                <Eye :size="14" /> {{ item.views }} 阅读
              </span>
            </div>
          </div>

          <div class="flex flex-col gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
            <button @click="openModal(item)" class="p-2 bg-white border border-slate-200 rounded-xl text-slate-500 hover:text-blue-600 hover:bg-blue-50 transition-colors" title="编辑">
              <Edit2 :size="18" />
            </button>
            <button 
              @click="togglePin(item.id)" 
              :class="['p-2 border rounded-xl transition-colors', item.priority === '置顶' ? 'bg-red-50 text-red-500 border-red-100' : 'bg-white text-slate-400 border-slate-200 hover:text-red-500']" 
              :title="item.priority === '置顶' ? '取消置顶' : '置顶'"
            >
              <Pin :size="18" />
            </button>
            <button @click="handleDelete(item.id)" class="p-2 bg-white border border-slate-200 rounded-xl text-slate-500 hover:text-red-600 hover:bg-red-50 transition-colors" title="删除">
              <Trash2 :size="18" />
            </button>
          </div>
        </div>
      </div>
      
      <div v-if="announcements.length === 0" class="text-center py-20 opacity-50">
        <Megaphone :size="48" class="mx-auto mb-4 text-slate-300" />
        <p class="text-slate-500 font-medium">暂无公告信息，点击右上角创建。</p>
      </div>
    </div>

    <!-- Modal -->
    <div v-if="isModalOpen" class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/20 backdrop-blur-sm p-4">
      <div class="bg-white rounded-[2rem] w-full max-w-2xl shadow-2xl border border-white/50 flex flex-col max-h-[90vh]">
        <div class="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 class="text-2xl font-bold text-slate-800 flex items-center gap-2">
            <Edit2 v-if="editingItem" :size="24" class="text-sage-600"/>
            <Plus v-else :size="24" class="text-sage-600"/>
            {{ editingItem ? '编辑公告' : '新建公告' }}
          </h2>
          <button @click="closeModal" class="p-2 hover:bg-slate-100 rounded-full text-slate-400 hover:text-slate-600"><X :size="20"/></button>
        </div>
        
        <div class="p-8 overflow-y-auto space-y-6">
          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">公告标题</label>
            <input 
              type="text" 
              v-model="formData.title"
              class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200 text-slate-700 font-bold text-lg placeholder:font-normal"
              placeholder="请输入醒目的标题..."
            />
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">发布部门</label>
              <select 
                v-model="formData.publisher"
                class="w-full bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200 text-slate-600 font-medium appearance-none"
              >
                <option value="教务处">教务处</option>
                <option value="校团委">校团委</option>
                <option value="后勤部">后勤部</option>
                <option value="保卫处">保卫处</option>
                <option value="学生会">学生会</option>
              </select>
            </div>
            <div class="flex gap-4">
              <div class="flex-1">
                <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">优先级</label>
                <div class="flex bg-slate-50 p-1 rounded-xl border border-slate-100">
                  <button 
                    v-for="p in ['普通', '置顶']"
                    :key="p"
                    @click="formData.priority = p"
                    :class="['flex-1 py-2 rounded-lg text-sm font-bold transition-all', formData.priority === p ? 'bg-white shadow-sm text-sage-700' : 'text-slate-400 hover:text-slate-600']"
                  >
                    {{ p }}
                  </button>
                </div>
              </div>
              <div class="flex-1">
                <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">状态</label>
                <div class="flex bg-slate-50 p-1 rounded-xl border border-slate-100">
                  <button 
                    v-for="s in ['草稿', '已发布']"
                    :key="s"
                    @click="formData.status = s"
                    :class="['flex-1 py-2 rounded-lg text-sm font-bold transition-all', formData.status === s ? 'bg-white shadow-sm text-sage-700' : 'text-slate-400 hover:text-slate-600']"
                  >
                    {{ s }}
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">正文内容</label>
            <textarea 
              v-model="formData.content"
              class="w-full h-48 bg-slate-50 border border-slate-100 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200 text-slate-600 leading-relaxed resize-none"
              placeholder="在这里输入公告的详细内容..."
            ></textarea>
          </div>
        </div>

        <div class="p-6 border-t border-slate-100 flex justify-end gap-3 bg-slate-50/50 rounded-b-[2rem]">
          <button @click="closeModal" class="px-6 py-3 rounded-xl font-bold text-slate-500 hover:bg-slate-100 transition-colors">取消</button>
          <button @click="handleSave" class="px-8 py-3 bg-sage-600 text-white rounded-xl font-bold hover:bg-sage-700 shadow-lg shadow-sage-200 hover:-translate-y-0.5 transition-all flex items-center gap-2">
            <Send :size="18" /> {{ formData.status === '已发布' ? '立即发布' : '保存草稿' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Plus, Trash2, Edit2, Pin, Eye, Calendar, X, Megaphone, Send, Loader2 } from 'lucide-vue-next'
import { dataService } from '../services/dataService'

const announcements = ref([])
const isLoading = ref(true)
const isModalOpen = ref(false)
const editingItem = ref(null)

const formData = ref({
  title: '',
  content: '',
  priority: '普通',
  status: '草稿',
  publisher: '教务处'
})
const priorityToBackend = (priority) => {
  if (priority === '置顶') return 'PINNED'
  if (priority === '普通') return 'NORMAL'
  return priority || 'NORMAL'
}

const statusToBackend = (status) => {
  if (status === '已发布') return 'PUBLISHED'
  if (status === '草稿') return 'DRAFT'
  return status || 'DRAFT'
}

const mapAnnouncement = (a) => ({
  id: a.id,
  title: a.title || '',
  content: a.content || '',
  publisher: a.publisher || '管理员',
  publisherId: a.publisherId,
  priority: a.priority === 'PINNED' ? '置顶' : '普通',
  status: a.status === 'PUBLISHED' ? '已发布' : '草稿',
  views: a.viewCount || 0,
  date: a.publishedAt
      ? new Date(a.publishedAt).toLocaleDateString('zh-CN')
      : a.createdAt
          ? new Date(a.createdAt).toLocaleDateString('zh-CN')
          : '未知'
})

const loadAnnouncements = async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchAnnouncements()
    announcements.value = (data || []).map(mapAnnouncement)
  } catch (e) {
    console.error('加载公告失败:', e)
    announcements.value = []
  } finally {
    isLoading.value = false
  }
}
onMounted(() => {
  loadAnnouncements()
})
const openModal = (item) => {
  if (item) {
    editingItem.value = item
    formData.value = { ...item }
  } else {
    editingItem.value = null
    formData.value = {
      title: '',
      content: '',
      priority: '普通',
      status: '草稿',
      publisher: '教务处',
      views: 0,
      date: new Date().toISOString().split('T')[0]
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
    if (!formData.value.title || !formData.value.content) {
      alert('请填写公告标题和正文内容')
      return
    }

    const payload = {
      title: formData.value.title,
      content: formData.value.content,
      publisher: formData.value.publisher,
      priority: priorityToBackend(formData.value.priority),
      status: statusToBackend(formData.value.status)
    }

    if (editingItem.value) {
      payload.id = editingItem.value.id
      await dataService.saveAnnouncement(payload)
    } else {
      // 新增公告不要自己造 id，让数据库自动生成
      await dataService.saveAnnouncement(payload)
    }

    closeModal()
    await loadAnnouncements()
  } catch (e) {
    console.error('保存公告失败:', e)
    alert('保存失败，请查看 F12 控制台或 Network 报错')
  }
}
const handleDelete = async (id) => {
  if (confirm('确定要删除这条公告吗？')) {
    await dataService.deleteAnnouncement(id)
    announcements.value = announcements.value.filter(a => a.id !== id)
  }
}

const togglePin = async (id) => {
  const item = announcements.value.find(a => a.id === id)
  if (item) {
    // 调用后端置顶 API
    await dataService.toggleAnnouncementPin(id)
    // 更新本地状态（中文显示）
    const newPriority = item.priority === '置顶' ? '普通' : '置顶'
    announcements.value = announcements.value.map(a => 
      a.id === id ? { ...a, priority: newPriority } : a
    )
  }
}
</script>
