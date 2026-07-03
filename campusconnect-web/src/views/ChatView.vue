<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-header">
        <div>
          <h1>校园聊天室</h1>
          <p>拼团沟通、学习交流、校园闲聊</p>
        </div>

        <button class="back-btn" @click="router.push('/')">
          返回首页
        </button>
      </div>

      <div class="chat-box">
        <vue-advanced-chat
            height="calc(100vh - 190px)"
            :current-user-id="currentUserId"
            :rooms="JSON.stringify(rooms)"
            :rooms-loaded="roomsLoaded"
            :messages="JSON.stringify(messages)"
            :messages-loaded="messagesLoaded"
            theme="light"
            @fetch-messages="fetchMessages($event.detail[0])"
            @send-message="sendMessage($event.detail[0])"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { register } from 'vue-advanced-chat'
import { chatApi,userApi } from '@/api'

register()

const router = useRouter()

// 目前后端写死的是 admin，id = 1，所以前端这里也先写死 1
//const currentUserId = '5'
const currentUserId = ref('')

const rooms = ref([])
const messages = ref([])

const roomsLoaded = ref(false)
const messagesLoaded = ref(false)

const currentRoomId = ref(null)

/**
 * 兼容不同 request 封装返回格式
 */
function getListData(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.data?.data)) return res.data.data
  return []
}

function getObjectData(res) {
  if (res?.data?.data) return res.data.data
  if (res?.data) return res.data
  return res
}
async function loadCurrentUser() {
  try {
    const res = await userApi.getCurrentUser()
    const user = getObjectData(res)

    currentUserId.value = String(user.id)

    console.log('当前登录用户ID：', currentUserId.value)
  } catch (error) {
    console.error('获取当前用户失败：', error)
  }
}onMounted()

/**
 * 后端聊天室列表 -> vue-advanced-chat rooms
 */
function convertRoom(item, index) {
  return {
    roomId: String(item.roomId),
    roomName: item.roomName,
    avatar: item.avatar || '',
    unreadCount: item.unreadCount || 0,
    index,

    lastMessage: {
      _id: String(item.roomId),
      content: item.lastMessageContent || '暂无消息',
      senderId: '',
      username: '',
      timestamp: item.lastMessageTime || '',
      saved: true,
      distributed: true,
      seen: true,
      new: false
    },

    users: [
      {
        _id: '1',
        username: '系统管理员',
        avatar: '',
        status: { state: 'online' }
      },
      {
        _id: '2',
        username: '林雨晴test1',
        avatar: '',
        status: { state: 'online' }
      },
      {
        _id: '3',
        username: '张伟',
        avatar: '',
        status: { state: 'offline' }
      },
      {
        _id: '4',
        username: '王志强',
        avatar: '',
        status: { state: 'offline' }
      },
      {
        _id: '5',
        username: '陈思思',
        avatar: '',
        status: { state: 'online' }
      }
    ]
  }
}

/**
 * 后端消息 -> vue-advanced-chat messages
 */
function convertMessage(item) {
  return {
    _id: String(item.messageId),
    content: item.content,
    senderId: String(item.senderId),
    username: item.username || '未知用户',
    date: item.messageDate || '今天',
    timestamp: item.messageTime || '',
    saved: true,
    distributed: true,
    seen: false
  }
}

/**
 * 加载聊天室列表
 */
async function loadRooms() {
  roomsLoaded.value = false

  try {
    const res = await chatApi.getConversations()
    const list = getListData(res)

    rooms.value = list.map((item, index) => convertRoom(item, index))

    if (rooms.value.length > 0) {
      currentRoomId.value = rooms.value[0].roomId
      await loadMessages(currentRoomId.value)
    }
  } catch (error) {
    console.error('加载聊天室列表失败：', error)
    rooms.value = []
  } finally {
    roomsLoaded.value = true
  }
}

/**
 * 加载某个聊天室历史消息
 */
async function loadMessages(roomId) {
  if (!roomId) return

  messagesLoaded.value = false

  try {
    const res = await chatApi.getMessages(roomId)
    const list = getListData(res)

    messages.value = list.map(convertMessage)
    await markRoomRead(roomId)
  } catch (error) {
    console.error('加载聊天消息失败：', error)
    messages.value = []
  } finally {
    messagesLoaded.value = true
  }
}
/**
 * 标记当前聊天室已读，并更新左侧未读数
 */
async function markRoomRead(roomId) {
  try {
    await chatApi.markConversationRead(roomId)

    rooms.value = rooms.value.map(room => {
      if (room.roomId !== String(roomId)) {
        return room
      }

      return {
        ...room,
        unreadCount: 0
      }
    })
  } catch (error) {
    console.error('标记已读失败：', error)
  }
}

/**
 * vue-advanced-chat 切换房间 / 拉取消息时会触发
 */
async function fetchMessages(data) {
  console.log('fetchMessages：', data)

  const roomId = data?.room?.roomId || data?.roomId

  if (roomId && roomId !== currentRoomId.value) {
    currentRoomId.value = roomId
    await loadMessages(roomId)
  } else {
    messagesLoaded.value = true
  }
}

/**
 * 发送消息
 */
async function sendMessage(data) {
  console.log('sendMessage：', data)

  const roomId = data?.roomId || currentRoomId.value

  if (!roomId) {
    console.warn('没有当前聊天室ID')
    return
  }

  const content = data?.content?.trim()

  if (!content) {
    return
  }

  try {
    const res = await chatApi.sendMessage({
      conversationId: Number(roomId),
      content,
      messageType: 1,
      clientMsgId: createClientMsgId()
    })

    const savedMessage = getObjectData(res)

    messages.value = [
      ...messages.value,
      convertMessage(savedMessage)
    ]

    updateRoomLastMessage(roomId, savedMessage)
  } catch (error) {
    console.error('发送消息失败：', error)
  }
}

/**
 * 更新左侧会话列表最后一条消息
 */
function updateRoomLastMessage(roomId, message) {
  rooms.value = rooms.value.map(room => {
    if (room.roomId !== String(roomId)) {
      return room
    }

    return {
      ...room,
      lastMessage: {
        _id: String(message.messageId),
        content: message.content,
        senderId: String(message.senderId),
        username: message.username,
        timestamp: message.messageTime,
        saved: true,
        distributed: true,
        seen: false,
        new: true
      }
    }
  })
}

/**
 * 生成前端消息ID，用于后端幂等
 */
function createClientMsgId() {
  if (window.crypto && window.crypto.randomUUID) {
    return window.crypto.randomUUID()
  }

  return `msg-${Date.now()}-${Math.random().toString(16).slice(2)}`
}

onMounted(async () => {
  await loadCurrentUser()
  await loadRooms()
})
</script>

<style scoped>
.chat-page {
  min-height: calc(100vh - 72px);
  background: #f5f7fb;
  padding: 28px 40px;
  box-sizing: border-box;
}

.chat-container {
  max-width: 1280px;
  margin: 0 auto;
}

.chat-header {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 800;
  color: #111827;
}

.chat-header p {
  margin: 6px 0 0;
  color: #8b95a7;
  font-size: 14px;
}

.back-btn {
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 12px;
  background: #ffffff;
  color: #5b5cf6;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(80, 90, 120, 0.08);
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: #f3f4ff;
  transform: translateY(-1px);
}

.chat-box {
  height: calc(100vh - 190px);
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(80, 90, 120, 0.13);
  border: 1px solid #edf0f7;
}
</style>