// ========================================
// 枚举类型定义
// ========================================

// 用户角色
export const UserRole = {
  STUDENT: 'student',
  TEACHER: 'teacher',
  DEPARTMENT: 'department',
  ADMIN: 'admin'
}

// 认证状态
export const VerificationStatus = {
  NONE: 'none',
  PENDING: 'pending',
  APPROVED: 'approved',
  REJECTED: 'rejected'
}

// 帖子状态
export const PostStatus = {
  DRAFT: 'draft',
  PUBLISHED: 'published',
  HIDDEN: 'hidden',
  DELETED: 'deleted'
}

// 帖子类型
export const PostType = {
  NORMAL: 'normal',
  ANNOUNCEMENT: 'announcement',
  ACTIVITY: 'activity',
  LOST_FOUND: 'lost_found',
  SECOND_HAND: 'second_hand'
}

// 通知类型
export const NotificationType = {
  LIKE: 'like',
  COMMENT: 'comment',
  FOLLOW: 'follow',
  MENTION: 'mention',
  SYSTEM: 'system',
  EVENT: 'event',
  ALERT: 'alert'
}

// 消息类型
export const MessageType = {
  TEXT: 'text',
  IMAGE: 'image',
  FILE: 'file',
  SYSTEM: 'system'
}

// 会话类型
export const ConversationType = {
  PRIVATE: 'private',
  GROUP: 'group'
}

// 群组成员角色
export const GroupMemberRole = {
  OWNER: 'owner',
  ADMIN: 'admin',
  MEMBER: 'member'
}

// 活动状态
export const EventStatus = {
  UPCOMING: 'upcoming',
  ONGOING: 'ongoing',
  ENDED: 'ended',
  CANCELLED: 'cancelled'
}

// 自习室状态
export const StudyRoomStatus = {
  AVAILABLE: 'available',
  CROWDED: 'crowded',
  FULL: 'full',
  CLOSED: 'closed'
}

// 模态框类型
export const ModalType = {
  LOGIN: 'LOGIN',
  REGISTER: 'REGISTER',
  PROFILE: 'PROFILE',
  CREATE_POST: 'CREATE_POST',
  POST_DETAIL: 'POST_DETAIL',
  NOTIFICATIONS: 'NOTIFICATIONS_FULL',
  MESSAGES: 'MESSAGES_FULL'
}

// ========================================
// API 响应格式
// ========================================

// 统一响应结构
export const ApiResponseCode = {
  SUCCESS: 200,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500
}

// ========================================
// 数据模型工厂函数
// ========================================

// 创建用户对象
export function createUser(data = {}) {
  return {
    id: data.id || null,
    name: data.name || '',
    username: data.username || '',
    avatar: data.avatar || '',
    email: data.email || '',
    phone: data.phone || '',
    role: data.role || UserRole.STUDENT,
    isVerified: data.isVerified || false,
    verificationStatus: data.verificationStatus || VerificationStatus.NONE,
    bio: data.bio || '',
    college: data.college || '',
    major: data.major || '',
    className: data.className || '',
    dormitory: data.dormitory || '',
    studentId: data.studentId || '',
    gender: data.gender || '',
    age: data.age || null,
    hobbies: data.hobbies || [],
    followers: data.followers || [],
    following: data.following || [],
    followerCount: data.followerCount || 0,
    followingCount: data.followingCount || 0,
    postCount: data.postCount || 0,
    coverImage: data.coverImage || '',
    privacy: {
      age: data.privacy?.age || false,
      gender: data.privacy?.gender || false,
      className: data.privacy?.className || false,
      dormitory: data.privacy?.dormitory || false
    },
    settings: {
      allowStrangerMessage: data.settings?.allowStrangerMessage ?? true,
      showOnlineStatus: data.settings?.showOnlineStatus ?? true,
      showFollowList: data.settings?.showFollowList ?? true,
      notifyLike: data.settings?.notifyLike ?? true,
      notifyComment: data.settings?.notifyComment ?? true,
      notifyFollow: data.settings?.notifyFollow ?? true,
      notifySystem: data.settings?.notifySystem ?? true
    },
    createdAt: data.createdAt || null,
    updatedAt: data.updatedAt || null
  }
}

