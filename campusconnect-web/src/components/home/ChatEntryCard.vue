<template>
  <button class="chat-entry-button" type="button" @click="goChat">
    <span class="glow"></span>
    <span class="scan-line"></span>

    <span class="left">
      <span class="icon-wrap">
        <span class="chat-icon">💬</span>
        <span class="pulse-dot"></span>
      </span>

      <span class="text-box">
        <span class="title">进入校园聊天室</span>
        <span class="desc">
<!--          {{ onlineCount }} 人在线 · {{ unreadCount }} 条新消息-->
        </span>
      </span>
    </span>

    <span class="arrow">
      →
    </span>
  </button>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const onlineCount = 23
const unreadCount = 4

const goChat = () => {
  router.push('/chat')
}
</script>

<style scoped>
.chat-entry-button {
  position: relative;
  width: 100%;
  height: 86px;
  margin-bottom: 22px;
  padding: 0 18px;
  border: none;
  border-radius: 22px;
  cursor: pointer;
  overflow: hidden;

  display: flex;
  align-items: center;
  justify-content: space-between;

  background:
      radial-gradient(circle at 20% 20%, rgba(255, 255, 255, 0.32), transparent 28%),
      linear-gradient(135deg, #6366f1 0%, #3b82f6 45%, #06b6d4 100%);

  color: #fff;
  box-shadow:
      0 16px 36px rgba(59, 130, 246, 0.35),
      inset 0 1px 0 rgba(255, 255, 255, 0.35);

  transition: all 0.25s ease;
}

.chat-entry-button:hover {
  transform: translateY(-3px) scale(1.01);
  box-shadow:
      0 22px 45px rgba(59, 130, 246, 0.46),
      inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.chat-entry-button:active {
  transform: translateY(0) scale(0.98);
}

.left {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 14px;
}

.icon-wrap {
  position: relative;
  width: 48px;
  height: 48px;
  border-radius: 17px;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.24);
}

.chat-icon {
  font-size: 23px;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.22));
}

.pulse-dot {
  position: absolute;
  right: -2px;
  top: -2px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.8);
  animation: pulse 1.5s infinite;
}

.text-box {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.title {
  font-size: 17px;
  font-weight: 800;
  letter-spacing: 0.4px;
}

.desc {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.9;
}

.arrow {
  position: relative;
  z-index: 2;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.22);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  font-weight: 700;
  transition: all 0.25s ease;
}

.chat-entry-button:hover .arrow {
  transform: translateX(4px);
  background: rgba(255, 255, 255, 0.32);
}

.glow {
  position: absolute;
  width: 130px;
  height: 130px;
  right: -35px;
  top: -40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.26);
  filter: blur(10px);
}

.scan-line {
  position: absolute;
  left: -80%;
  top: 0;
  width: 55%;
  height: 100%;
  background: linear-gradient(
      100deg,
      transparent,
      rgba(255, 255, 255, 0.28),
      transparent
  );
  transform: skewX(-18deg);
  animation: scan 3.2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.75);
  }
  70% {
    box-shadow: 0 0 0 9px rgba(34, 197, 94, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(34, 197, 94, 0);
  }
}

@keyframes scan {
  0% {
    left: -80%;
  }
  45% {
    left: 130%;
  }
  100% {
    left: 130%;
  }
}
</style>