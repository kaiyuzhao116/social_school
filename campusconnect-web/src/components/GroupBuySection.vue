<template>
  <div class="bg-white rounded-3xl shadow-sm border border-gray-100 p-6 mt-6 mb-6">
    <div class="flex items-center justify-between mb-5">
      <div>
        <h2 class="text-xl font-bold text-gray-900">学生拼团</h2>
        <p class="text-sm text-gray-500 mt-1">
          奶茶、打印、外卖、资料，一起拼更方便
        </p>
      </div>

      <button
          @click="openCreateModal"
          class="px-4 py-2 rounded-xl bg-indigo-500 text-white text-sm font-bold hover:bg-indigo-600 transition"
      >
        发起拼团
      </button>
    </div>

    <div v-if="groupBuys.length === 0" class="text-center text-gray-400 py-8">
      暂无拼团
    </div>

    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div
          v-for="item in groupBuys"
          :key="item.id"
          class="rounded-2xl border border-gray-100 bg-gray-50 p-4 hover:shadow-md transition"
      >
        <div class="flex gap-4">
          <img
              :src="item.coverImage"
              class="w-24 h-24 rounded-xl object-cover"
              alt=""
          />

          <div class="flex-1 min-w-0">
            <div class="flex items-center justify-between gap-2">
              <h3 class="font-bold text-gray-900 truncate">
                {{ item.title }}
              </h3>

              <span
                  :class="[
                  'text-xs px-2 py-1 rounded-full font-bold',
                  item.status === 'SUCCESS'
                    ? 'bg-green-100 text-green-600'
                    : item.status === 'GROUPING'
                      ? 'bg-emerald-100 text-emerald-600'
                      : item.status === 'CANCELLED'
                        ? 'bg-orange-100 text-orange-600'
                        : 'bg-gray-100 text-gray-500'
                ]"
              >
                {{ statusText(item.status) }}
              </span>
            </div>

            <p class="text-sm text-gray-500 line-clamp-2 mt-1">
              {{ item.description }}
            </p>

            <div class="mt-3 text-xs text-gray-500 space-y-1">
              <div>分类：{{ item.category }}</div>
              <div>地点：{{ item.location }}</div>
              <div>人数：{{ item.currentCount }} / {{ item.targetCount }}</div>
            </div>

            <div class="mt-3 flex items-center justify-between">
              <span class="text-xs text-gray-400">
                截止：{{ formatTime(item.deadline) }}
              </span>

              <!-- 发起人：取消拼团 -->
              <button
                  v-if="item.status === 'GROUPING' && isCreator(item.id)"
                  @click="handleCancel(item.id)"
                  class="px-3 py-1.5 rounded-lg bg-orange-500 text-white text-xs font-bold hover:bg-orange-600 transition"
              >
                取消拼团
              </button>

              <!-- 普通用户未参加：参加拼团 -->
              <button
                  v-else-if="item.status === 'GROUPING' && !hasJoined(item.id)"
                  @click="handleJoin(item.id)"
                  class="px-3 py-1.5 rounded-lg bg-indigo-500 text-white text-xs font-bold hover:bg-indigo-600 transition"
              >
                参加拼团
              </button>

              <!-- 普通用户已参加：退出拼团 -->
              <button
                  v-else-if="item.status === 'GROUPING' && hasJoined(item.id)"
                  @click="handleQuit(item.id)"
                  class="px-3 py-1.5 rounded-lg bg-red-500 text-white text-xs font-bold hover:bg-red-600 transition"
              >
                退出拼团
              </button>

              <!-- 已成团 / 已取消 / 已过期 -->
              <button
                  v-else
                  disabled
                  class="px-3 py-1.5 rounded-lg bg-gray-300 text-white text-xs font-bold cursor-not-allowed"
              >
                {{ item.status === 'SUCCESS' ? '已成团' : statusText(item.status) }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 发起拼团弹窗 -->
    <div
        v-if="isModalOpen"
        class="fixed inset-0 z-[200] flex items-center justify-center bg-black/30 backdrop-blur-sm p-4"
    >
      <div class="bg-white rounded-3xl w-full max-w-xl shadow-2xl overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex items-center justify-between">
          <h3 class="text-xl font-bold text-gray-900">发起拼团</h3>

          <button
              @click="closeModal"
              class="text-gray-400 hover:text-gray-600 text-xl"
          >
            ×
          </button>
        </div>

        <div class="p-6 space-y-4">
          <div>
            <label class="text-xs font-bold text-gray-500">拼团标题</label>
            <input
                v-model="form.title"
                class="mt-2 w-full bg-gray-50 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-200"
                placeholder="例如：瑞幸咖啡拼团"
            />
          </div>

          <div>
            <label class="text-xs font-bold text-gray-500">拼团说明</label>
            <textarea
                v-model="form.description"
                class="mt-2 w-full h-24 bg-gray-50 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-200 resize-none"
                placeholder="简单说明拼团内容、价格、集合方式等"
            ></textarea>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-xs font-bold text-gray-500">分类</label>
              <select
                  v-model="form.category"
                  class="mt-2 w-full bg-gray-50 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-200"
              >
                <option>饮品拼单</option>
                <option>外卖拼单</option>
                <option>学习资料</option>
                <option>生活服务</option>
              </select>
            </div>

            <div>
              <label class="text-xs font-bold text-gray-500">目标人数</label>
              <input
                  v-model.number="form.targetCount"
                  type="number"
                  min="2"
                  class="mt-2 w-full bg-gray-50 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-200"
              />
            </div>
          </div>

          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="text-xs font-bold text-gray-500">地点</label>
              <input
                  v-model="form.location"
                  class="mt-2 w-full bg-gray-50 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-200"
                  placeholder="例如：三食堂门口"
              />
            </div>

            <div>
              <label class="text-xs font-bold text-gray-500">联系方式</label>
              <input
                  v-model="form.contactInfo"
                  class="mt-2 w-full bg-gray-50 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-200"
                  placeholder="例如：QQ / 微信"
              />
            </div>
          </div>

          <div>
            <label class="text-xs font-bold text-gray-500">截止时间</label>
            <input
                v-model="form.deadline"
                class="mt-2 w-full bg-gray-50 rounded-xl px-4 py-3 outline-none focus:ring-2 focus:ring-indigo-200"
                placeholder="YYYY-MM-DD HH:MM"
            />
          </div>
        </div>

        <div class="p-6 bg-gray-50 flex justify-end gap-3">
          <button
              @click="closeModal"
              class="px-5 py-3 rounded-xl text-gray-500 font-bold hover:bg-gray-100"
          >
            取消
          </button>

          <button
              @click="handleCreate"
              :disabled="isSaving"
              class="px-6 py-3 rounded-xl bg-indigo-500 text-white font-bold hover:bg-indigo-600 disabled:opacity-60"
          >
            {{ isSaving ? '发布中...' : '立即发布' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'

const groupBuys = ref([])
const joinedIds = ref([])
const createdIds = ref([])

const isModalOpen = ref(false)
const isSaving = ref(false)

const form = ref({
  title: '',
  description: '',
  category: '饮品拼单',
  targetCount: 3,
  location: '',
  contactInfo: '',
  deadline: ''
})

const statusText = (status) => {
  if (status === 'GROUPING') return '拼团中'
  if (status === 'SUCCESS') return '已成团'
  if (status === 'CANCELLED') return '已取消'
  if (status === 'EXPIRED') return '已过期'
  return status || '拼团中'
}

const formatTime = (value) => {
  if (!value) return '暂无'
  return String(value).replace('T', ' ').slice(0, 16)
}

const formatDateTime = (value) => {
  if (!value) return null

  if (value.includes('T')) {
    if (value.length === 16) return value + ':00'
    return value
  }

  if (value.length === 16) {
    return value.replace(' ', 'T') + ':00'
  }

  return value.replace(' ', 'T')
}

const hasJoined = (id) => {
  return joinedIds.value.includes(id)
}

const isCreator = (id) => {
  return createdIds.value.includes(id)
}

const loadGroupBuys = async () => {
  try {
    const res = await request.get('/group-buys')
    groupBuys.value = res.data || []
  } catch (e) {
    console.error('加载学生拼团失败:', e)
    groupBuys.value = []
  }
}

const loadMyJoined = async () => {
  try {
    const res = await request.get('/group-buys/my-joined')
    joinedIds.value = res.data || []
  } catch (e) {
    console.error('加载我的拼团状态失败:', e)
    joinedIds.value = []
  }
}

const loadMyCreated = async () => {
  try {
    const res = await request.get('/group-buys/my-created')
    createdIds.value = res.data || []
  } catch (e) {
    console.error('加载我发起的拼团失败:', e)
    createdIds.value = []
  }
}

const refreshGroupBuys = async () => {
  await loadGroupBuys()
  await loadMyJoined()
  await loadMyCreated()
}

const openCreateModal = () => {
  form.value = {
    title: '',
    description: '',
    category: '饮品拼单',
    targetCount: 3,
    location: '',
    contactInfo: '',
    deadline: ''
  }
  isModalOpen.value = true
}

const closeModal = () => {
  isModalOpen.value = false
}

const handleCreate = async () => {
  if (!form.value.title || !form.value.description || !form.value.location || !form.value.deadline) {
    alert('请填写标题、说明、地点和截止时间')
    return
  }

  isSaving.value = true

  try {
    const payload = {
      title: form.value.title,
      description: form.value.description,
      category: form.value.category,
      targetCount: form.value.targetCount,
      location: form.value.location,
      contactInfo: form.value.contactInfo,
      deadline: formatDateTime(form.value.deadline)
    }

    await request.post('/group-buys', payload)

    alert('拼团发布成功')
    closeModal()
    await refreshGroupBuys()
  } catch (e) {
    console.error('发起拼团失败:', e)
    alert('发起失败，请看 F12 控制台或 Network 报错')
  } finally {
    isSaving.value = false
  }
}

const handleJoin = async (id) => {
  try {
    await request.post(`/group-buys/${id}/join`)
    alert('参加成功')
    await refreshGroupBuys()
  } catch (e) {
    console.error('参加拼团失败:', e)
    alert('参加失败，可能已经参加过，或者拼团人数已满')
  }
}

const handleQuit = async (id) => {
  try {
    await request.post(`/group-buys/${id}/quit`)
    alert('已退出拼团')
    await refreshGroupBuys()
  } catch (e) {
    console.error('退出拼团失败:', e)
    alert('退出失败，发起人不能退出，只能取消拼团')
  }
}

const handleCancel = async (id) => {
  if (!confirm('确定要取消这个拼团吗？')) {
    return
  }

  try {
    await request.post(`/group-buys/${id}/cancel`)
    alert('拼团已取消')
    await refreshGroupBuys()
  } catch (e) {
    console.error('取消拼团失败:', e)
    alert('取消失败，只有发起人可以取消拼团')
  }
}

onMounted(() => {
  refreshGroupBuys()
})
</script>