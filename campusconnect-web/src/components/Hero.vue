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
    image: 'https://picsum.photos/1200/600?random=1',
    title: '连接 · 学习 · 成长',
    subtitle: '你的校园生活，化繁为简。探索活动、资源等更多内容。'
  },
  {
    image: 'https://picsum.photos/1200/600?random=2',
    title: '探索 · 发现 · 创新',
    subtitle: '加入社团，参与竞赛，激发你的无限潜能。'
  },
  {
    image: 'https://picsum.photos/1200/600?random=3',
    title: '分享 · 交流 · 进步',
    subtitle: '在校园社区中找到志同道合的伙伴，共同进步。'
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
