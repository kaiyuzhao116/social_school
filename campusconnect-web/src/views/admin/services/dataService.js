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

// true：使用后端真实接口
// false：使用本地 mock 数据
const USE_REAL_API = true

const LATENCY = 400
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

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

function unwrap(res) {
  if (!res) return null
  if (res.data !== undefined) return res.data
  return res
}

function unwrapList(res) {
  const data = unwrap(res)

  if (!data) return []
  if (Array.isArray(data)) return data
  if (Array.isArray(data.records)) return data.records
  if (data.data && Array.isArray(data.data)) return data.data
  if (data.data && Array.isArray(data.data.records)) return data.data.records

  return []
}

function getFromDB(key) {
  const data = localStorage.getItem(key)
  return data ? JSON.parse(data) : []
}

function saveToDB(key, data) {
  localStorage.setItem(key, JSON.stringify(data))
}

export const dataService = {
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

  async login(username, password) {
    if (USE_REAL_API) {
      const currentToken = localStorage.getItem('token')
      const currentRefreshToken = localStorage.getItem('refreshToken')
      const currentUser = localStorage.getItem('user')
      const savedFrontendToken = localStorage.getItem('frontendUserToken')

      if (currentToken && !savedFrontendToken) {
        localStorage.setItem('frontendUserToken', currentToken)

        if (currentRefreshToken) {
          localStorage.setItem('frontendUserRefreshToken', currentRefreshToken)
        }

        if (currentUser) {
          localStorage.setItem('frontendUser', currentUser)
        }
      }

      const res = await request.post('/auth/login', {
        username,
        password
      })

      const data = unwrap(res) || {}

      localStorage.setItem('adminToken', data.token)
      localStorage.setItem('adminRefreshToken', data.refreshToken || '')
      localStorage.setItem('token', data.token)
      localStorage.setItem('refreshToken', data.refreshToken || '')

      return data
    }

    await delay(LATENCY)
    return {
      token: 'mock-token',
      refreshToken: 'mock-refresh-token',
      user: {
        name: 'Admin',
        role: '管理员'
      }
    }
  },

  async fetchAdminProfile() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/profile')
      return unwrap(res) || {}
    }

    await delay(LATENCY)

    const profile = localStorage.getItem(DB_KEYS.ADMIN_PROFILE)
    return profile
        ? JSON.parse(profile)
        : {
          name: 'Error',
          role: 'Error',
          avatar: ''
        }
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

  async fetchDashboardStats() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/dashboard/stats')
        return unwrap(res) || {
          userCount: 0,
          postCount: 0,
          pendingVerifications: 0,
          pendingReports: 0,
          pendingPosts: 0
        }
      } catch (e) {
        console.error('fetchDashboardStats 失败:', e)
        return {
          userCount: 0,
          postCount: 0,
          pendingVerifications: 0,
          pendingReports: 0,
          pendingPosts: 0
        }
      }
    }

    await delay(LATENCY)

    const users = getFromDB(DB_KEYS.USERS)
    const posts = getFromDB(DB_KEYS.POSTS)
    const verifications = getFromDB(DB_KEYS.VERIFICATIONS)
    const reports = getFromDB(DB_KEYS.REPORTS)

    return {
      userCount: users.length,
      postCount: posts.length,
      pendingVerifications: verifications.filter(v => v.status === '待审核').length,
      pendingReports: reports.filter(r => r.status === '待处理').length,
      pendingPosts: posts.filter(p => p.status === '待审核').length
    }
  },

  async fetchActivityTrend() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/dashboard/activity-trend')
        return unwrapList(res)
      } catch (e) {
        console.error('fetchActivityTrend 失败:', e)
        return []
      }
    }

    await delay(LATENCY)

    const hours = []
    const now = new Date()

    for (let i = 6; i >= 0; i--) {
      const d = new Date(now.getTime() - i * 60 * 60 * 1000)

      hours.push({
        name: d.getHours().toString().padStart(2, '0') + ':00',
        visits: 0,
        posts: 0
      })
    }

    return hours
  },

  async fetchContentStats() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/dashboard/content-stats')
        return unwrapList(res)
      } catch (e) {
        console.error('fetchContentStats 失败:', e)
        return []
      }
    }

    await delay(LATENCY)

    const posts = getFromDB(DB_KEYS.POSTS)
    const counter = {}

    posts.forEach(post => {
      const tags = Array.isArray(post.tags) ? post.tags : []

      tags.forEach(tag => {
        counter[tag] = (counter[tag] || 0) + 1
      })
    })

    return Object.keys(counter).map(name => ({
      name,
      posts: counter[name]
    }))
  },

  async fetchUsers() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/users')
        return unwrapList(res)
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

  async fetchPosts() {
    if (USE_REAL_API) {
      try {
        const res = await request.get('/admin/posts')
        return unwrapList(res)
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
      await request.post(`/admin/posts/${postId}/moderate`, {
        action
      })
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

  async fetchVerifications() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/verifications')
      return unwrapList(res)
    }

    await delay(LATENCY)
    return getFromDB(DB_KEYS.VERIFICATIONS)
  },

  async reviewVerification(id, status, rejectReason = '') {
    if (USE_REAL_API) {
      await request.post(`/admin/verifications/${id}/review`, {
        status,
        rejectReason
      })
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

  async fetchReports() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/reports')
      return unwrapList(res)
    }

    await delay(LATENCY)
    return getFromDB(DB_KEYS.REPORTS)
  },

  async resolveReport(id, action) {
    if (USE_REAL_API) {
      await request.post(`/admin/reports/${id}/resolve`, {
        action
      })
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

  async fetchAnnouncements() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/announcements')
      return unwrapList(res)
    }

    await delay(300)
    return getFromDB(DB_KEYS.ANNOUNCEMENTS)
  },

  async saveAnnouncement(announcement) {
    if (USE_REAL_API) {
      if (announcement.id) {
        await request.put(`/admin/announcements/${announcement.id}`, announcement)
        return announcement
      }

      const res = await request.post('/admin/announcements', announcement)
      return unwrap(res)
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
    return announcement
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

  async fetchActivities() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/activities')
      return unwrapList(res)
    }

    await delay(300)
    return getFromDB(DB_KEYS.ACTIVITIES)
  },

  async saveActivity(activity) {
    if (USE_REAL_API) {
      if (activity.id) {
        await request.put(`/admin/activities/${activity.id}`, activity)
        return activity
      }

      const res = await request.post('/admin/activities', activity)
      return unwrap(res)
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
    return activity
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

  async fetchLostFoundItems() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/lost-found')
      return unwrapList(res)
    }

    await delay(LATENCY)
    return []
  },

  async saveLostFoundItem(item) {
    if (USE_REAL_API) {
      if (item.id) {
        await request.put(`/admin/lost-found/${item.id}`, item)
        return item
      }

      const res = await request.post('/admin/lost-found', item)
      return unwrap(res)
    }

    await delay(LATENCY)
    return item
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

  async fetchNotifications() {
    if (USE_REAL_API) {
      const res = await request.get('/admin/notifications')
      return unwrapList(res)
    }

    await delay(200)
    return getFromDB(DB_KEYS.NOTIFICATIONS)
  }
}