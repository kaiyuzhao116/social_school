<template>
  <div class="space-y-6 animate-in fade-in duration-500">
    <div class="flex justify-between items-end mb-2">
      <div>
        <h2 class="text-2xl font-bold text-gray-900 dark:text-white">
          校园活动
        </h2>
        <p class="text-gray-500 dark:text-gray-400 text-sm mt-1">
          发现精彩校园生活，参与你感兴趣的活动
        </p>
      </div>

      <button class="text-sm text-brand-purple font-medium hover:underline">
        查看日历视图
      </button>
    </div>

    <div v-if="isLoading" class="py-20 text-center text-gray-400">
      活动加载中...
    </div>

    <div v-else-if="events.length === 0" class="py-20 text-center text-gray-400">
      暂无活动
    </div>

    <div v-else class="grid gap-6">
      <div
          v-for="event in events"
          :key="event.id"
          class="bg-white dark:bg-gray-800 rounded-2xl p-4 md:p-0 shadow-sm border border-gray-100 dark:border-gray-700 hover:shadow-md transition-all flex flex-col md:flex-row overflow-hidden group"
      >
        <!-- Image Section -->
        <div
            class="w-full md:w-64 h-48 md:h-auto shrink-0 relative overflow-hidden rounded-xl md:rounded-none"
        >
          <img
              :src="event.image"
              class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
              :alt="event.title"
          />

          <div
              class="absolute top-3 left-3 bg-white/90 dark:bg-gray-900/90 backdrop-blur-sm px-3 py-1 rounded-lg text-center shadow-sm"
          >
            <span class="block text-xs text-gray-500 uppercase tracking-wide">
              {{ event.month }}月
            </span>
            <span class="block text-xl font-bold text-gray-900 dark:text-white">
              {{ event.day }}
            </span>
          </div>
        </div>

        <!-- Content Section -->
        <div
            class="flex-1 p-0 md:p-6 flex flex-col justify-between mt-4 md:mt-0"
        >
          <div>
            <div class="flex justify-between items-start">
              <span
                  :class="[
                  'px-2.5 py-0.5 rounded text-xs font-bold mb-2 inline-block',
                  event.color
                ]"
              >
                {{ event.category }}
              </span>

              <button
                  class="text-gray-400 hover:text-brand-purple transition-colors"
              >
                <Calendar class="w-5 h-5" />
              </button>
            </div>

            <h3
                class="text-xl font-bold text-gray-900 dark:text-white mb-2 group-hover:text-brand-purple transition-colors"
            >
              {{ event.title }}
            </h3>

            <div class="space-y-2 text-sm text-gray-500 dark:text-gray-400">
              <div class="flex items-center gap-2">
                <Clock class="w-4 h-4" />
                <span>{{ event.time }}</span>
              </div>

              <div class="flex items-center gap-2">
                <MapPin class="w-4 h-4" />
                <span>{{ event.location }}</span>
              </div>

              <div class="flex items-center gap-2">
                <Users class="w-4 h-4" />
                <span>{{ event.attendees }} / {{ event.maxAttendees }} 人已报名</span>
              </div>
            </div>
          </div>

          <div
              class="flex items-center justify-between mt-6 pt-4 border-t border-gray-100 dark:border-gray-700"
          >
            <div class="flex items-center gap-2 text-sm">
              <span class="text-gray-400">主办方:</span>
              <span class="font-medium text-gray-900 dark:text-white">
                {{ event.organizer }}
              </span>
            </div>

            <button
                @click="event.registered ? handleUnregister(event) : handleRegister(event)"
                :disabled="
      !event.registered &&
      (
        event.category !== '报名中' ||
        (event.maxAttendees > 0 && event.attendees >= event.maxAttendees)
      )
    "
                class="flex items-center gap-1 text-sm font-bold text-brand-purple hover:text-indigo-600 transition-colors disabled:text-gray-400 disabled:cursor-not-allowed"
            >
              {{
                event.registered
                    ? '取消报名'
                    : event.category !== '报名中'
                        ? event.category
                        : event.maxAttendees > 0 && event.attendees >= event.maxAttendees
                            ? '名额已满'
                            : '立即报名'
              }}
              <ArrowRight class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Calendar, MapPin, Users, Clock, ArrowRight } from 'lucide-vue-next'
