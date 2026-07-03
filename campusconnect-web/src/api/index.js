import request, { withRetry } from './request'

// ========================================
// 文件上传 API
// ========================================
export const fileApi = {
  // 上传文件
  upload(file, type = 'image') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)
    // 不设置 Content-Type，让浏览器自动设置 boundary
    return request.post('/file/upload', formData)
  }
}

// ========================================
// 认证相关 API
// ========================================
export const authApi = {
  // 登录
  login(data) {
    return request.post('/auth/login', data)
  },
  // 注册
  register(data) {
    return request.post('/auth/register', data)
  },
  // 退出登录
  logout() {
    return request.post('/auth/logout')
  },
  // 刷新 Token
  refreshToken(refreshToken) {
    return request.post('/auth/refresh', { refreshToken })
  },
  // 发送验证码
  sendVerifyCode(phone) {
    return request.post('/auth/send-code', { phone })
  },
  // 验证码登录
  loginWithCode(data) {
    return request.post('/auth/login-code', data)
  },
  // 重置密码
  resetPassword(data) {
    return request.post('/auth/reset-password', data)
  },
  // 修改密码
  changePassword(data) {
    return request.put('/auth/change-password', data)
  }
}

// ========================================
// 用户相关 API
// ========================================
export const userApi = {
  // 获取当前用户信息
  getCurrentUser() {
    return request.get('/user/me')
  },
  // 更新用户信息
  updateUser(data) {
    return request.put('/user/me', data)
  },
  // 更新用户头像
  updateAvatar(avatarUrl) {
    return request.put('/user/me/avatar', { avatar: avatarUrl })
  },
  // 更新封面图片
  updateCover(coverUrl) {
    return request.put('/user/me/cover', { coverImage: coverUrl })
  },
  // 获取用户详情
  getUserById(id) {
    return request.get(`/user/${id}`)
  },
  // 搜索用户
  searchUsers(keyword, params = {}) {
    return request.get('/user/search', { params: { keyword, ...params } })
  },
  // 关注用户
  followUser(userId) {
    return request.post(`/user/${userId}/follow`)
  },
  // 取消关注
  unfollowUser(userId) {
    return request.delete(`/user/${userId}/follow`)
  },
  // 获取关注列表
  getFollowing(userId, params = {}) {
    return request.get(`/user/${userId}/following`, { params })
  },
  // 获取粉丝列表
  getFollowers(userId, params = {}) {
    return request.get(`/user/${userId}/followers`, { params })
  },
  // 检查是否关注某用户
  isFollowing(userId) {
    return request.get(`/user/${userId}/is-following`)
  },
  // 更新隐私设置
  updatePrivacy(data) {
    return request.put('/user/me/privacy', data)
  },
  // 更新通知设置
  updateSettings(data) {
    return request.put('/user/me/settings', data)
  },
  // 绑定手机
  bindPhone(data) {
    return request.post('/user/me/bindPhone', data)
  },
  // 绑定邮箱
  bindEmail(data) {
    return request.post('/user/me/bindEmail', data)
  }
}

// ========================================
// 帖子相关 API
// ========================================
export const postApi = {
  // 获取帖子列表
  getPosts(params = {}) {
    return request.get('/posts', { params })
  },
  // 获取热门帖子
  getHotPosts(params = {}) {
    return request.get('/posts/hot', { params })
  },
  // 获取推荐帖子
  getRecommendedPosts(params = {}) {
    return request.get('/posts/recommended', { params })
  },
  // 获取用户帖子
  getUserPosts(userId, params = {}) {
    return request.get(`/user/${userId}/posts`, { params })
  },
  // 获取单个帖子
  getPostById(id) {
    return request.get(`/posts/${id}`)
  },
  // 创建帖子
  createPost(data) {
    return request.post('/posts', data)
  },
  // 更新帖子
  updatePost(id, data) {
    return request.put(`/posts/${id}`, data)
  },
  // 删除帖子
  deletePost(id) {
    return request.delete(`/posts/${id}`)
  },
  // 点赞帖子
  likePost(id) {
    return request.post(`/posts/${id}/like`)
  },
  // 取消点赞
  unlikePost(id) {
    return request.delete(`/posts/${id}/like`)
  },
  // 收藏帖子
  collectPost(id) {
    return request.post(`/posts/${id}/collect`)
  },
  // 取消收藏
  uncollectPost(id) {
    return request.delete(`/posts/${id}/collect`)
  },
  // 分享帖子
  sharePost(id) {
    return request.post(`/posts/${id}/share`)
  },
  // 搜索帖子
  searchPosts(keyword, params = {}) {
    return request.get('/posts/search', { params: { keyword, ...params } })
  },
  // 获取标签列表
  getTags() {
    return request.get('/posts/tags')
  },
  // 按标签获取帖子
  getPostsByTag(tag, params = {}) {
    return request.get(`/posts/tag/${tag}`, { params })
  }
}

