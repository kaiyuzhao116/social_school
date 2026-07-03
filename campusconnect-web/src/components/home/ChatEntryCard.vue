<template>
  <div class="chat-entry-card" @click="goChat">
    <div class="chat-top">
      <div class="chat-icon">💬</div>

      <div class="chat-title-box">
        <div class="chat-title">校园聊天室</div>
        <div class="chat-desc">实时聊天 · 拼团沟通 · 学习交流</div>
      </div>

      <div class="chat-status">
        <span class="dot"></span>
        在线 {{ onlineCount }}
      </div>
    </div>

    <div class="chat-room-list">
      <div class="chat-room">
        <span class="room-name">🍱 拼团群聊</span>
        <span class="unread">3</span>
      </div>

      <div class="chat-room">
        <span class="room-name">📚 学习交流</span>
        <span class="unread">1</span>
      </div>

      <div class="chat-room">
        <span class="room-name">🏀 校园闲聊</span>
        <span class="read">已读</span>
      </div>
    </div>

    <button class="enter-btn" @click.stop="goChat">
      进入聊天室
    </button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const onlineCount = 23

const goChat = () => {
  router.push('/chat')
}
</script>

<style scoped>
.chat-entry-card {
  width: 100%;
  background: #ffffff;
  border-radius: 18px;
  padding: 18px;
  box-sizing: border-box;
  margin-bottom: 22px;
  cursor: pointer;
  border: 1px solid #eef0f6;
  box-shadow: 0 8px 24px rgba(79, 86, 110, 0.08);
  transition: all 0.2s ease;
}

.chat-entry-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(79, 86, 110, 0.13);
}

.chat-top {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: linear-gradient(135deg, #6366f1, #3b82f6);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.chat-title-box {
  flex: 1;
  min-width: 0;
}

.chat-title {
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.chat-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #8b95a7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 8px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #16a34a;
  font-size: 12px;
  white-space: nowrap;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22c55e;
}

.chat-room-list {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.chat-room {
  height: 34px;
  padding: 0 10px;
  border-radius: 12px;
  background: #f8f9ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.room-name {
  font-size: 13px;
  color: #374151;
}

.unread {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.read {
  font-size: 12px;
  color: #9ca3af;
}

.enter-btn {
  width: 100%;
  height: 40px;
  border: none;
  margin-top: 16px;
  border-radius: 13px;
  background: linear-gradient(135deg, #6366f1, #3b82f6);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
</style>