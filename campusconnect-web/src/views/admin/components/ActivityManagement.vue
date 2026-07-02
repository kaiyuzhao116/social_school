<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <div class="flex justify-between items-center mb-8">
      <div>
        <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">活动发布</h1>
        <p class="text-slate-500 mt-2 font-medium">创建校园活动，管理报名状态。</p>
      </div>

      <button
          @click="openModal()"
          class="flex items-center gap-2 bg-sage-600 text-white px-6 py-3.5 rounded-2xl shadow-lg shadow-sage-200 hover:bg-sage-700 hover:-translate-y-0.5 transition-all font-bold"
      >
        <Plus :size="20" /> 发布活动
      </button>
    </div>

    <!-- Loading -->
    <div v-if="isLoading" class="flex h-full items-center justify-center">
      <Loader2 class="w-8 h-8 animate-spin text-sage-600" />
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
          v-for="act in activities"
          :key="act.id"
          class="glass-panel p-0 rounded-[2rem] overflow-hidden border border-white/60 group hover:shadow-xl transition-all"
      >
        <div class="h-48 relative overflow-hidden">
          <img
              :src="act.coverImage"
              class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              alt=""
          />

          <div class="absolute top-4 right-4 bg-white/90 backdrop-blur px-3 py-1 rounded-full text-xs font-bold text-slate-700 border border-white/50 shadow-sm">
            {{ act.status }}
          </div>
        </div>

        <div class="p-6">
          <h3 class="text-xl font-bold text-slate-800 mb-4 line-clamp-1">
            {{ act.title }}
          </h3>

          <div class="space-y-3 mb-6">
            <div class="flex items-center gap-3 text-sm text-slate-500">
              <Calendar :size="16" class="text-sage-500" />
              <span>{{ act.startTime }}</span>
            </div>

            <div class="flex items-center gap-3 text-sm text-slate-500">
              <MapPin :size="16" class="text-sage-500" />
              <span>{{ act.location }}</span>
            </div>

            <div class="flex items-center gap-3 text-sm text-slate-500">
              <Users :size="16" class="text-sage-500" />
              <span>{{ act.participantCount }} / {{ act.maxParticipants }} 人已报名</span>            </div>
          </div>

          <div class="flex items-center justify-between border-t border-slate-100 pt-4">
            <span class="text-xs font-bold text-slate-400 bg-slate-50 px-2 py-1 rounded-md">
              {{ act.organizer }}
            </span>

            <div class="flex gap-2">
              <button
                  @click="openModal(act)"
                  class="p-2 hover:bg-slate-100 rounded-xl text-slate-400 hover:text-blue-600 transition-colors"
              >
                <Edit2 :size="18" />
              </button>

              <button
                  @click="handleDelete(act.id)"
                  class="p-2 hover:bg-red-50 rounded-xl text-slate-400 hover:text-red-600 transition-colors"
              >
                <Trash2 :size="18" />
              </button>
            </div>
          </div>
        </div>
      </div>

      <div
          v-if="activities.length === 0"
          class="col-span-full text-center text-slate-400 font-medium py-20"
      >
        暂无活动数据
      </div>
    </div>

    <!-- Modal -->
    <div
        v-if="isModalOpen"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-slate-900/20 backdrop-blur-sm p-4"
    >
      <div class="bg-white rounded-[2rem] w-full max-w-lg shadow-2xl border border-white/50 flex flex-col max-h-[90vh]">
        <div class="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 class="text-2xl font-bold text-slate-800">
            {{ form.id ? '编辑活动' : '发布新活动' }}
          </h2>

          <button
              @click="closeModal"
              class="p-2 hover:bg-slate-100 rounded-full text-slate-400 hover:text-slate-600"
          >
            <X :size="20" />
          </button>
        </div>

        <div class="p-8 space-y-5 overflow-y-auto">
          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
              活动主题
            </label>
            <input
                v-model="form.title"
                class="w-full bg-slate-50 border-none rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200"
            />
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
                时间
              </label>
              <input
                  v-model="form.startTime"
                  placeholder="YYYY-MM-DD HH:MM"
                  class="w-full bg-slate-50 border-none rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200"
              />
            </div>

            <div>
              <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
                状态
              </label>
              <select
                  v-model="form.status"
                  class="w-full bg-slate-50 border-none rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200"
              >
                <option>报名中</option>
                <option>进行中</option>
                <option>已结束</option>
              </select>
            </div>
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
              地点
            </label>
            <input
                v-model="form.location"
                class="w-full bg-slate-50 border-none rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200"
            />
          </div>

          <div>
            <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
              主办方
            </label>
            <input
                v-model="form.organizer"
                class="w-full bg-slate-50 border-none rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200"
            />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
                结束时间
              </label>
              <input
                  v-model="form.endTime"
                  placeholder="YYYY-MM-DD HH:MM"
                  class="w-full bg-slate-50 border-none rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200"
              />
            </div>

            <div>
              <label class="block text-xs font-bold text-slate-400 uppercase tracking-wider mb-2">
                最大人数
              </label>
              <input
                  v-model.number="form.maxParticipants"
                  type="number"
                  class="w-full bg-slate-50 border-none rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-sage-200"
              />
            </div>
          </div>
        </div>

        <div class="p-6 border-t border-slate-100 flex justify-end gap-3 bg-slate-50/50 rounded-b-[2rem]">
          <button
              @click="closeModal"
              class="px-6 py-3 rounded-xl font-bold text-slate-500 hover:bg-slate-100 transition-colors"
          >
            取消
          </button>

          <button
              @click="handleSave"
              :disabled="isSaving"
              class="px-8 py-3 bg-sage-600 text-white rounded-xl font-bold hover:bg-sage-700 shadow-lg shadow-sage-200 flex items-center gap-2 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            <Loader2 v-if="isSaving" :size="18" class="animate-spin" />
            <Save v-else :size="18" />
            {{ isSaving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Calendar,
  MapPin,
  Users,
  Plus,
  Trash2,
  Edit2,
  Loader2,
  Save,
  X
} from 'lucide-vue-next'
import { dataService } from '../services/dataService'

const activities = ref([])
const isLoading = ref(true)
const isSaving = ref(false)
const isModalOpen = ref(false)
const form = ref({})

const statusToChinese = (status) => {
  if (status === 'REGISTERING') return '报名中'
  if (status === 'ONGOING') return '进行中'
  if (status === 'ENDED') return '已结束'
  return status || '报名中'
}

const statusToBackend = (status) => {
  if (status === '报名中') return 'REGISTERING'
  if (status === '进行中') return 'ONGOING'
  if (status === '已结束') return 'ENDED'
  return status || 'REGISTERING'
}

const formatDateTime = (value) => {
  if (!value) return null

  // 已经是 2026-09-12T19:00:00 这种格式就直接返回
  if (value.includes('T')) {
    if (value.length === 16) return value + ':00'
    return value
  }

  // 把 2026-09-12 19:00 转成 2026-09-12T19:00:00
  if (value.length === 16) {
    return value.replace(' ', 'T') + ':00'
  }

  return value.replace(' ', 'T')
}

const formatShowTime = (value) => {
  if (!value) return ''
  return String(value).replace('T', ' ').slice(0, 16)
}

const mapActivity = (a) => {
  return {
    id: a.id,
    title: a.title || '',
    description: a.description || '',
    coverImage: a.coverImage || 'https://picsum.photos/400/300?random=' + a.id,
    location: a.location || '待定',
    startTime: formatShowTime(a.startTime),
    endTime: formatShowTime(a.endTime),
    organizer: a.organizer || '管理员',
    organizerId: a.organizerId,
    status: statusToChinese(a.status),
    maxParticipants: a.maxParticipants || 0,
    participantCount: a.participantCount || 0,
    createdAt: a.createdAt
  }
}

const loadActivities = async () => {
  isLoading.value = true
  try {
    const data = await dataService.fetchActivities()
    activities.value = (data || []).map(mapActivity)
  } catch (e) {
    console.error('加载活动失败:', e)
    activities.value = []
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadActivities()
})

const openModal = (act) => {
  if (act) {
    form.value = { ...act }
  } else {
    form.value = {
      title: '',
      description: '',
      location: '',
      organizer: '学生会',
      status: '报名中',
      startTime: '',
      endTime: '',
      coverImage: `https://picsum.photos/400/300?random=${Date.now()}`,
      maxParticipants: 100
    }
  }

  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
  form.value = {}
}

const handleSave = async () => {
  if (!form.value.title || !form.value.startTime || !form.value.location) {
    alert('请填写活动主题、时间和地点')
    return
  }

  isSaving.value = true

  try {
    const payload = {
      title: form.value.title,
      description: form.value.description || '',
      coverImage: form.value.coverImage || `https://picsum.photos/400/300?random=${Date.now()}`,
      location: form.value.location,
      organizer: form.value.organizer || '学生会',
      startTime: formatDateTime(form.value.startTime),
      endTime: form.value.endTime
          ? formatDateTime(form.value.endTime)
          : formatDateTime(form.value.startTime),
      status: statusToBackend(form.value.status),
      maxParticipants: form.value.maxParticipants || 0
    }

    if (form.value.id) {
      payload.id = form.value.id
      await dataService.saveActivity(payload)
    } else {
      // 新增活动不要自己造 id，数据库会自动生成
      await dataService.saveActivity(payload)
    }

    closeModal()
    await loadActivities()
  } catch (e) {
    console.error('保存活动失败:', e)
    alert('保存失败，请打开 F12 看 Console 或 Network 报错')
  } finally {
    isSaving.value = false
  }
}

const handleDelete = async (id) => {
  if (confirm('确认取消并删除此活动吗？')) {
    try {
      await dataService.deleteActivity(id)
      await loadActivities()
    } catch (e) {
      console.error('删除活动失败:', e)
      alert('删除失败，请查看控制台报错')
    }
  }
}
</script>