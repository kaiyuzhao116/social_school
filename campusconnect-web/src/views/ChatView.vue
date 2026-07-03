<template>
  <div class="chat-page">
    <div class="chat-container">
      <div class="chat-header">
        <div>
          <h1>校园聊天室</h1>
          <p>拼团沟通、学习交流、校园闲聊</p>

          <!-- 临时调试用：确认当前登录用户 ID，确认没问题后可以删掉 -->
          <span class="debug-user-id">
            当前用户ID：{{ currentUserId || '加载中' }}
          </span>
        </div>

        <div class="header-actions">
          <button
              class="burn-mode-btn"
              :class="{ active: burnAfterReadMode }"
              @click="burnAfterReadMode = !burnAfterReadMode"
          >
            {{ burnAfterReadMode ? '阅后即焚：开' : '阅后即焚：关' }}
          </button>

          <button class="private-btn" @click="openPrivateUserDialog">
            发起私聊
          </button>

          <button class="back-btn" @click="router.push('/')">
            返回首页
          </button>
        </div>
      </div>

      <div class="chat-box">
        <vue-advanced-chat
            v-if="currentUserId && currentUserId !== 'undefined'"
            :key="`chat-${currentUserId}-${rooms.length}-${rooms.map(r => r.roomId + '-' + r.unreadCount).join('_')}`"
            height="calc(100vh - 190px)"
            :current-user-id.prop="currentUserId"
            :rooms="JSON.stringify(rooms)"
            :rooms-loaded="roomsLoaded"
            :messages="JSON.stringify(messages)"
            :messages-loaded="messagesLoaded"
            theme="light"
            @fetch-messages="fetchMessages($event.detail[0])"
            @send-message="sendMessage($event.detail[0])"
            @add-room="openPrivateUserDialog"
            :message-actions="JSON.stringify(messageActions)"
            @message-action-handler="handleMessageAction($event.detail[0])"
        />

        <div v-else class="chat-loading">
          正在加载用户信息...
        </div>

        <div v-if="showPrivateDialog" class="private-dialog-mask">
          <div class="private-dialog">
            <div class="private-dialog-header">
              <h3>发起私聊</h3>
              <button @click="showPrivateDialog = false">×</button>
            </div>

            <div class="private-user-list">
              <div
                  v-for="user in privateUsers"
                  :key="user.userId"
                  class="private-user-item"
                  @click="startPrivateChat(user)"
              >
                <div class="private-user-avatar">
                  {{ getUserAvatarText(user) }}
                </div>

                <div class="private-user-info">
                  <div class="private-user-name">
                    {{ user.nickname || user.username }}
                  </div>
                  <div class="private-user-sub">
                    @{{ user.username }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div v-if="showReadDialog" class="private-dialog-mask">
          <div class="read-dialog">
            <div class="private-dialog-header">
              <h3>消息已读详情</h3>
              <button @click="showReadDialog = false">×</button>
            </div>

            <div class="read-dialog-body">
              <div class="read-count-box">
                已读人数：<strong>{{ readCount }}</strong>
              </div>

              <div v-if="readLoading" class="read-loading">
                正在加载已读信息...
              </div>

              <div v-else-if="readUsers.length === 0" class="read-empty">
                暂无已读用户
              </div>

              <div v-else class="read-user-list">
                <div
                    v-for="user in readUsers"
                    :key="user.userId"
                    class="read-user-item"
                >
                  <div class="private-user-avatar">
                    {{ (user.nickname || user.username || '?').slice(0, 1) }}
                  </div>

                  <div class="private-user-info">
                    <div class="private-user-name">
                      {{ user.nickname || user.username }}
                    </div>
                    <div class="private-user-sub">
                      {{ user.readTime }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { register } from 'vue-advanced-chat'
import { chatApi, userApi } from '@/api'

register()

const router = useRouter()

// 当前登录用户 ID，由 userApi.getCurrentUser() 动态获取
const currentUserId = ref('')

// WebSocket 连接对象
const chatSocket = ref(null)

const rooms = ref([])
const messages = ref([])

const roomsLoaded = ref(false)
const messagesLoaded = ref(false)

const currentRoomId = ref(null)
const showPrivateDialog = ref(false)
const privateUsers = ref([])
// 是否开启阅后即焚发送模式
const burnAfterReadMode = ref(false)

// 防止同一条阅后即焚消息重复触发定时器
const burnTimerMap = new Map()

const showReadDialog = ref(false)
const readLoading = ref(false)
const readCount = ref(0)
const readUsers = ref([])

const messageActions = ref([
  {
    name: 'showReadUsers',
    title: '查看已读'
  }
])
/**
 * 兼容不同 request 封装返回格式
 */
function getListData(res) {
  if (Array.isArray(res)) return res
  if (Array.isArray(res?.data)) return res.data
  if (Array.isArray(res?.data?.data)) return res.data.data
  return []
}

/**
 * 兼容不同 request 封装返回格式
 */
function getObjectData(res) {
  if (res?.data?.data) return res.data.data
  if (res?.data) return res.data
  return res
}

/**
 * 从当前用户对象中尽量提取用户 ID
 */
function extractUserId(user) {
  return (
      user?.id ||
      user?.userId ||
      user?.uid ||
      user?.user?.id ||
      user?.user?.userId ||
      user?.userInfo?.id ||
      user?.userInfo?.userId ||
      user?.profile?.id ||
      user?.profile?.userId
  )
}

/**
 * 加载当前登录用户
 */
async function loadCurrentUser() {
  try {
    const res = await userApi.getCurrentUser()
    const user = getObjectData(res)

    console.log('当前登录用户完整数据：', user)

    const id = extractUserId(user)

    if (!id) {
      console.error('没有从当前用户接口里拿到用户ID：', user)
      currentUserId.value = ''
      return
    }

    currentUserId.value = String(id)

    console.log('当前登录用户ID：', currentUserId.value)
  } catch (error) {
    console.error('获取当前用户失败：', error)
    currentUserId.value = ''
  }
}

/**
 * 建立 WebSocket 连接
 */
function connectChatWebSocket() {
  if (!currentUserId.value || currentUserId.value === 'undefined') {
    console.warn('当前用户ID为空，暂不连接 WebSocket')
    return
  }

  const wsUrl = `ws://localhost:8080/api/ws/chat?userId=${currentUserId.value}`

  chatSocket.value = new WebSocket(wsUrl)

  chatSocket.value.onopen = () => {
    console.log('WebSocket 连接成功')
  }

  chatSocket.value.onmessage = async (event) => {
    console.log('收到 WebSocket 消息：', event.data)

    try {
      const payload = JSON.parse(event.data)

      if (payload.type === 'CONNECTED') {
        return
      }

      if (payload.type === 'CHAT_MESSAGE') {
        handleWebSocketChatMessage(payload.data)
      }
    } catch (error) {
      console.error('解析 WebSocket 消息失败：', error)
    }
  }

  chatSocket.value.onerror = (error) => {
    console.error('WebSocket 连接异常：', error)
  }

  chatSocket.value.onclose = () => {
    console.log('WebSocket 已断开')
  }
}
/**
 * 处理 WebSocket 推送过来的聊天消息
 */
function handleWebSocketChatMessage(message) {
  if (!message || !message.messageId) {
    return
  }

  const roomId = String(message.conversationId)

  updateRoomLastMessage(roomId, message)

  // 如果当前正打开这个聊天室，就直接追加到消息列表
  if (String(currentRoomId.value) === roomId) {
    const exists = messages.value.some(
        item => String(item._id) === String(message.messageId)
    )

    if (!exists) {
      const convertedMessage = convertMessage(message)

      messages.value = [
        ...messages.value,
        convertedMessage
      ]

      scheduleBurnMessage(convertedMessage)
    }

    markRoomRead(roomId)
    return
  }

  // 如果不是当前聊天室，就给左侧未读数 +1
  rooms.value = rooms.value.map(room => {
    if (room.roomId !== roomId) {
      return room
    }

    return {
      ...room,
      unreadCount: (room.unreadCount || 0) + 1
    }
  })
}
/**
 * 后端聊天室列表 -> vue-advanced-chat rooms
 */
function convertRoom(item, index) {
  return {
    roomId: String(item.roomId),
    type: item.type,
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
  const burned = Number(item.burned ?? item.burned_status ?? 0)
  const burnAfterRead = Number(item.burnAfterRead ?? item.burn_after_read ?? 0)

  return {
    _id: String(item.messageId),
    content: burned === 1 ? '消息已焚毁' : item.content,
    senderId: String(item.senderId),
    username: item.username || '未知用户',
    date: item.messageDate || '今天',
    timestamp: item.messageTime || '',
    saved: true,
    distributed: true,
    seen: false,

    burnAfterRead,
    burned
  }
}
async function startPrivateChat(user) {
  if (!user || !user.userId) {
    return
  }

  try {
    const res = await chatApi.createPrivateConversation({
      targetUserId: user.userId
    })

    const room = getObjectData(res)

    console.log('创建/获取私聊会话：', room)

    showPrivateDialog.value = false

    const convertedRoom = convertRoom(room, 0)

    const exists = rooms.value.some(
        item => String(item.roomId) === String(convertedRoom.roomId)
    )

    if (!exists) {
      rooms.value = [
        convertedRoom,
        ...rooms.value
      ]
    } else {
      rooms.value = rooms.value.map(item => {
        if (String(item.roomId) === String(convertedRoom.roomId)) {
          return {
            ...item,
            ...convertedRoom
          }
        }
        return item
      })
    }

    rooms.value = rooms.value.map((item, index) => ({
      ...item,
      index
    }))

    currentRoomId.value = String(convertedRoom.roomId)

    await loadMessages(currentRoomId.value)
  } catch (error) {
    console.error('创建私聊失败：', error)
  }
}
function getUserAvatarText(user) {
  const name = user?.nickname || user?.username || '?'
  return name.slice(0, 1)
}
async function openPrivateUserDialog() {
  showPrivateDialog.value = true

  try {
    const res = await chatApi.getPrivateUsers()
    privateUsers.value = getListData(res)

    console.log('可私聊用户列表：', privateUsers.value)
  } catch (error) {
    console.error('加载可私聊用户失败：', error)
    privateUsers.value = []
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

    scheduleBurnAfterReadMessages()

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
  console.log('准备标记已读，roomId =', roomId)

  try {
    const res = await chatApi.markConversationRead(roomId)

    console.log('标记已读接口返回：', res)

    rooms.value = rooms.value.map((room) => {
      if (String(room.roomId) !== String(roomId)) {
        return room
      }

      return {
        ...room,
        unreadCount: 0
      }
    })

    rooms.value = [...rooms.value]

    console.log(
        '清零后的 rooms：',
        rooms.value.map(room => ({
          roomId: room.roomId,
          roomName: room.roomName,
          unreadCount: room.unreadCount
        }))
    )
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

  if (!roomId) {
    messagesLoaded.value = true
    return
  }

  currentRoomId.value = String(roomId)

  await loadMessages(roomId)
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
      burnAfterRead: burnAfterReadMode.value,
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
  rooms.value = rooms.value.map((room) => {
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
/**
 * 检查当前消息列表里有没有需要焚毁的消息
 */
function scheduleBurnAfterReadMessages() {
  messages.value.forEach((message) => {
    scheduleBurnMessage(message)
  })
}

/**
 * 对单条阅后即焚消息设置焚毁定时器
 */
function scheduleBurnMessage(message) {
  console.log('检查阅后即焚消息：', message)

  if (!message || !message._id) {
    console.log('跳过：消息为空')
    return
  }

  if (Number(message.burnAfterRead) !== 1) {
    console.log('跳过：不是阅后即焚消息', message._id, message.burnAfterRead)
    return
  }

  if (Number(message.burned) === 1) {
    console.log('跳过：已经焚毁', message._id)
    return
  }

  if (String(message.senderId) === String(currentUserId.value)) {
    console.log('跳过：这是自己发的阅后即焚消息', message._id)
    return
  }

  if (burnTimerMap.has(message._id)) {
    console.log('跳过：已经设置过定时器', message._id)
    return
  }

  console.log('阅后即焚消息开始倒计时：', message._id)

  const timer = setTimeout(async () => {
    try {
      console.log('准备调用 burn-read 接口：', message._id)

      await chatApi.burnReadMessage(message._id)

      messages.value = messages.value.map((item) => {
        if (String(item._id) !== String(message._id)) {
          return item
        }

        return {
          ...item,
          content: '消息已焚毁',
          burned: 1
        }
      })

      burnTimerMap.delete(message._id)

      console.log('阅后即焚消息已焚毁：', message._id)
    } catch (error) {
      console.error('阅后即焚消息焚毁失败：', error)
      burnTimerMap.delete(message._id)
    }
  }, 5000)

  burnTimerMap.set(message._id, timer)
}
/**
 * 页面初始化
 */
onMounted(async () => {
  await loadCurrentUser()

  if (!currentUserId.value || currentUserId.value === 'undefined') {
    console.error('当前用户ID无效，停止加载聊天室')
    return
  }

  connectChatWebSocket()
  await loadRooms()
})
async function testReadStatus() {
  if (!messages.value || messages.value.length === 0) {
    console.warn('当前没有消息，无法查看已读')
    return
  }

  const lastMessage = messages.value[messages.value.length - 1]

  await openMessageReadDialog(lastMessage)
}

async function openMessageReadDialog(message) {
  if (!message || !message._id) {
    console.warn('消息为空，无法查看已读')
    return
  }

  const messageId = message._id

  showReadDialog.value = true
  readLoading.value = true
  readCount.value = 0
  readUsers.value = []

  console.log('准备查看消息已读详情，messageId =', messageId)

  try {
    const countRes = await chatApi.getMessageReadCount(messageId)
    const usersRes = await chatApi.getMessageReadUsers(messageId)

    readCount.value = getObjectData(countRes)
    readUsers.value = getListData(usersRes)

    console.log('消息已读人数：', readCount.value)
    console.log('消息已读用户列表：', readUsers.value)
  } catch (error) {
    console.error('查询消息已读详情失败：', error)
    readCount.value = 0
    readUsers.value = []
  } finally {
    readLoading.value = false
  }
}

function handleMessageAction(data) {
  console.log('消息菜单事件：', data)

  const actionName = data?.action?.name || data?.action || data?.name
  const message = data?.message || data

  if (actionName === 'showReadUsers') {
    openMessageReadDialog(message)
  }
}
/**
 * 离开页面时关闭 WebSocket
 */
onBeforeUnmount(() => {
  if (chatSocket.value) {
    chatSocket.value.close()
  }

  burnTimerMap.forEach((timer) => {
    clearTimeout(timer)
  })

  burnTimerMap.clear()
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

.debug-user-id {
  display: inline-block;
  margin-top: 4px;
  font-size: 12px;
  color: #8b95a7;
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
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.private-btn {
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 12px;
  background: #5b5cf6;
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(91, 92, 246, 0.18);
  transition: all 0.2s ease;
}

.private-btn:hover {
  background: #4f46e5;
  transform: translateY(-1px);
}
.chat-box {
  position: relative;
  height: calc(100vh - 190px);
  background: #ffffff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 12px 32px rgba(80, 90, 120, 0.13);
  border: 1px solid #edf0f7;
}

.chat-loading {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b95a7;
  font-size: 14px;
}
.private-dialog-mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.private-dialog {
  width: 360px;
  max-height: 480px;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.private-dialog-header {
  height: 54px;
  padding: 0 18px;
  border-bottom: 1px solid #edf0f7;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.private-dialog-header h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.private-dialog-header button {
  border: none;
  background: transparent;
  font-size: 24px;
  color: #8b95a7;
  cursor: pointer;
}

.private-user-list {
  max-height: 420px;
  overflow-y: auto;
  padding: 8px;
}

.private-user-item {
  height: 64px;
  border-radius: 14px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background 0.2s ease;
}

.private-user-item:hover {
  background: #f3f6ff;
}

.private-user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #6366f1;
  color: #ffffff;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.private-user-name {
  font-size: 14px;
  font-weight: 700;
  color: #111827;
}

.private-user-sub {
  margin-top: 3px;
  font-size: 12px;
  color: #8b95a7;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.burn-mode-btn {
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 12px;
  background: #ffffff;
  color: #ef4444;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(80, 90, 120, 0.08);
  transition: all 0.2s ease;
}

.burn-mode-btn.active {
  background: #ef4444;
  color: #ffffff;
  box-shadow: 0 8px 20px rgba(239, 68, 68, 0.2);
}

.burn-mode-btn:hover {
  transform: translateY(-1px);
}

.read-test-btn {
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 12px;
  background: #ffffff;
  color: #16a34a;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(80, 90, 120, 0.08);
  transition: all 0.2s ease;
}

.read-test-btn:hover {
  background: #f0fdf4;
  transform: translateY(-1px);
}

.read-dialog {
  width: 380px;
  max-height: 520px;
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 20px 45px rgba(15, 23, 42, 0.18);
  overflow: hidden;
}

.read-dialog-body {
  padding: 16px;
}

.read-count-box {
  height: 44px;
  border-radius: 12px;
  background: #f0fdf4;
  color: #166534;
  display: flex;
  align-items: center;
  padding: 0 14px;
  font-size: 14px;
  margin-bottom: 12px;
}

.read-count-box strong {
  margin-left: 4px;
  font-size: 18px;
}

.read-loading,
.read-empty {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b95a7;
  font-size: 14px;
}

.read-user-list {
  max-height: 360px;
  overflow-y: auto;
}

.read-user-item {
  height: 62px;
  border-radius: 14px;
  padding: 0 10px;
  display: flex;
  align-items: center;
}

.read-user-item:hover {
  background: #f3f6ff;
}
</style>