// 创建帖子对象
export function createPost(data = {}) {
  return {
    id: data.id || null,
    title: data.title || '',
    content: data.content || '',
    images: data.images || [],
    tags: data.tags || [],
    type: data.type || PostType.NORMAL,
    status: data.status || PostStatus.PUBLISHED,
    user: data.user || null,
    likes: data.likes || 0,
    comments: data.comments || 0,
    shares: data.shares || 0,
    views: data.views || 0,
    isLiked: data.isLiked || false,
    isCollected: data.isCollected || false,
    timeAgo: data.timeAgo || '',
    createdAt: data.createdAt || null,
    updatedAt: data.updatedAt || null
  }
}

// 创建评论对象
export function createComment(data = {}) {
  return {
    id: data.id || null,
    content: data.content || '',
    user: data.user || null,
    postId: data.postId || null,
    parentId: data.parentId || null,
    replyTo: data.replyTo || null,
    likes: data.likes || 0,
    isLiked: data.isLiked || false,
    timeAgo: data.timeAgo || '',
    createdAt: data.createdAt || null
  }
}

// 创建通知对象
export function createNotification(data = {}) {
  return {
    id: data.id || null,
    type: data.type || NotificationType.SYSTEM,
    title: data.title || '',
    content: data.content || '',
    avatar: data.avatar || '',
    read: data.read || false,
    postId: data.postId || null,
    postTitle: data.postTitle || '',
    userId: data.userId || null,
    isFollowing: data.isFollowing || false,
    time: data.time || '',
    createdAt: data.createdAt || null
  }
}

// 创建消息对象
export function createMessage(data = {}) {
  return {
    id: data.id || null,
    type: data.type || MessageType.TEXT,
    content: data.content || '',
    senderId: data.senderId || null,
    sender: data.sender || '',
    isMine: data.isMine || false,
    isSystem: data.isSystem || false,
    time: data.time || '',
    createdAt: data.createdAt || null
  }
}

// 创建会话/联系人对象
export function createConversation(data = {}) {
  return {
    id: data.id || null,
    name: data.name || '',
    avatar: data.avatar || '',
    color: data.color || '',
    isGroup: data.isGroup || false,
    lastMessage: data.lastMessage || '',
    lastSender: data.lastSender || '',
    time: data.time || '',
    unread: data.unread || 0,
    memberCount: data.memberCount || 0,
    ownerId: data.ownerId || null,
    announcement: data.announcement || '',
    members: data.members || []
  }
}

// 创建活动对象
export function createEvent(data = {}) {
  return {
    id: data.id || null,
    title: data.title || '',
    description: data.description || '',
    image: data.image || '',
    location: data.location || '',
    startTime: data.startTime || null,
    endTime: data.endTime || null,
    organizer: data.organizer || '',
    participants: data.participants || 0,
    maxParticipants: data.maxParticipants || null,
    status: data.status || EventStatus.UPCOMING,
    isRegistered: data.isRegistered || false,
    tags: data.tags || [],
    createdAt: data.createdAt || null
  }
}

// 创建认证申请对象
export function createVerificationRequest(data = {}) {
  return {
    id: data.id || null,
    type: data.type || UserRole.TEACHER,
    identifier: data.identifier || '',
    department: data.department || '',
    title: data.title || '',
    phone: data.phone || '',
    files: data.files || [],
    reason: data.reason || '',
    status: data.status || VerificationStatus.PENDING,
    rejectReason: data.rejectReason || '',
    createdAt: data.createdAt || null,
    reviewedAt: data.reviewedAt || null
  }
}

// ========================================
// 工具函数
// ========================================

// 格式化时间为相对时间
export function formatTimeAgo(date) {
  if (!date) return ''
  const now = new Date()
  const past = new Date(date)
  const diffMs = now - past
  const diffSec = Math.floor(diffMs / 1000)
  const diffMin = Math.floor(diffSec / 60)
  const diffHour = Math.floor(diffMin / 60)
  const diffDay = Math.floor(diffHour / 24)

  if (diffSec < 60) return '刚刚'
  if (diffMin < 60) return `${diffMin}分钟前`
  if (diffHour < 24) return `${diffHour}小时前`
  if (diffDay < 7) return `${diffDay}天前`
  return past.toLocaleDateString('zh-CN')
}

// 格式化数字 (1000 -> 1k)
export function formatNumber(num) {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

// 截断文本
export function truncateText(text, maxLength = 100) {
  if (!text || text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

// 移除 HTML 标签
export function stripHtml(html) {
  return html?.replace(/<[^>]+>/g, '') || ''
}

// 验证邮箱格式
export function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

// 验证手机号格式
export function isValidPhone(phone) {
  return /^1[3-9]\d{9}$/.test(phone)
}

// 验证学号/工号格式
export function isValidStudentId(id) {
  return /^\d{10,12}$/.test(id)
}
