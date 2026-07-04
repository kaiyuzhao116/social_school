<template>
  <div class="coming-soon-page animate-in fade-in duration-500">
    <div class="coming-card">
      <div class="icon-wrap">
        <BookOpen class="w-10 h-10" />
      </div>

      <div class="badge">
        <Sparkles class="w-4 h-4" />
        学习模块建设中
      </div>

      <h1>敬请期待</h1>

      <p class="desc">
        学习资料库、自习室状态、学习搭子等功能正在开发中。
        后续将接入真实数据，为你提供更实用的校园学习服务。
      </p>

      <div class="feature-list">
        <div class="feature-item">
          <Clock class="w-4 h-4" />
          <span>自习室状态</span>
        </div>

        <div class="feature-item">
          <Download class="w-4 h-4" />
          <span>学习资料共享</span>
        </div>

        <div class="feature-item">
          <Users class="w-4 h-4" />
          <span>学习小组</span>
        </div>
      </div>

      <button class="back-btn" @click="goHome">
        先去首页看看
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  BookOpen,
  Clock,
  Download,
  Users,
  Sparkles
} from 'lucide-vue-next'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.coming-soon-page {
  min-height: calc(100vh - 180px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
}

.coming-card {
  position: relative;
  width: 100%;
  max-width: 720px;
  padding: 56px 48px;
  border-radius: 32px;
  text-align: center;
  overflow: hidden;

  background:
      radial-gradient(circle at 15% 15%, rgba(99, 102, 241, 0.22), transparent 30%),
      radial-gradient(circle at 85% 20%, rgba(59, 130, 246, 0.2), transparent 32%),
      linear-gradient(180deg, #ffffff 0%, #f8faff 100%);

  border: 1px solid rgba(226, 232, 240, 0.95);
  box-shadow:
      0 24px 70px rgba(79, 86, 110, 0.14),
      inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.dark .coming-card {
  background:
      radial-gradient(circle at 15% 15%, rgba(99, 102, 241, 0.28), transparent 30%),
      radial-gradient(circle at 85% 20%, rgba(59, 130, 246, 0.22), transparent 32%),
      linear-gradient(180deg, #111827 0%, #0f172a 100%);

  border-color: rgba(55, 65, 81, 0.9);
  box-shadow: 0 24px 70px rgba(0, 0, 0, 0.35);
}

.coming-card::before {
  content: '';
  position: absolute;
  width: 220px;
  height: 220px;
  left: -80px;
  bottom: -90px;
  border-radius: 50%;
  background: rgba(99, 102, 241, 0.12);
  filter: blur(4px);
}

.coming-card::after {
  content: '';
  position: absolute;
  width: 160px;
  height: 160px;
  right: -60px;
  top: -60px;
  border-radius: 50%;
  background: rgba(6, 182, 212, 0.16);
  filter: blur(4px);
}

.icon-wrap {
  position: relative;
  z-index: 1;
  width: 86px;
  height: 86px;
  margin: 0 auto 22px;
  border-radius: 28px;
  display: flex;
  align-items: center;
  justify-content: center;

  color: #ffffff;
  background: linear-gradient(135deg, #6366f1, #3b82f6, #06b6d4);
  box-shadow: 0 18px 38px rgba(59, 130, 246, 0.35);
}

.badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  color: #4f46e5;
  background: rgba(99, 102, 241, 0.1);
  margin-bottom: 18px;
}

.dark .badge {
  color: #a5b4fc;
  background: rgba(99, 102, 241, 0.18);
}

h1 {
  position: relative;
  z-index: 1;
  font-size: 44px;
  line-height: 1.15;
  font-weight: 900;
  color: #111827;
  margin: 0;
  letter-spacing: 2px;
}

.dark h1 {
  color: #ffffff;
}

.desc {
  position: relative;
  z-index: 1;
  max-width: 520px;
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
  z-index: 1;
  margin: 30px auto 0;
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 700;
  color: #374151;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(226, 232, 240, 0.9);
}

.dark .feature-item {
  color: #e5e7eb;
  background: rgba(31, 41, 55, 0.72);
  border-color: rgba(75, 85, 99, 0.75);
}

.back-btn {
  position: relative;
  z-index: 1;
  margin-top: 34px;
  height: 44px;
  padding: 0 26px;
  border: none;
  border-radius: 999px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 800;
  cursor: pointer;
  background: linear-gradient(135deg, #6366f1, #3b82f6);
  box-shadow: 0 12px 26px rgba(59, 130, 246, 0.32);
  transition: all 0.22s ease;
}

.back-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 32px rgba(59, 130, 246, 0.42);
}

.back-btn:active {
  transform: translateY(0);
}

@media (max-width: 640px) {
  .coming-card {
    padding: 42px 24px;
    border-radius: 26px;
  }

  h1 {
    font-size: 36px;
  }

  .desc {
    font-size: 14px;
  }
}
</style>