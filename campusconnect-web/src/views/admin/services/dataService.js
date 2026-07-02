import request from '../../../api/request'
import {
  MOCK_USERS,
  MOCK_POSTS,
  MOCK_REPORTS,
  MOCK_VERIFICATIONS,
  MOCK_ANNOUNCEMENTS,
  MOCK_NOTIFICATIONS,
  MOCK_ACTIVITIES
} from '../constants'

// 配置：设为true使用后端API，false使用本地模拟数据
const USE_REAL_API = true

// 模拟网络延迟
const LATENCY = 400
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

// 数据库键（本地模式使用）
const DB_KEYS = {
  USERS: 'cp_db_users',
  POSTS: 'cp_db_posts',
  REPORTS: 'cp_db_reports',
  VERIFICATIONS: 'cp_db_verifications',
  ANNOUNCEMENTS: 'cp_db_announcements',
  ACTIVITIES: 'cp_db_activities',
  NOTIFICATIONS: 'cp_db_notifications',
  ADMIN_PROFILE: 'cp_db_admin_profile'
}

// 辅助函数
const getFromDB = (key) => {
  const data = localStorage.getItem(key)
  return data ? JSON.parse(data) : []
}

const saveToDB = (key, data) => {
  localStorage.setItem(key, JSON.stringify(data))
}