import request from '../api/request'

const events = ref([])
const isLoading = ref(false)
const handleRegister = async (event) => {
  if (event.category !== '报名中') {
    alert('当前活动不在报名中')
    return
  }

  if (event.maxAttendees > 0 && event.attendees >= event.maxAttendees) {
    alert('活动名额已满')
    return
  }

  try {
    const res = await request.post(`/events/${event.id}/register`)

    if (res.code && res.code !== 200) {
      alert(res.message || res.msg || '报名失败')
      return
    }

    alert('报名成功')

    await loadEvents()
  } catch (e) {
    console.error('报名失败:', e)

    alert(
        e.response?.data?.message ||
        e.response?.data?.msg ||
        e.message ||
        '报名失败，请检查是否已登录或是否已经报名'
    )
  }
}


const handleUnregister = async (event) => {
  if (!confirm('确认取消报名该活动吗？')) {
    return
  }

  try {
    const res = await request.delete(`/events/${event.id}/register`)

    if (res.code && res.code !== 200) {
      alert(res.message || res.msg || '取消报名失败')
      return
    }

    alert('取消报名成功')

    await loadEvents()
  } catch (e) {
    console.error('取消报名失败:', e)

    alert(
        e.response?.data?.message ||
        e.response?.data?.msg ||
        e.message ||
        '取消报名失败'
    )
  }
}
const statusToCategory = (status) => {
  if (status === 'REGISTERING' || status === '报名中') return '报名中'
  if (status === 'ONGOING' || status === '进行中') return '进行中'
  if (status === 'ENDED' || status === '已结束') return '已结束'
  return '活动'
}

const getColor = (status) => {
  if (status === 'REGISTERING' || status === '报名中') {
    return 'bg-purple-100 text-purple-600'
  }
  if (status === 'ONGOING' || status === '进行中') {
    return 'bg-blue-100 text-blue-600'
  }
  if (status === 'ENDED' || status === '已结束') {
    return 'bg-gray-100 text-gray-600'
  }
  return 'bg-green-100 text-green-600'
}

const formatDate = (value) => {
  if (!value) {
    return {
      date: '',
      month: '--',
      day: '--',
      time: '--'
    }
  }

  const str = String(value).replace('T', ' ')
  const datePart = str.slice(0, 10)
  const timePart = str.slice(11, 16)

  return {
    date: datePart,
    month: datePart.slice(5, 7),
    day: datePart.slice(8, 10),
    time: timePart
  }
}

const mapEvent = (activity) => {
  const dateInfo = formatDate(activity.startTime)

  return {
    id: activity.id,
    title: activity.title || '未命名活动',
    category: statusToCategory(activity.status),
    date: dateInfo.date,
    month: dateInfo.month,
    day: dateInfo.day,
    time: dateInfo.time,
    location: activity.location || '待定',
    organizer: activity.organizer || '管理员',
    image:
        activity.coverImage ||
        activity.cover_image ||
        `https://picsum.photos/seed/event${activity.id}/800/400`,
    attendees: activity.participantCount || 0,
    maxAttendees: activity.maxParticipants || 0,
    color: getColor(activity.status),
    registered: false
  }
}
const loadMyRegisteredIds = async () => {
  try {
    const res = await request.get('/events/my')

    const list =
        res.data?.records ||
        res.data ||
        res.records ||
        []

    return new Set(list.map(item => item.id))
  } catch (e) {
    console.warn('加载我的报名活动失败:', e)
    return new Set()
  }
}

const loadEvents = async () => {
  isLoading.value = true

  try {
    const [activityRes, registeredIds] = await Promise.all([
      request.get('/activities'),
      loadMyRegisteredIds()
    ])

    events.value = (activityRes.data || [])
        .map(mapEvent)
        .map(event => ({
          ...event,
          registered: registeredIds.has(event.id)
        }))
  } catch (e) {
    console.error('加载活动失败:', e)
    events.value = []
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  loadEvents()
})
</script>