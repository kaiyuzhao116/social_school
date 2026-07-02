<template>
  <div
    v-if="uiStore.modalData.type"
    class="fixed inset-0 z-[100] flex items-center justify-center p-4"
  >
    <div
      class="absolute inset-0 bg-gray-900/40 dark:bg-black/80 backdrop-blur-sm transition-opacity"
      @click="uiStore.closeModal()"
    ></div>
    <div
      :class="[
        'relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-h-[90vh] overflow-hidden animate-in fade-in zoom-in-95 duration-200 transition-all flex flex-col',
        modalSizeClass
      ]"
    >
      <component :is="currentModalComponent" />
    </div>
  </div>
</template>

<script setup>
import { computed, markRaw } from 'vue'
import { useUIStore } from '../stores'
import LoginModal from './modals/LoginModal.vue'
import ProfileModal from './modals/ProfileModal.vue'
import CreatePostModal from './modals/CreatePostModal.vue'
import PostDetailModal from './modals/PostDetailModal.vue'
import NotificationsModal from './modals/NotificationsModal.vue'
import MessagesModal from './modals/MessagesModal.vue'

const uiStore = useUIStore()

const modalComponents = {
  LOGIN: markRaw(LoginModal),
  PROFILE: markRaw(ProfileModal),
  CREATE_POST: markRaw(CreatePostModal),
  POST_DETAIL: markRaw(PostDetailModal),
  NOTIFICATIONS_FULL: markRaw(NotificationsModal),
  MESSAGES_FULL: markRaw(MessagesModal)
}

const currentModalComponent = computed(() => {
  return modalComponents[uiStore.modalData.type] || null
})

const modalSizeClass = computed(() => {
  const type = uiStore.modalData.type
  if (type === 'PROFILE') return 'max-w-4xl h-[85vh]'
  if (['CREATE_POST', 'POST_DETAIL', 'NOTIFICATIONS_FULL'].includes(type))
    return 'max-w-2xl'
  if (type === 'MESSAGES_FULL') return 'max-w-6xl h-[85vh]'
  if (type === 'LOGIN') return 'max-w-5xl w-full'
  return 'max-w-lg'
})
</script>
