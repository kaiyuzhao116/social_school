import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUIStore = defineStore('ui', () => {
  // ========================================
  // 模态框状态
  // ========================================
  const modalData = ref({
    type: null,
    data: null
  })

  const isModalOpen = computed(() => !!modalData.value.type)

  function openModal(type, data = null) {
    modalData.value = { type, data }
  }

  function closeModal() {
    modalData.value = { type: null, data: null }
  }

  // ========================================
  // Toast 消息
  // ========================================
  const toast = ref({
    show: false,
    message: '',
    type: 'info', // 'success' | 'error' | 'warning' | 'info'
    duration: 3000
  })

  let toastTimer = null

  function showToast(message, type = 'info', duration = 3000) {
    if (toastTimer) {
      clearTimeout(toastTimer)
    }
    
    toast.value = {
      show: true,
      message,
      type,
      duration
    }

    if (duration > 0) {
      toastTimer = setTimeout(() => {
        hideToast()
      }, duration)
    }
  }

  function hideToast() {
    toast.value.show = false
    if (toastTimer) {
      clearTimeout(toastTimer)
      toastTimer = null
    }
  }

  // 快捷方法
  function showSuccess(message, duration) {
    showToast(message, 'success', duration)
  }

  function showError(message, duration) {
    showToast(message, 'error', duration)
  }

  function showWarning(message, duration) {
    showToast(message, 'warning', duration)
  }

  function showInfo(message, duration) {
    showToast(message, 'info', duration)
  }

  // ========================================
  // 全局加载状态
  // ========================================
  const globalLoading = ref(false)
  const loadingText = ref('')
  const loadingCount = ref(0)

  function startLoading(text = '加载中...') {
    loadingCount.value++
    globalLoading.value = true
    loadingText.value = text
  }

  function stopLoading() {
    loadingCount.value = Math.max(0, loadingCount.value - 1)
    if (loadingCount.value === 0) {
      globalLoading.value = false
      loadingText.value = ''
    }
  }

  function forceStopLoading() {
    loadingCount.value = 0
    globalLoading.value = false
    loadingText.value = ''
  }

  // ========================================
  // 确认对话框
  // ========================================
  const confirmDialog = ref({
    show: false,
    title: '',
    message: '',
    confirmText: '确定',
    cancelText: '取消',
    type: 'info', // 'info' | 'warning' | 'danger'
    onConfirm: null,
    onCancel: null
  })

  function showConfirm(options) {
    return new Promise((resolve) => {
      confirmDialog.value = {
        show: true,
        title: options.title || '提示',
        message: options.message || '',
        confirmText: options.confirmText || '确定',
        cancelText: options.cancelText || '取消',
        type: options.type || 'info',
        onConfirm: () => {
          confirmDialog.value.show = false
          resolve(true)
        },
        onCancel: () => {
          confirmDialog.value.show = false
          resolve(false)
        }
      }
    })
  }

  function closeConfirm() {
    confirmDialog.value.show = false
  }

  // ========================================
  // 主题设置
  // ========================================
  const isDarkMode = ref(false)

  function initTheme() {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme) {
      isDarkMode.value = savedTheme === 'dark'
    } else {
      isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  function toggleTheme() {
    isDarkMode.value = !isDarkMode.value
    localStorage.setItem('theme', isDarkMode.value ? 'dark' : 'light')
    applyTheme()
  }

  function applyTheme() {
    if (isDarkMode.value) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }
  }

  // ========================================
  // 侧边栏状态 (移动端)
  // ========================================
  const isSidebarOpen = ref(false)

  function toggleSidebar() {
    isSidebarOpen.value = !isSidebarOpen.value
  }

  function closeSidebar() {
    isSidebarOpen.value = false
  }

  // ========================================
  // 页面滚动状态
  // ========================================
  const isScrolled = ref(false)
  const scrollY = ref(0)

  function updateScroll(y) {
    scrollY.value = y
    isScrolled.value = y > 50
  }

  // ========================================
  // 网络状态
  // ========================================
  const isOnline = ref(navigator.onLine)

  function initNetworkListener() {
    window.addEventListener('online', () => {
      isOnline.value = true
      showSuccess('网络已恢复')
    })
    window.addEventListener('offline', () => {
      isOnline.value = false
      showError('网络已断开', 0)
    })
  }

  // ========================================
  // API 错误监听
  // ========================================
  function initApiErrorListener() {
    window.addEventListener('api:error', (e) => {
      showError(e.detail.message)
    })
  }

  // ========================================
  // 初始化
  // ========================================
  function init() {
    initTheme()
    initNetworkListener()
    initApiErrorListener()
  }

  return {
    // 模态框
    modalData,
    isModalOpen,
    openModal,
    closeModal,
    
    // Toast
    toast,
    showToast,
    hideToast,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    
    // 全局加载
    globalLoading,
    loadingText,
    startLoading,
    stopLoading,
    forceStopLoading,
    
    // 确认对话框
    confirmDialog,
    showConfirm,
    closeConfirm,
    
    // 主题
    isDarkMode,
    toggleTheme,
    
    // 侧边栏
    isSidebarOpen,
    toggleSidebar,
    closeSidebar,
    
    // 滚动
    isScrolled,
    scrollY,
    updateScroll,
    
    // 网络
    isOnline,
    
    // 初始化
    init
  }
})