export const dataService = {
  // 初始化数据库
  initDB() {
    if (USE_REAL_API) return

    if (!localStorage.getItem(DB_KEYS.USERS)) saveToDB(DB_KEYS.USERS, MOCK_USERS)
    if (!localStorage.getItem(DB_KEYS.POSTS)) saveToDB(DB_KEYS.POSTS, MOCK_POSTS)
    if (!localStorage.getItem(DB_KEYS.REPORTS)) saveToDB(DB_KEYS.REPORTS, MOCK_REPORTS)
    if (!localStorage.getItem(DB_KEYS.VERIFICATIONS)) saveToDB(DB_KEYS.VERIFICATIONS, MOCK_VERIFICATIONS)
    if (!localStorage.getItem(DB_KEYS.ANNOUNCEMENTS)) saveToDB(DB_KEYS.ANNOUNCEMENTS, MOCK_ANNOUNCEMENTS)
    if (!localStorage.getItem(DB_KEYS.ACTIVITIES)) saveToDB(DB_KEYS.ACTIVITIES, MOCK_ACTIVITIES)
    if (!localStorage.getItem(DB_KEYS.NOTIFICATIONS)) saveToDB(DB_KEYS.NOTIFICATIONS, MOCK_NOTIFICATIONS)

    if (!localStorage.getItem(DB_KEYS.ADMIN_PROFILE)) {
      saveToDB(DB_KEYS.ADMIN_PROFILE, {
        name: 'Admin',
        role: '管理员',
        avatar: 'https://picsum.photos/100/100?random=100'
      })
    }
  },

  // 管理员登录
  async login(username, password) {
    if (USE_REAL_API) {
      // 先保存当前前台用户的完整状态（token + user 数据）
      const currentToken = localStorage.getItem('token')
      const currentRefreshToken = localStorage.getItem('refreshToken')
      const currentUser = localStorage.getItem('user')
      const savedFrontendToken = localStorage.getItem('frontendUserToken')

      // 如果当前有 token 且没有保存过前台备份，则保存完整状态
      if (currentToken && !savedFrontendToken) {
        localStorage.setItem('frontendUserToken', currentToken)
        if (currentRefreshToken) {
          localStorage.setItem('frontendUserRefreshToken', currentRefreshToken)
        }
        if (currentUser) {
          localStorage.setItem('frontendUser', currentUser)
        }
      }

      const res = await request.post('/auth/login', { username, password })
      // 使用单独的 key 存储管理员 token
      localStorage.setItem('adminToken', res.data.token)
      localStorage.setItem('adminRefreshToken', res.data.refreshToken)
      // 同时设置 token 以便 request 拦截器使用
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      return res.data
    }
    await delay(LATENCY)
    return { token: 'mock-token', user: { name: 'Admin', role: '管理员' } }
  },

  // 管理员资料
  async fetchAdminProfile() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/profile')
      return res.data
    }
    await delay(LATENCY)
    const profile = localStorage.getItem(DB_KEYS.ADMIN_PROFILE)
    return profile ? JSON.parse(profile) : { name: 'Error', role: 'Error', avatar: '' }
  },

  async updateAdminProfile(profile) {
    if (USE_REAL_API) {
      await request.put('/admin/profile', profile)
      return profile
    }
    await delay(LATENCY)
    saveToDB(DB_KEYS.ADMIN_PROFILE, profile)
    return profile
  },

  // 仪表盘统计
  async fetchDashboardStats() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/dashboard/stats')
        console.log('fetchDashboardStats API响应:', res)
        return res.data || res || {}
      } catch (e) {
        console.error('fetchDashboardStats 失败:', e)
        return { userCount: 0, postCount: 0, pendingVerifications: 0, pendingReports: 0 }
      }
    }
    await delay(LATENCY)
    const posts = getFromDB(DB_KEYS.POSTS)
    const verifications = getFromDB(DB_KEYS.VERIFICATIONS)
    return {
      visits: 12405 + Math.floor(Math.random() * 500),
      posts: posts.length,
      pendingVerifications: verifications.filter(v => v.status === '待审核').length,
      systemHealth: '99.8%'
    }
  },

  async fetchActivityTrend() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/dashboard/activity-trend')
        return res.data || []
      } catch (e) {
        console.error('fetchActivityTrend 失败:', e)
        return []
      }
    }
    await delay(LATENCY)
    // 模拟数据
    const hours = []
    const now = new Date()
    for (let i = 6; i >= 0; i--) {
      const d = new Date(now.getTime() - i * 60 * 60 * 1000)
      hours.push({
        name: d.getHours().toString().padStart(2, '0') + ':00',
        visits: Math.floor(Math.random() * 5000),
        posts: Math.floor(Math.random() * 500)
      })
    }
    return hours
  },

  async fetchContentStats() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/dashboard/content-stats')
        return res.data || []
      } catch (e) {
        console.error('fetchContentStats 失败:', e)
        return []
      }
    }
    await delay(LATENCY)
    return [
      { name: '学习', posts: 150 },
      { name: '生活', posts: 120 },
      { name: '活动', posts: 80 },
      { name: '二手', posts: 60 },
      { name: '失物招领', posts: 40 }
    ]
  },

  // 用户管理
  async fetchUsers() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/users')
        console.log('fetchUsers API响应:', res)
        return res.data || res || []
      } catch (e) {
        console.error('fetchUsers 失败:', e)
        return []
      }
    }
    await delay(LATENCY)
    return getFromDB(DB_KEYS.USERS)
  },

  async updateUser(user) {
    if (USE_REAL_API) {
      await request.put(`/admin/users/${user.id}`, user)
      return
    }
    await delay(LATENCY)
    const users = getFromDB(DB_KEYS.USERS)
    const index = users.findIndex(u => u.id === user.id)
    if (index !== -1) {
      users[index] = user
      saveToDB(DB_KEYS.USERS, users)
    }
  },

  // 帖子管理
  async fetchPosts() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/posts')
        console.log('fetchPosts API响应:', res)
        return res.data?.records || res.data || res.records || res || []
      } catch (e) {
        console.error('fetchPosts 失败:', e)
        return []
      }
    }
    await delay(LATENCY)
    return getFromDB(DB_KEYS.POSTS)
  },

  async deletePost(id) {
    if (USE_REAL_API) {
      await request.delete(`/admin/posts/${id}`)
      return
    }
    await delay(LATENCY)
    let posts = getFromDB(DB_KEYS.POSTS)
    posts = posts.filter(p => p.id !== id)
    saveToDB(DB_KEYS.POSTS, posts)
  },

  async togglePostPin(id) {
    if (USE_REAL_API) {
      await request.post(`/admin/posts/${id}/pin`)
      return
    }
    await delay(200)
    const posts = getFromDB(DB_KEYS.POSTS)
    const post = posts.find(p => p.id === id)
    if (post) {
      post.isPinned = !post.isPinned
      saveToDB(DB_KEYS.POSTS, posts)
    }
  },

  async moderatePost(postId, action) {
    if (USE_REAL_API) {
      await request.post(`/admin/posts/${postId}/moderate`, { action })
      return
    }
    await delay(LATENCY)
    const posts = getFromDB(DB_KEYS.POSTS)
    const post = posts.find(p => p.id === postId)
    if (post) {
      post.status = action === 'approve' ? '已发布' : '被标记'
      saveToDB(DB_KEYS.POSTS, posts)
    }
  },

  // 身份认证
  async fetchVerifications() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/verifications')
      return res.data
    }
    await delay(LATENCY)
    return getFromDB(DB_KEYS.VERIFICATIONS)
  },

  async reviewVerification(id, status) {
    if (USE_REAL_API) {
      await request.post(`/admin/verifications/${id}/review`, { status })
      return
    }
    await delay(LATENCY)
    const reqs = getFromDB(DB_KEYS.VERIFICATIONS)
    const req = reqs.find(v => v.id === id)
    if (req) {
      req.status = status
      saveToDB(DB_KEYS.VERIFICATIONS, reqs)
    }
  },

  // 举报管理
  async fetchReports() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/reports')
      return res.data
    }
    await delay(LATENCY)
    return getFromDB(DB_KEYS.REPORTS)
  },

  async resolveReport(id, action) {
    if (USE_REAL_API) {
      await request.post(`/admin/reports/${id}/resolve`, { action })
      return
    }
    await delay(LATENCY)
    const reports = getFromDB(DB_KEYS.REPORTS)
    const report = reports.find(r => r.id === id)
    if (report) {
      report.status = '已解决'
      saveToDB(DB_KEYS.REPORTS, reports)
      if (action === 'punish' && report.targetType === '帖子') {
        let posts = getFromDB(DB_KEYS.POSTS)
        posts = posts.filter(p => p.id !== report.targetId)
        saveToDB(DB_KEYS.POSTS, posts)
      }
    }
  },

  // 公告管理
  async fetchAnnouncements() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/announcements')
      return res.data
    }
    await delay(300)
    return getFromDB(DB_KEYS.ANNOUNCEMENTS)
  },

  async saveAnnouncement(announcement) {
    if (USE_REAL_API) {
      if (announcement.id) {
        await request.put(`/admin/announcements/${announcement.id}`, announcement)
      } else {
        const res = await request.post('/admin/announcements', announcement)
        return res.data
      }
      return announcement
    }
    await delay(LATENCY)
    const list = getFromDB(DB_KEYS.ANNOUNCEMENTS)
    const index = list.findIndex(a => a.id === announcement.id)
    if (index !== -1) {
      list[index] = announcement
    } else {
      list.unshift(announcement)
    }
    saveToDB(DB_KEYS.ANNOUNCEMENTS, list)
  },

  async deleteAnnouncement(id) {
    if (USE_REAL_API) {
      await request.delete(`/admin/announcements/${id}`)
      return
    }
    await delay(LATENCY)
    let list = getFromDB(DB_KEYS.ANNOUNCEMENTS)
    list = list.filter(a => a.id !== id)
    saveToDB(DB_KEYS.ANNOUNCEMENTS, list)
  },

  async toggleAnnouncementPin(id) {
    if (USE_REAL_API) {
      await request.post(`/admin/announcements/${id}/pin`)
      return
    }
    await delay(LATENCY)
  },

  // 活动管理
  async fetchActivities() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/activities')
      return res.data
    }
    await delay(300)
    return getFromDB(DB_KEYS.ACTIVITIES)
  },

  async saveActivity(activity) {
    if (USE_REAL_API) {
      if (activity.id) {
        await request.put(`/admin/activities/${activity.id}`, activity)
      } else {
        const res = await request.post('/admin/activities', activity)
        return res.data
      }
      return activity
    }
    await delay(LATENCY)
    const list = getFromDB(DB_KEYS.ACTIVITIES)
    const index = list.findIndex(a => a.id === activity.id)
    if (index !== -1) {
      list[index] = activity
    } else {
      list.unshift(activity)
    }
    saveToDB(DB_KEYS.ACTIVITIES, list)
  },

  async deleteActivity(id) {
    if (USE_REAL_API) {
      await request.delete(`/admin/activities/${id}`)
      return
    }
    await delay(LATENCY)
    let list = getFromDB(DB_KEYS.ACTIVITIES)
    list = list.filter(a => a.id !== id)
    saveToDB(DB_KEYS.ACTIVITIES, list)
  },

  // 失物招领管理
  async fetchLostFoundItems() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/lost-found')
      return res.data
    }
    await delay(LATENCY)
    return []
  },

  async saveLostFoundItem(item) {
    if (USE_REAL_API) {
      if (item.id) {
        await request.put(`/admin/lost-found/${item.id}`, item)
      } else {
        const res = await request.post('/admin/lost-found', item)
        return res.data
      }
      return item
    }
    await delay(LATENCY)
  },

  async toggleLostFoundPin(id) {
    if (USE_REAL_API) {
      await request.post(`/admin/lost-found/${id}/pin`)
      return
    }
    await delay(LATENCY)
  },

  async deleteLostFoundItem(id) {
    if (USE_REAL_API) {
      await request.delete(`/admin/lost-found/${id}`)
      return
    }
    await delay(LATENCY)
  },

  // 通知
  async fetchNotifications() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/notifications')
      return res.data
    }
    await delay(200)
    return getFromDB(DB_KEYS.NOTIFICATIONS)
  }
}