// ========================================
// 评论相关 API
// ========================================
export const commentApi = {
  // 获取帖子评论
  getComments(postId, params = {}) {
    return request.get(`/posts/${postId}/comments`, { params })
  },
  // 添加评论
  addComment(postId, data) {
    return request.post(`/posts/${postId}/comments`, data)
  },
  // 回复评论
  replyComment(postId, commentId, data) {
    return request.post(`/posts/${postId}/comments/${commentId}/reply`, data)
  },
  // 删除评论
  deleteComment(postId, commentId) {
    return request.delete(`/posts/${postId}/comments/${commentId}`)
  },
  // 点赞评论
  likeComment(postId, commentId) {
    return request.post(`/posts/${postId}/comments/${commentId}/like`)
  },
  // 取消点赞评论
  unlikeComment(postId, commentId) {
    return request.delete(`/posts/${postId}/comments/${commentId}/like`)
  }
}

// ========================================
// 通知相关 API
// ========================================
export const notificationApi = {
  // 获取通知列表
  getNotifications(params = {}) {
    return request.get('/notifications', { params })
  },
  // 获取未读数量
  getUnreadCount() {
    return request.get('/notifications/unread-count')
  },
  // 标记已读
  markAsRead(id) {
    return request.put(`/notifications/${id}/read`)
  },
  // 标记全部已读
  markAllAsRead() {
    return request.put('/notifications/read-all')
  },
  // 删除通知
  deleteNotification(id) {
    return request.delete(`/notifications/${id}`)
  },
  // 清空通知
  clearNotifications() {
    return request.delete('/notifications/clear')
  }
}

// ========================================
// 消息相关 API
// ========================================
export const messageApi = {
  // 获取会话列表
  getConversations(params = {}) {
    return request.get('/messages/conversations', { params })
  },
  // 获取会话详情
  getConversationDetail(conversationId) {
    return request.get(`/messages/conversations/${conversationId}`)
  },
  // 获取消息历史
  getMessages(conversationId, params = {}) {
    return request.get(`/messages/conversations/${conversationId}/messages`, { params })
  },
  // 发送消息
  sendMessage(data) {
    return request.post('/messages', data)
  },
  // 标记会话已读
  markConversationRead(conversationId) {
    return request.put(`/messages/conversations/${conversationId}/read`)
  },
  // 删除会话
  deleteConversation(conversationId) {
    return request.delete(`/messages/conversations/${conversationId}`)
  },
  // 获取未读消息数
  getUnreadCount() {
    return request.get('/messages/unread-count')
  },
  // 创建群聊
  createGroup(data) {
    return request.post('/messages/groups', data)
  },
  // 获取/创建私聊会话
  getOrCreatePrivateConversation(targetUserId) {
    return request.post(`/messages/private/${targetUserId}`)
  }
}

// ========================================
// 身份认证申请 API
// ========================================
export const verificationApi = {
  // 获取认证状态
  getStatus() {
    return request.get('/user/me/verification')
  },
  // 提交认证申请
  submit(data) {
    return request.post('/user/me/verification', data)
  },
  // 获取申请历史
  getHistory() {
    return request.get('/user/me/verification/history')
  }
}

