import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { notificationApi, messageApi } from '../api'

// 分别控制通知和消息是否使用真实 API
const USE_NOTIFICATION_API = true   // 通知 API 已实现
const USE_MESSAGE_API = true       // 消息 API 暂未实现，使用模拟数据

export const useMessageStore = defineStore('message', () => {
  // 会话/联系人列表
  const contacts = ref([])

  // 通知列表
  const notifications = ref([])

  // 加载状态
  const loading = ref(false)
  const error = ref(null)

  // 计算属性
  const totalUnreadMessages = computed(() => {
    return contacts.value.reduce((sum, c) => sum + (c.unread || 0), 0)
  })

  const totalUnreadNotifications = computed(() => {
    return notifications.value.filter(n => !n.read).length
  })

  // ========================================
  // 通知相关方法
  // ========================================

  // 获取通知列表
  async function fetchNotifications() {
    if (!USE_NOTIFICATION_API) {
      // 使用模拟数据
      notifications.value = getMockNotifications()
      return
    }

    loading.value = true
    try {
      const res = await notificationApi.getNotifications()
      if (res.data) {
        // 转换后端数据格式
        notifications.value = (res.data.records || res.data || []).map(transformNotification)
      }
    } catch (e) {
      console.error('获取通知失败:', e)
      error.value = e.message
      // 失败时使用空数组
      notifications.value = []
    } finally {
      loading.value = false
    }
  }

  // 标记通知已读
  async function markNotificationAsRead(notificationId) {
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.read = true

      if (USE_NOTIFICATION_API) {
        try {
          await notificationApi.markAsRead(notificationId)
        } catch (e) {
          console.error('标记通知已读失败:', e)
        }
      }
    }
  }

  // 标记所有通知已读
  async function markAllNotificationsAsRead() {
    notifications.value.forEach(n => n.read = true)

    if (USE_NOTIFICATION_API) {
      try {
        await notificationApi.markAllAsRead()
      } catch (e) {
        console.error('标记全部已读失败:', e)
      }
    }
  }

  // ========================================
  // 消息/会话相关方法
  // ========================================

  // 获取会话列表
  async function fetchConversations() {
    if (!USE_MESSAGE_API) {
      // 使用模拟数据
      contacts.value = getMockContacts()
      return
    }

    loading.value = true
    try {
      const res = await messageApi.getConversations()
      if (res.data) {
        // 转换后端数据格式
        contacts.value = (res.data.records || res.data || []).map(transformConversation)
      }
    } catch (e) {
      console.error('获取会话列表失败:', e)
      error.value = e.message
      contacts.value = []
    } finally {
      loading.value = false
    }
  }

  // 标记会话已读
  async function markContactAsRead(contactId) {
    const contact = contacts.value.find(c => c.id === contactId)
    if (contact) {
      contact.unread = 0

      if (USE_MESSAGE_API) {
        try {
          await messageApi.markConversationRead(contactId)
        } catch (e) {
          console.error('标记会话已读失败:', e)
        }
      }
    }
  }

  // 标记所有消息已读
  function markAllMessagesAsRead() {
    contacts.value.forEach(c => c.unread = 0)
  }

  // 添加联系人
  function addContact(contact) {
    if (!contacts.value.find(c => c.id === contact.id)) {
      contacts.value.unshift(contact)
    }
  }

  // 更新联系人最后消息
  function updateContactLastMessage(contactId, message, sender = '') {
    const contact = contacts.value.find(c => c.id === contactId)
    if (contact) {
      contact.lastMessage = message
      contact.time = '刚刚'
      if (contact.isGroup && sender) contact.lastSender = sender
    }
  }

  // ========================================
  // 数据转换函数
  // ========================================

  // 转换后端通知数据格式
  function transformNotification(item) {
    return {
      id: item.id,
      title: item.senderName || item.title || '系统通知',
      content: item.content || item.message || '',
      time: formatTime(item.createdAt || item.createTime),
      read: item.isRead || item.read || false,
      type: mapNotificationType(item.type),
      avatar: item.senderAvatar || item.avatar || null,
      postTitle: item.postTitle || item.relatedTitle || null,
      isFollowing: item.isFollowing || false
    }
  }

  // 转换后端会话数据格式
  function transformConversation(item) {
    return {
      id: item.id,
      name: item.name || item.targetName || '未知用户',
      avatar: item.avatar || item.targetAvatar || null,
      targetId: item.targetId || item.targetUserId || null, // 添加对方用户 ID
      verifyType: item.targetVerifyType || null, // 添加认证类型
      lastMessage: item.lastMessage || item.latestContent || '',
      time: formatTime(item.updatedAt || item.updateTime),
      unread: item.unreadCount || item.unread || 0,
      isGroup: item.isGroup || item.type === 'GROUP',
      memberCount: item.memberCount || 0,
      lastSender: item.lastSenderName || '',
      color: item.isGroup ? 'bg-blue-500' : null,
      members: item.members || [],
      announcement: item.announcement || ''
    }
  }

  // 映射通知类型
  function mapNotificationType(type) {
    const typeMap = {
      'LIKE': 'like',
      'COMMENT': 'comment',
      'FOLLOW': 'follow',
      'MENTION': 'mention',
      'SYSTEM': 'system',
      'EVENT': 'event',
      'ALERT': 'alert'
    }
    return typeMap[type] || type?.toLowerCase() || 'system'
  }

  // 格式化时间
  function formatTime(dateStr) {
    if (!dateStr) return ''

    const date = new Date(dateStr)
    const now = new Date()
    const diff = now - date

    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)

    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`

    return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
  }

  // ========================================
  // 模拟数据（当 API 不可用时的后备）
  // ========================================

  function getMockNotifications() {
    return [
      { id: 1, title: '张明', content: '赞了你的帖子', time: '10分钟前', read: false, type: 'like', avatar: 'https://picsum.photos/seed/n1/40', postTitle: '今天的校园风景真美' },
      { id: 2, title: '李伊桑', content: '评论了你的帖子："写得太好了！"', time: '30分钟前', read: false, type: 'comment', avatar: 'https://picsum.photos/seed/n2/40', postTitle: '分享一下学习心得' },
      { id: 3, title: '王奥莉', content: '关注了你', time: '1小时前', read: false, type: 'follow', avatar: 'https://picsum.photos/seed/n3/40', isFollowing: false },
      { id: 4, title: '教务处', content: '发布了新的考试安排通知，请查看', time: '2小时前', read: false, type: 'system' },
      { id: 5, title: '陈思思', content: '在评论中@了你', time: '3小时前', read: true, type: 'mention', avatar: 'https://picsum.photos/seed/n4/40' }
    ]
  }

  function getMockContacts() {
    return [
      { id: '1', name: '张明', avatar: 'https://picsum.photos/seed/msg1/40', lastMessage: '好的，我已经看到你的帖子了', time: '10:30', unread: 2, isGroup: false },
      { id: '2', name: '李伊桑', avatar: 'https://picsum.photos/seed/user2/40', lastMessage: '周末一起去逛校园吗？', time: '2小时前', unread: 1, isGroup: false },
      { id: '3', name: '王奥莉', avatar: 'https://picsum.photos/seed/user3/40', lastMessage: '那个活动报名链接?', time: '昨天', unread: 0, isGroup: false }
    ]
  }

  return {
    // 状态
    contacts,
    notifications,
    loading,
    error,

    // 计算属性
    totalUnreadMessages,
    totalUnreadNotifications,

    // 通知方法
    fetchNotifications,
    markNotificationAsRead,
    markAllNotificationsAsRead,

    // 消息方法
    fetchConversations,
    markContactAsRead,
    markAllMessagesAsRead,
    addContact,
    updateContactLastMessage
  }
})
