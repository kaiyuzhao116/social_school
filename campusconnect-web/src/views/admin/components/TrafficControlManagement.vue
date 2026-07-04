<template>
  <div class="h-full overflow-y-auto pr-4 pb-32 no-scrollbar">
    <header class="mb-8">
      <h1 class="text-4xl font-extrabold text-slate-800 tracking-tight">
        系统流控
      </h1>
      <p class="text-slate-500 mt-2 font-medium">
        基于 Redis 发布订阅、BeanPostProcessor 和反射实现动态限流配置。
      </p>
    </header>

    <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 mb-8">
      <!-- 限流开关 -->
      <div class="bg-white/80 rounded-[2rem] border border-slate-100 shadow-sm p-6">
        <div class="flex items-center justify-between mb-5">
          <div>
            <h2 class="font-bold text-slate-900">限流开关</h2>
            <p class="text-xs text-slate-500 mt-1">控制是否启用接口级限流</p>
          </div>
          <div class="w-12 h-12 rounded-2xl bg-green-100 flex items-center justify-center text-xl">
            🚦
          </div>
        </div>

        <div class="text-3xl font-black text-slate-900 mb-6">
          {{ trafficEnabled === '1' ? '已开启' : '已关闭' }}
        </div>

        <button
            @click="toggleTraffic"
            class="w-full h-11 rounded-2xl text-white font-bold transition-all"
            :class="trafficEnabled === '1'
            ? 'bg-slate-600 hover:bg-slate-700'
            : 'bg-green-600 hover:bg-green-700'"
        >
          {{ trafficEnabled === '1' ? '关闭限流' : '开启限流' }}
        </button>
      </div>

      <!-- QPS 阈值 -->
      <div class="bg-white/80 rounded-[2rem] border border-slate-100 shadow-sm p-6">
        <div class="flex items-center justify-between mb-5">
          <div>
            <h2 class="font-bold text-slate-900">QPS 阈值</h2>
            <p class="text-xs text-slate-500 mt-1">每个接口每秒最大访问次数</p>
          </div>
          <div class="w-12 h-12 rounded-2xl bg-blue-100 flex items-center justify-center text-xl">
            ⚡
          </div>
        </div>

        <input
            v-model="trafficQps"
            type="number"
            min="1"
            class="w-full h-12 rounded-2xl bg-slate-50 border border-slate-100 px-4 text-lg font-bold outline-none focus:ring-2 focus:ring-green-200"
            placeholder="例如 30"
        />

        <button
            @click="updateQps"
            class="w-full h-11 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-bold mt-6 transition-all"
        >
          更新 QPS
        </button>
      </div>

      <!-- 全站降级 -->
      <div class="bg-white/80 rounded-[2rem] border border-slate-100 shadow-sm p-6">
        <div class="flex items-center justify-between mb-5">
          <div>
            <h2 class="font-bold text-slate-900">全站降级</h2>
            <p class="text-xs text-slate-500 mt-1">紧急情况下拒绝普通接口请求</p>
          </div>
          <div class="w-12 h-12 rounded-2xl bg-orange-100 flex items-center justify-center text-xl">
            ⚠️
          </div>
        </div>

        <div class="text-3xl font-black text-slate-900 mb-6">
          {{ trafficDegrade === '1' ? '已降级' : '正常运行' }}
        </div>

        <button
            @click="toggleDegrade"
            class="w-full h-11 rounded-2xl text-white font-bold transition-all"
            :class="trafficDegrade === '1'
            ? 'bg-green-600 hover:bg-green-700'
            : 'bg-orange-500 hover:bg-orange-600'"
        >
          {{ trafficDegrade === '1' ? '恢复访问' : '开启降级' }}
        </button>
      </div>
    </div>

    <div class="bg-white/80 rounded-[2rem] border border-slate-100 shadow-sm p-6">
      <h2 class="font-bold text-slate-900 mb-4">配置说明</h2>

      <div class="space-y-3 text-sm text-slate-600">
        <div class="flex justify-between border-b border-slate-100 pb-3">
          <span class="font-mono text-slate-800">traffic.enabled</span>
          <span>是否开启接口限流，1 开启，0 关闭</span>
        </div>

        <div class="flex justify-between border-b border-slate-100 pb-3">
          <span class="font-mono text-slate-800">traffic.qps</span>
          <span>每个接口每秒允许的最大请求数</span>
        </div>

        <div class="flex justify-between">
          <span class="font-mono text-slate-800">traffic.degrade</span>
          <span>是否开启全站降级，1 开启，0 关闭</span>
        </div>
      </div>
    </div>

    <div
        v-if="message"
        class="fixed top-24 left-1/2 -translate-x-1/2 bg-slate-900 text-white px-5 py-3 rounded-2xl shadow-xl text-sm z-[9999]"
    >
      {{ message }}
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../../api/request'

const trafficEnabled = ref('1')
const trafficQps = ref('30')
const trafficDegrade = ref('0')
const message = ref('')

function showMessage(text) {
  message.value = text
  setTimeout(() => {
    message.value = ''
  }, 1800)
}

function getResultData(res) {
  if (res && typeof res === 'object' && 'data' in res) {
    return res.data
  }
  return res
}

async function getConfig(key, targetRef, defaultValue) {
  try {
    const res = await request.get('/admin/dynamic-config/get', {
      params: { key }
    })

    const value = getResultData(res)
    targetRef.value = value == null || value === '' ? defaultValue : String(value)
  } catch (e) {
    targetRef.value = defaultValue
  }
}

async function updateConfig(key, value) {
  await request.post('/admin/dynamic-config/update', null, {
    params: {
      key,
      value
    }
  })
}

async function toggleTraffic() {
  const next = trafficEnabled.value === '1' ? '0' : '1'
  await updateConfig('traffic.enabled', next)
  trafficEnabled.value = next
  showMessage(next === '1' ? '限流已开启' : '限流已关闭')
}

async function updateQps() {
  const value = trafficQps.value || '30'
  await updateConfig('traffic.qps', value)
  trafficQps.value = value
  showMessage(`QPS 已更新为 ${value}`)
}

async function toggleDegrade() {
  const next = trafficDegrade.value === '1' ? '0' : '1'
  await updateConfig('traffic.degrade', next)
  trafficDegrade.value = next
  showMessage(next === '1' ? '全站降级已开启' : '系统访问已恢复')
}

onMounted(() => {
  getConfig('traffic.enabled', trafficEnabled, '1')
  getConfig('traffic.qps', trafficQps, '30')
  getConfig('traffic.degrade', trafficDegrade, '0')
})
</script>