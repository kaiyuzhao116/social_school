<template>
  <!-- Toast 消息 -->
  <Teleport to="body">
    <Transition name="toast">
      <div v-if="uiStore.toast.show" :class="toastClass" class="fixed top-20 left-1/2 -translate-x-1/2 z-[9999] px-6 py-3 rounded-xl shadow-lg flex items-center gap-3 max-w-md">
        <component :is="toastIcon" class="w-5 h-5 shrink-0" />
        <span class="text-sm font-medium">{{ uiStore.toast.message }}</span>
        <button @click="uiStore.hideToast()" class="ml-2 p-1 hover:bg-white/20 rounded-full transition-colors">
          <X class="w-4 h-4" />
        </button>
      </div>
    </Transition>
  </Teleport>

  <!-- 全局加载 -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="uiStore.globalLoading" class="fixed inset-0 z-[9998] bg-black/50 backdrop-blur-sm flex items-center justify-center">
        <div class="bg-white dark:bg-gray-800 rounded-2xl px-8 py-6 shadow-2xl flex flex-col items-center gap-4">
          <div class="w-12 h-12 border-4 border-brand-purple border-t-transparent rounded-full animate-spin"></div>
          <span class="text-gray-600 dark:text-gray-300 text-sm font-medium">{{ uiStore.loadingText }}</span>
        </div>
      </div>
    </Transition>
  </Teleport>

  <!-- 确认对话框 -->
  <Teleport to="body">
    <Transition name="fade">
      <div v-if="uiStore.confirmDialog.show" class="fixed inset-0 z-[9997] bg-black/50 backdrop-blur-sm flex items-center justify-center p-4" @click="uiStore.confirmDialog.onCancel?.()">
        <Transition name="scale">
          <div v-if="uiStore.confirmDialog.show" class="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl max-w-sm w-full overflow-hidden" @click.stop>
            <div class="p-6">
              <div class="flex items-start gap-4">
                <div :class="confirmIconBg" class="w-10 h-10 rounded-full flex items-center justify-center shrink-0">
                  <AlertTriangle v-if="uiStore.confirmDialog.type === 'danger'" class="w-5 h-5 text-red-500" />
                  <AlertCircle v-else-if="uiStore.confirmDialog.type === 'warning'" class="w-5 h-5 text-amber-500" />
                  <Info v-else class="w-5 h-5 text-blue-500" />
                </div>
                <div class="flex-1">
                  <h3 class="text-lg font-bold text-gray-900 dark:text-white mb-2">{{ uiStore.confirmDialog.title }}</h3>
                  <p class="text-gray-600 dark:text-gray-400 text-sm">{{ uiStore.confirmDialog.message }}</p>
                </div>
              </div>
            </div>
            <div class="px-6 py-4 bg-gray-50 dark:bg-gray-700/50 flex gap-3 justify-end">
              <button @click="uiStore.confirmDialog.onCancel?.()" class="px-4 py-2 text-gray-600 dark:text-gray-300 font-medium hover:bg-gray-200 dark:hover:bg-gray-600 rounded-lg transition-colors">
                {{ uiStore.confirmDialog.cancelText }}
              </button>
              <button @click="uiStore.confirmDialog.onConfirm?.()" :class="confirmBtnClass" class="px-4 py-2 font-medium rounded-lg transition-colors">
                {{ uiStore.confirmDialog.confirmText }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>

  <!-- 离线提示 -->
  <Teleport to="body">
    <Transition name="slide-up">
      <div v-if="!uiStore.isOnline" class="fixed bottom-4 left-1/2 -translate-x-1/2 z-[9996] bg-red-500 text-white px-6 py-3 rounded-xl shadow-lg flex items-center gap-3">
        <WifiOff class="w-5 h-5" />
        <span class="text-sm font-medium">网络已断开，请检查网络连接</span>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useUIStore } from '../stores'
import { X, CheckCircle, XCircle, AlertCircle, AlertTriangle, Info, WifiOff } from 'lucide-vue-next'

const uiStore = useUIStore()

const toastClass = computed(() => {
  const base = 'text-white'
  switch (uiStore.toast.type) {
    case 'success':
      return `${base} bg-green-500`
    case 'error':
      return `${base} bg-red-500`
    case 'warning':
      return `${base} bg-amber-500`
    default:
      return `${base} bg-blue-500`
  }
})

const toastIcon = computed(() => {
  switch (uiStore.toast.type) {
    case 'success':
      return CheckCircle
    case 'error':
      return XCircle
    case 'warning':
      return AlertCircle
    default:
      return Info
  }
})

const confirmIconBg = computed(() => {
  switch (uiStore.confirmDialog.type) {
    case 'danger':
      return 'bg-red-100 dark:bg-red-900/30'
    case 'warning':
      return 'bg-amber-100 dark:bg-amber-900/30'
    default:
      return 'bg-blue-100 dark:bg-blue-900/30'
  }
})

const confirmBtnClass = computed(() => {
  switch (uiStore.confirmDialog.type) {
    case 'danger':
      return 'bg-red-500 text-white hover:bg-red-600'
    case 'warning':
      return 'bg-amber-500 text-white hover:bg-amber-600'
    default:
      return 'bg-brand-purple text-white hover:bg-indigo-600'
  }
})
</script>

<style scoped>
/* Toast 动画 */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}
.toast-enter-from {
  opacity: 0;
  transform: translate(-50%, -20px);
}
.toast-leave-to {
  opacity: 0;
  transform: translate(-50%, -20px);
}

/* Fade 动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Scale 动画 */
.scale-enter-active,
.scale-leave-active {
  transition: all 0.2s ease;
}
.scale-enter-from,
.scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* Slide Up 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translate(-50%, 20px);
}
</style>
