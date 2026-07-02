<template>
  <span class="inline-flex items-center align-middle">
    <!-- 官方认证 -->
    <span v-if="type === 'department' || type === 'ORG'" class="ml-1 text-blue-500" title="官方认证">
      <BadgeCheck class="w-4 h-4 fill-blue-500 text-white" />
    </span>
    
    <!-- 教师认证 -->
    <span v-else-if="type === 'teacher' || type === 'TEACHER'" class="ml-1.5 px-1.5 py-0.5 rounded text-[10px] font-medium bg-amber-50 text-amber-600 border border-amber-200 align-middle inline-block leading-none">
      教师
    </span>
  </span>
</template>

<script setup>
import { BadgeCheck } from 'lucide-vue-next'

defineProps({
  type: {
    type: String,
    default: ''
  }
})
</script>
