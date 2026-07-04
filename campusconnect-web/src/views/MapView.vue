<template>
  <div class="map-coming-page animate-in fade-in duration-500">
    <div class="map-card">
      <div class="map-bg-grid"></div>
      <div class="float-pin pin-one">
        <MapPin class="w-5 h-5" />
      </div>
      <div class="float-pin pin-two">
        <Navigation class="w-5 h-5" />
      </div>
      <div class="float-pin pin-three">
        <Compass class="w-5 h-5" />
      </div>

      <div class="main-icon">
        <Map class="w-11 h-11" />
      </div>

      <div class="status-badge">
        <Sparkles class="w-4 h-4" />
        校园地图建设中
      </div>

      <h1>敬请期待</h1>

      <p class="desc">
        校园地图、地点搜索、路线导航、教学楼与宿舍定位等功能正在开发中。
        后续将接入真实校园数据，为你提供更准确的校园导航体验。
      </p>

      <div class="feature-list">
        <div class="feature-item">
          <Search class="w-4 h-4" />
          <span>地点搜索</span>
        </div>

        <div class="feature-item">
          <Navigation class="w-4 h-4" />
          <span>路线导航</span>
        </div>

        <div class="feature-item">
          <MapPin class="w-4 h-4" />
          <span>校园点位</span>
        </div>
      </div>

      <button class="back-btn" @click="goHome">
        返回首页
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  Map,
  MapPin,
  Navigation,
  Compass,
  Search,
  Sparkles
} from 'lucide-vue-next'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.map-coming-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 42px 16px;
}

.map-card {
  position: relative;
  width: 100%;
  max-width: 780px;
  min-height: 520px;
  border-radius: 34px;
  overflow: hidden;
  text-align: center;
  padding: 64px 48px;
  box-sizing: border-box;

  background:
      radial-gradient(circle at 20% 18%, rgba(99, 102, 241, 0.22), transparent 28%),
      radial-gradient(circle at 80% 20%, rgba(14, 165, 233, 0.22), transparent 30%),
      linear-gradient(180deg, #ffffff 0%, #f6f9ff 100%);

  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow:
      0 26px 80px rgba(79, 86, 110, 0.15),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.dark .map-card {
  background:
      radial-gradient(circle at 20% 18%, rgba(99, 102, 241, 0.28), transparent 28%),
      radial-gradient(circle at 80% 20%, rgba(14, 165, 233, 0.24), transparent 30%),
      linear-gradient(180deg, #111827 0%, #0f172a 100%);

  border-color: rgba(55, 65, 81, 0.9);
  box-shadow: 0 26px 80px rgba(0, 0, 0, 0.36);
}

.map-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
      linear-gradient(rgba(148, 163, 184, 0.16) 1px, transparent 1px),
      linear-gradient(90deg, rgba(148, 163, 184, 0.16) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at center, black, transparent 75%);
  opacity: 0.8;
}

.main-icon {
  position: relative;
  z-index: 2;
  width: 92px;
  height: 92px;
  margin: 0 auto 24px;
  border-radius: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;

  background: linear-gradient(135deg, #6366f1, #3b82f6, #06b6d4);
  box-shadow:
      0 20px 45px rgba(59, 130, 246, 0.38),
      inset 0 1px 0 rgba(255, 255, 255, 0.35);
}

.status-badge {
  position: relative;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 15px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 800;
  color: #4f46e5;
  background: rgba(99, 102, 241, 0.11);
  margin-bottom: 18px;
}

.dark .status-badge {
  color: #a5b4fc;
  background: rgba(99, 102, 241, 0.18);
}

h1 {
  position: relative;
  z-index: 2;
  margin: 0;
  font-size: 46px;
  line-height: 1.15;
  font-weight: 900;
  color: #111827;
  letter-spacing: 2px;
}

.dark h1 {
  color: #ffffff;
}

.desc {
  position: relative;
  z-index: 2;
  max-width: 560px;
  margin: 18px auto 0;
  font-size: 15px;
  line-height: 1.9;
  color: #6b7280;
}

.dark .desc {
  color: #cbd5e1;
}

.feature-list {
  position: relative;
  z-index: 2;
  margin: 32px auto 0;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 15px;
  border-radius: 15px;
  font-size: 13px;
  font-weight: 800;
  color: #374151;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(226, 232, 240, 0.95);
  backdrop-filter: blur(10px);
}

.dark .feature-item {
  color: #e5e7eb;
  background: rgba(31, 41, 55, 0.72);
  border-color: rgba(75, 85, 99, 0.78);
}

.back-btn {
  position: relative;
  z-index: 2;
  margin-top: 36px;
  height: 44px;
  padding: 0 28px;
  border: none;
  border-radius: 999px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  background: linear-gradient(135deg, #6366f1, #3b82f6);
  box-shadow: 0 13px 28px rgba(59, 130, 246, 0.33);
  transition: all 0.22s ease;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 17px 35px rgba(59, 130, 246, 0.45);
}

.back-btn:active {
  transform: translateY(0);
}

.float-pin {
  position: absolute;
  z-index: 1;
  width: 42px;
  height: 42px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  opacity: 0.85;
  animation: float 3s ease-in-out infinite;
}

.pin-one {
  left: 16%;
  top: 22%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.pin-two {
  right: 18%;
  top: 26%;
  background: linear-gradient(135deg, #3b82f6, #06b6d4);
  animation-delay: 0.5s;
}

.pin-three {
  left: 22%;
  bottom: 20%;
  background: linear-gradient(135deg, #f97316, #ef4444);
  animation-delay: 0.9s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-10px);
  }
}

@media (max-width: 640px) {
  .map-card {
    padding: 44px 24px;
    min-height: 480px;
    border-radius: 28px;
  }

  h1 {
    font-size: 36px;
  }

  .desc {
    font-size: 14px;
  }

  .float-pin {
    display: none;
  }
}
</style>