<template>
  <div
    class="relative w-full h-[180px] md:h-[320px] rounded-2xl md:rounded-3xl overflow-hidden mb-6 md:mb-8 shadow-lg md:shadow-xl group"
  >

    <div
      v-for="(slide, index) in slides"
      :key="index"
      :class="[
        'absolute inset-0 transition-opacity duration-1000 ease-in-out',
        index === currentSlide ? 'opacity-100' : 'opacity-0'
      ]"
    >
      <!-- Image -->
      <div
        class="absolute inset-0 bg-cover bg-center transform group-hover:scale-105 transition-transform duration-[2000ms]"
        :style="{ backgroundImage: `url('${slide.image}')` }"
      ></div>
      <!-- Gradient Overlay -->
      <div
        class="absolute inset-0 bg-gradient-to-r from-gray-900/90 via-gray-900/50 to-transparent"
      ></div>
      <!-- Content -->
      <div class="absolute inset-0 flex flex-col justify-center px-6 md:px-12">
        <h1
          class="text-2xl md:text-5xl font-bold text-white mb-2 md:mb-4 tracking-tight leading-tight animate-in fade-in slide-in-from-bottom-4 duration-700"
        >
          {{ slide.title }}
        </h1>
        <p
          class="text-sm md:text-xl text-gray-200 mb-4 md:mb-8 max-w-[80%] md:max-w-lg font-light leading-relaxed animate-in fade-in slide-in-from-bottom-5 duration-700 delay-100"
        >
          {{ slide.subtitle }}
        </p>
      </div>
    </div>

    <!-- Dots -->
    <div class="absolute bottom-6 left-6 md:left-12 flex gap-2 md:gap-4 z-10">
      <div
          class="inline-flex items-center gap-2 w-fit mb-3 px-3 py-1 rounded-full bg-white/15 border border-white/20 text-white text-xs font-bold backdrop-blur-sm"
      >
        📍 渤海大学 · 锦州
      </div>
      <div
          class="inline-flex items-center gap-2 w-fit mb-4 px-3 py-1 rounded-full bg-white/10 border border-white/20 text-white/90 text-xs font-medium backdrop-blur-sm"
      >
        🌊 网站仍在不断完善中，欢迎体验与建议，
        悲观者永远正确，乐观者永远前行
        想一起学习做网站计算机抖音号：renshengshengren
      </div>
      <button
        v-for="(_, index) in slides"
        :key="index"
        @click="currentSlide = index"
        :class="[
          'h-1 rounded-full transition-all duration-300',
          index === currentSlide
            ? 'w-8 md:w-12 bg-brand-purple'
            : 'w-4 md:w-8 bg-white/30 hover:bg-white/50'
        ]"
        :aria-label="`Go to slide ${index + 1}`"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const currentSlide = ref(0)
let timer = null

const slides = [
  {
    image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=1400&q=80',
    title: '渤大今天发生什么？',
    subtitle: '拼团互助、校园动态、即时聊天，一站式发现身边的新鲜事。'
  },
  {
    image: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1?auto=format&fit=crop&w=1400&q=80',
    title: '在渤大，找到同路人',
    subtitle: '一起自习、拼外卖、参加活动，让校园生活更轻松。'
  },
  {
    image: 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1400&q=80',
    title: '连接渤海大学生活圈',
    subtitle: '从图书馆到食堂，从宿舍到操场，校园信息不再错过。'
  }
]

onMounted(() => {
  timer = setInterval(() => {
    currentSlide.value = (currentSlide.value + 1) % slides.length
  }, 5000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
</script>