// ========================================
// 文件上传 API
// ========================================
export const uploadApi = {
  // 上传单个图片
  uploadImage(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress ? (e) => onProgress(Math.round((e.loaded * 100) / e.total)) : undefined
    })
  },
  // 批量上传图片
  uploadImages(files, onProgress) {
    const formData = new FormData()
    files.forEach((file, index) => {
      formData.append('files', file)
    })
    return request.post('/upload/images', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress ? (e) => onProgress(Math.round((e.loaded * 100) / e.total)) : undefined
    })
  },
  // 上传文件 (PDF等)
  uploadFile(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: onProgress ? (e) => onProgress(Math.round((e.loaded * 100) / e.total)) : undefined
    })
  },
  // 获取上传凭证 (用于OSS直传)
  getUploadToken(type = 'image') {
    return request.get('/upload/token', { params: { type } })
  }
}

// ========================================
// 活动相关 API
// ========================================
export const eventApi = {
  // 获取活动列表
  getEvents(params = {}) {
    return request.get('/events', { params })
  },
  // 获取活动详情
  getEventById(id) {
    return request.get(`/events/${id}`)
  },
  // 报名活动
  registerEvent(id) {
    return request.post(`/events/${id}/register`)
  },
  // 取消报名
  unregisterEvent(id) {
    return request.delete(`/events/${id}/register`)
  },
  // 获取已报名活动
  getMyEvents(params = {}) {
    return request.get('/events/my', { params })
  }
}

// ========================================
// 自习室相关 API
// ========================================
export const studyRoomApi = {
  // 获取自习室列表
  getRooms(params = {}) {
    return request.get('/study-rooms', { params })
  },
  // 获取自习室详情
  getRoomById(id) {
    return request.get(`/study-rooms/${id}`)
  },
  // 获取实时人数
  getRoomStatus(id) {
    return withRetry(() => request.get(`/study-rooms/${id}/status`), 2)()
  },
  // 签到
  checkIn(id) {
    return request.post(`/study-rooms/${id}/check-in`)
  },
  // 签退
  checkOut(id) {
    return request.post(`/study-rooms/${id}/check-out`)
  }
}

// ========================================
// 校园地图 API
// ========================================
export const mapApi = {
  // 获取地点列表
  getLocations(params = {}) {
    return request.get('/map/locations', { params })
  },
  // 搜索地点
  searchLocations(keyword) {
    return request.get('/map/search', { params: { keyword } })
  },
  // 获取地点详情
  getLocationById(id) {
    return request.get(`/map/locations/${id}`)
  },
  // 标记会话已读
  markConversationRead(conversationId) {
    return request.put(`/chat/conversations/${conversationId}/read`)
  }
}

// ========================================
// 失物招领 API
// ========================================
export const lostFoundApi = {
  // 获取列表
  getItems(params = {}) {
    return request.get('/lost-found', { params })
  },
  // 发布信息
  createItem(data) {
    return request.post('/lost-found', data)
  },
  // 更新信息
  updateItem(id, data) {
    return request.put(`/lost-found/${id}`, data)
  },
  // 标记已找到/已认领
  markResolved(id) {
    return request.put(`/lost-found/${id}/resolve`)
  },
  // 删除
  deleteItem(id) {
    return request.delete(`/lost-found/${id}`)
  }
}

// ========================================
// AI 服务 API
// ========================================
export const aiApi = {
  // 生成内容
  generateContent(prompt, type = 'polish') {
    return request.post('/ai/generate', { prompt, type })
  },
  // AI 对话
  chat(messages) {
    return request.post('/ai/chat', { messages })
  }
}

// ========================================
// 系统配置 API
// ========================================
export const systemApi = {
  // 获取系统配置
  getConfig() {
    return request.get('/system/config')
  },
  // 获取公告
  getAnnouncements() {
    return request.get('/system/announcements')
  },
  // 反馈
  submitFeedback(data) {
    return request.post('/system/feedback', data)
  }
}
// ========================================
// 聊天室 API
// ========================================
export const chatApi = {
  // 获取聊天室列表
  getConversations() {
    return request.get('/chat/conversations')
  },

  // 获取某个聊天室历史消息
  getMessages(conversationId) {
    return request.get(`/chat/conversations/${conversationId}/messages`)
  },

  // 发送聊天消息
  sendMessage(data) {
    return request.post('/chat/messages', data)
  },
  // 标记聊天室已读
  markConversationRead(conversationId) {
    return request.put(`/chat/conversations/${conversationId}/read`)
  }
}
