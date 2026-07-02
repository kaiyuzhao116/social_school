<template>
  <div class="fixed right-6 bottom-6 z-[300] space-y-3 w-[360px] pointer-events-none">
    <transition-group name="live-card" tag="div" class="space-y-3">
      <div
          v-for="card in liveCards"
          :key="card.id"
          class="pointer-events-auto bg-white rounded-2xl shadow-2xl border border-gray-100 overflow-hidden"
      >
        <div
            :class="[
            'h-1',
            card.type === 'GROUP_BUY_SUCCESS'
              ? 'bg-green-500'
              : card.type === 'GROUP_BUY_JOINED'
                ? 'bg-indigo-500'
                : card.type === 'GROUP_BUY_EXPIRED'
                  ? 'bg-orange-500'
                  : 'bg-blue-500'
          ]"
        ></div>

        <div class="p-4">
          <div class="flex items-start gap-3">
            <div
                :class="[
                'w-10 h-10 rounded-full flex items-center justify-center text-xl shrink-0',
                card.type === 'GROUP_BUY_SUCCESS'
                  ? 'bg-green-100'
                  : card.type === 'GROUP_BUY_JOINED'
                    ? 'bg-indigo-100'
                    : card.type === 'GROUP_BUY_EXPIRED'
                      ? 'bg-orange-100'
                      : 'bg-blue-100'
              ]"
            >
              {{ iconText(card.type) }}
            </div>

            <div class="flex-1 min-w-0">
              <div class="flex items-center justify-between gap-2">
                <h3 class="font-bold text-gray-900 truncate">
                  {{ titleText(card) }}
                </h3>

                <button
                    @click="removeCard(card.id)"
                    class="text-gray-400 hover:text-gray-600 text-lg leading-none"
                >
                  ×
                </button>
              </div>

              <p class="text-sm text-gray-600 mt-1 line-clamp-2">
                {{ card.message || '拼团状态发生变化' }}
              </p>

              <div
                  v-if="card.currentCount !== undefined && card.targetCount !== undefined"
                  class="mt-3"
              >
                <div class="flex items-center justify-between text-xs text-gray-500 mb-1">
                  <span>拼团进度</span>
                  <span>{{ card.currentCount }} / {{ card.targetCount }}</span>
                </div>

                <div class="h-2 bg-gray-100 rounded-full overflow-hidden">
                  <div
                      class="h-full bg-indigo-500 rounded-full transition-all"
                      :style="{ width: progressWidth(card) }"
                  ></div>
                </div>
              </div>

              <div class="mt-3 flex items-center justify-between">
                <span class="text-xs text-gray-400">
                  刚刚
                </span>

                <button
                    @click="handleView(card)"
                    class="text-xs text-indigo-600 font-bold hover:underline"
                >
                  查看拼团
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const liveCards = ref([])
let ws = null

const iconText = (type) => {
  if (type === 'GROUP_BUY_SUCCESS') return '🎉'
  if (type === 'GROUP_BUY_JOINED') return '👥'
  if (type === 'GROUP_BUY_EXPIRED') return '⏰'
  return '📢'
}

const titleText = (card) => {
  if (card.type === 'GROUP_BUY_SUCCESS') return '拼团已成团'
  if (card.type === 'GROUP_BUY_JOINED') return '有人加入拼团'
  if (card.type === 'GROUP_BUY_EXPIRED') return '拼团已过期'
  if (card.type === 'CONNECTED') return '实时推送已连接'
  return card.title || '拼团动态'
}

const progressWidth = (card) => {
  const current = Number(card.currentCount || 0)
  const target = Number(card.targetCount || 1)
  const percent = Math.min(100, Math.round((current / target) * 100))
  return percent + '%'
}

const addCard = (data) => {
  if (!data || data.type === 'CONNECTED') {
    return
  }

  const card = {
    id: Date.now() + Math.random(),
    ...data
  }

  liveCards.value.unshift(card)

  if (liveCards.value.length > 4) {
    liveCards.value.pop()
  }

  setTimeout(() => {
    removeCard(card.id)
  }, 8000)
}

const removeCard = (id) => {
  liveCards.value = liveCards.value.filter(item => item.id !== id)
}

const handleView = () => {
  // 现在先滚动到学生拼团区域，后面如果你有详情页，可以改成跳转详情页
  const title = Array.from(document.querySelectorAll('h2'))
      .find(el => el.innerText.includes('学生拼团'))

  if (title) {
    title.scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    })
  }
}

const connectWebSocket = () => {
  ws = new WebSocket('ws://localhost:8080/api/ws/group-buy/live')

  ws.onopen = () => {
    console.log('拼团实时 WebSocket 已连接')
  }

  ws.onmessage = (event) => {
    console.log('收到拼团实时消息：', event.data)

    try {
      const data = JSON.parse(event.data)
      addCard(data)
    } catch (e) {
      console.error('解析拼团实时消息失败：', e)
    }
  }

  ws.onclose = () => {
    console.log('拼团实时 WebSocket 已关闭')
  }

  ws.onerror = (error) => {
    console.error('拼团实时 WebSocket 出错：', error)
  }
}

onMounted(() => {
  connectWebSocket()
})

onBeforeUnmount(() => {
  if (ws) {
    ws.close()
  }
})
</script>

<style scoped>
.live-card-enter-active,
.live-card-leave-active {
  transition: all 0.35s ease;
}

.live-card-enter-from {
  opacity: 0;
  transform: translateX(80px) translateY(10px);
}

.live-card-leave-to {
  opacity: 0;
  transform: translateX(80px) translateY(10px);
}
</style>