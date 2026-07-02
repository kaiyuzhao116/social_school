import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, userApi, verificationApi } from '../api'
import { createUser, UserRole, VerificationStatus } from '../types'

// 配置：设为true使用后端API，false使用本地模拟数据
const USE_REAL_API = true

export const useUserStore = defineStore('user', () => {
  // ========================================
  // 状态
  // ========================================
  const currentUser = ref(null)
  const loading = ref(false)
  const error = ref(null)

  // ========================================
  // 计算属性
  // ========================================
  const isLoggedIn = computed(() => !!currentUser.value)
  const userId = computed(() => currentUser.value?.id)
  const userName = computed(() => currentUser.value?.name || currentUser.value?.nickname || '')
  const userAvatar = computed(() => currentUser.value?.avatar || '')
  const userRole = computed(() => currentUser.value?.role || UserRole.STUDENT)
  const isVerified = computed(() => currentUser.value?.isVerified || currentUser.value?.verifyStatus === 'VERIFIED')

  // ========================================
  // 默认图片
  // ========================================
  const DEFAULT_AVATAR = 'https://ui-avatars.com/api/?name=User&background=6366f1&color=fff&size=200'
  const DEFAULT_COVER = ''

  // ========================================
  // 工具函数：转换后端用户数据
  // ========================================
  function transformUser(data) {
    if (!data) return null
    const name = data.nickname || data.username || 'User'
    const roleStr = (data.role || '').toUpperCase()
    const verifyType = (data.verifyType || '').toUpperCase() // STUDENT / TEACHER / ORG / DEPARTMENT
    const verifyStatus = (data.verifyStatus || '').toUpperCase() // NONE / PENDING / VERIFIED / REJECTED

    // 解析JSON字段
    let hobbies = []
    let privacy = {}
    try {
      if (data.hobbies) {
        // 如果已经是数组，直接使用
        if (Array.isArray(data.hobbies)) {
          hobbies = data.hobbies
        } else {
          // 尝试解析JSON字符串
          let parsed = JSON.parse(data.hobbies)
          // 如果解析后还是字符串（双重编码），再解析一次
          if (typeof parsed === 'string') {
            parsed = JSON.parse(parsed)
          }
          hobbies = Array.isArray(parsed) ? parsed : []
        }
      }
    } catch (e) {
      console.warn('解析hobbies失败:', e)
    }
    try {
      if (data.privacy) {
        if (typeof data.privacy === 'object') {
          privacy = data.privacy
        } else {
          let parsed = JSON.parse(data.privacy)
          if (typeof parsed === 'string') {
            parsed = JSON.parse(parsed)
          }
          privacy = parsed || {}
        }
      }
    } catch (e) {
      console.warn('解析privacy失败:', e)
    }

    // 处理用户角色 - 支持多种格式
    let userRole = UserRole.STUDENT
    if (roleStr === 'ADMIN') {
      userRole = UserRole.ADMIN
    } else if (roleStr === 'MODERATOR') {
      userRole = UserRole.MODERATOR
    } else if (roleStr === 'TEACHER' || roleStr === 'teacher') {
      userRole = UserRole.TEACHER
    } else if (roleStr === 'DEPARTMENT' || roleStr === 'ORG' || roleStr === 'department') {
      userRole = UserRole.DEPARTMENT
    } else if (verifyType === 'TEACHER') {
      userRole = UserRole.TEACHER
    } else if (verifyType === 'ORG' || verifyType === 'DEPARTMENT') {
      userRole = UserRole.DEPARTMENT
    }

    // 判断是否已认证
    const isVerified = verifyStatus === 'VERIFIED' ||
      userRole === UserRole.TEACHER ||
      userRole === UserRole.DEPARTMENT

    // 认证状态映射
    let mappedVerificationStatus = VerificationStatus.NONE
    if (verifyStatus === 'VERIFIED') {
      mappedVerificationStatus = VerificationStatus.APPROVED
    } else if (verifyStatus === 'PENDING') {
      mappedVerificationStatus = VerificationStatus.PENDING
    } else if (verifyStatus === 'REJECTED') {
      mappedVerificationStatus = VerificationStatus.REJECTED
    }

    return createUser({
      id: String(data.id),
      name: name,
      username: data.username,
      avatar: data.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=6366f1&color=fff&size=200`,
      coverImage: data.coverImage || DEFAULT_COVER,
      email: data.email,
      phone: data.phone,
      bio: data.bio,
      gender: data.gender,
      college: data.college,
      major: data.major,
      className: data.className,
      dormitory: data.dormitory,
      age: data.age,
      hobbies: hobbies,
      privacy: privacy,
      role: userRole,
      isVerified: isVerified,
      verificationStatus: mappedVerificationStatus,
      followers: [],
      following: [],
      followerCount: data.followerCount || 0,
      followingCount: data.followingCount || 0,
      postCount: data.postCount || 0
    })
  }

  // ========================================
  // 认证方法
  // ========================================

  // 登录
  async function login(credentials) {
    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        const res = await authApi.login({
          // 使用邮箱登录
          username: credentials.email,
          password: credentials.password
        })
        const { token, refreshToken, user } = res.data
        localStorage.setItem('token', token)
        if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
        currentUser.value = transformUser(user)
        localStorage.setItem('user', JSON.stringify(currentUser.value))
        // 额外保存前台用户完整状态备份，以便管理员登录后能恢复
        localStorage.setItem('frontendUserToken', token)
        if (refreshToken) localStorage.setItem('frontendUserRefreshToken', refreshToken)
        localStorage.setItem('frontendUser', JSON.stringify(currentUser.value))
        return { success: true, user: currentUser.value }
      }

      // 模拟登录
      const mockName = credentials.name || credentials.username || 'User'
      const mockUser = createUser({
        id: '000001',
        name: mockName,
        avatar: `https://ui-avatars.com/api/?name=${encodeURIComponent(mockName)}&background=6366f1&color=fff&size=200`,
        role: UserRole.STUDENT,
        isVerified: true
      })
      currentUser.value = mockUser
      localStorage.setItem('user', JSON.stringify(mockUser))
      localStorage.setItem('token', 'mock-token-' + Date.now())
      return { success: true, user: mockUser }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '登录失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 注册
  async function register(data) {
    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        await authApi.register({
          username: data.username,
          password: data.password,
          confirmPassword: data.confirmPassword,
          nickname: data.username, // 昵称缺省为用户名
          email: data.email
        })
        return { success: true, message: '注册成功，请登录' }
      }

      await new Promise(resolve => setTimeout(resolve, 500))
      return { success: true, message: '注册成功，请登录' }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '注册失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 退出登录
  async function logout() {
    try {
      if (USE_REAL_API) {
        await authApi.logout().catch(() => { })
      }
    } catch (e) {
      console.error('Logout error:', e)
    } finally {
      currentUser.value = null
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      // 同时清除备份的前台用户完整状态
      localStorage.removeItem('frontendUserToken')
      localStorage.removeItem('frontendUserRefreshToken')
      localStorage.removeItem('frontendUser')
    }
  }

  // ========================================
  // 用户信息方法
  // ========================================

  // 获取当前用户信息
  async function fetchCurrentUser() {
    if (!localStorage.getItem('token')) return null

    // 先从 localStorage 恢复用户（确保刷新不丢失）
    const saved = localStorage.getItem('user')
    let localUser = null
    if (saved && !currentUser.value) {
      try {
        localUser = createUser(JSON.parse(saved))
        currentUser.value = localUser
      } catch (e) {
        console.error('Failed to parse saved user:', e)
      }
    } else if (currentUser.value) {
      localUser = currentUser.value
    }

    // 如果不使用真实API，直接返回
    if (!USE_REAL_API) {
      return currentUser.value
    }

    // 尝试从后端获取最新用户信息
    loading.value = true
    error.value = null

    try {
      const res = await userApi.getCurrentUser()

      // 新格式: { user: {...}, following: [...] }
      // 兼容旧格式: 直接返回 user 对象
      const userData = res.data.user || res.data
      const followingList = res.data.following || []

      const backendUser = transformUser(userData)
      // 将关注列表挂到用户对象上，供前端判断 isFollowing 使用
      backendUser.following = followingList

      // 检查用户 ID 是否一致，如果不一致说明 token 对应了不同的用户
      // 这种情况可能发生在：管理员登录后台后回到前台
      const isSameUser = localUser && String(localUser.id) === String(backendUser.id)

      if (isSameUser) {
        // 同一用户，合并数据：后端数据优先，但如果后端某些字段为空而本地有值，保留本地值
        // 头像：如果后端为空或是默认头像，保留本地有效头像
        if (!backendUser.avatar || backendUser.avatar.includes('ui-avatars.com')) {
          if (localUser.avatar && !localUser.avatar.includes('ui-avatars.com')) {
            backendUser.avatar = localUser.avatar
          }
        }
        // 封面：如果后端为空，保留本地封面
        if (!backendUser.coverImage && localUser.coverImage) {
          backendUser.coverImage = localUser.coverImage
        }
      }

      // 更新 currentUser 和 localStorage
      currentUser.value = backendUser
      localStorage.setItem('user', JSON.stringify(backendUser))

      return backendUser

    } catch (e) {
      error.value = e.message

      // If 401/403 (Auth failed), clear user state immediately
      if (e.response?.status === 401 || e.response?.status === 403) {
        logout()
        return null
      }

      // If API fails but we have local data (and it's not an auth error), keep local data
      return currentUser.value
    } finally {
      loading.value = false
    }
  }

  // 更新用户信息
  async function updateUser(userData) {
    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        await userApi.updateUser(userData)
        currentUser.value = { ...currentUser.value, ...userData }
        localStorage.setItem('user', JSON.stringify(currentUser.value))
        return { success: true }
      }

      currentUser.value = { ...currentUser.value, ...userData }
      localStorage.setItem('user', JSON.stringify(currentUser.value))
      return { success: true }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '更新失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 更新头像
  async function updateAvatar(avatarUrl) {
    try {
      if (USE_REAL_API) {
        await userApi.updateAvatar(avatarUrl)
      }
      if (currentUser.value) {
        currentUser.value.avatar = avatarUrl
        localStorage.setItem('user', JSON.stringify(currentUser.value))
      }
      return { success: true }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // 更新封面
  async function updateCover(coverUrl) {
    try {
      if (USE_REAL_API) {
        await userApi.updateCover(coverUrl)
      }
      if (currentUser.value) {
        currentUser.value.coverImage = coverUrl
        localStorage.setItem('user', JSON.stringify(currentUser.value))
      }
      return { success: true }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // ========================================
  // 社交功能
  // ========================================

  // 关注/取消关注
  async function toggleFollow(targetUserId) {
    if (!currentUser.value) return { success: false, error: '请先登录' }

    const isFollowingUser = currentUser.value.following?.includes(targetUserId)

    try {
      if (USE_REAL_API) {
        if (isFollowingUser) {
          await userApi.unfollowUser(targetUserId)
        } else {
          await userApi.followUser(targetUserId)
        }
      }

      if (isFollowingUser) {
        currentUser.value.following = currentUser.value.following.filter(id => id !== targetUserId)
        currentUser.value.followingCount = Math.max(0, (currentUser.value.followingCount || 1) - 1)
      } else {
        currentUser.value.following = [...(currentUser.value.following || []), targetUserId]
        currentUser.value.followingCount = (currentUser.value.followingCount || 0) + 1
      }
      localStorage.setItem('user', JSON.stringify(currentUser.value))

      return { success: true, isFollowing: !isFollowingUser }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // 检查是否关注
  function isFollowing(userId) {
    return currentUser.value?.following?.includes(userId) || false
  }

  // ========================================
  // 认证申请
  // ========================================

  // 提交认证申请
  async function submitVerification(data) {
    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        const res = await verificationApi.submit(data)
        return { success: true, data: res.data }
      }

      await new Promise(resolve => setTimeout(resolve, 500))
      const history = JSON.parse(localStorage.getItem('verificationHistory') || '[]')
      const record = {
        id: Date.now(),
        type: data.type,
        status: VerificationStatus.PENDING,
        time: new Date().toLocaleString('zh-CN'),
        data
      }
      history.unshift(record)
      localStorage.setItem('verificationHistory', JSON.stringify(history))

      const status = JSON.parse(localStorage.getItem('verificationStatus') || '{}')
      status[data.type] = VerificationStatus.PENDING
      localStorage.setItem('verificationStatus', JSON.stringify(status))

      return { success: true, data: record }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '提交失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 获取认证状态
  async function getVerificationStatus() {
    if (USE_REAL_API) {
      try {
        const res = await verificationApi.getStatus()
        return res.data
      } catch (e) {
        return {}
      }
    }
    return JSON.parse(localStorage.getItem('verificationStatus') || '{}')
  }

  // 获取认证历史
  async function getVerificationHistory() {
    if (USE_REAL_API) {
      try {
        const res = await verificationApi.getHistory()
        return res.data || []
      } catch (e) {
        return []
      }
    }
    return JSON.parse(localStorage.getItem('verificationHistory') || '[]')
  }

  // ========================================
  // 设置
  // ========================================

  // 更新隐私设置
  async function updatePrivacy(privacy) {
    if (!currentUser.value) return { success: false }

    try {
      currentUser.value.privacy = { ...currentUser.value.privacy, ...privacy }
      localStorage.setItem('user', JSON.stringify(currentUser.value))
      return { success: true }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // 更新通知设置
  async function updateSettings(settings) {
    if (!currentUser.value) return { success: false }

    try {
      currentUser.value.settings = { ...currentUser.value.settings, ...settings }
      localStorage.setItem('user', JSON.stringify(currentUser.value))
      return { success: true }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // ========================================
  // 密码找回
  // ========================================

  // 发送重置验证码
  async function sendPasswordResetCode(email) {
    loading.value = true
    error.value = null
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000))
      // 真实场景: await authApi.sendResetCode(email)

      // 模拟成功
      return { success: true, message: '验证码已发送至您的邮箱' }
    } catch (e) {
      error.value = e.message || '发送失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 重置密码
  async function resetPassword(data) {
    // data: { email, code, newPassword }
    loading.value = true
    error.value = null
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1500))
      // 真实场景: await authApi.resetPassword(data)

      return { success: true, message: '密码重置成功，请登录' }
    } catch (e) {
      error.value = e.message || '重置失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // ========================================
  // 初始化
  // ========================================
  function initUser() {
    const savedUser = localStorage.getItem('user')
    if (savedUser) {
      try {
        currentUser.value = createUser(JSON.parse(savedUser))
      } catch (e) {
        console.error('Failed to parse saved user:', e)
        localStorage.removeItem('user')
      }
    }
  }

  return {
    // 状态
    currentUser,
    loading,
    error,

    // 计算属性
    isLoggedIn,
    userId,
    userName,
    userAvatar,
    userRole,
    isVerified,

    // 认证
    login,
    register,
    logout,

    // 用户信息
    fetchCurrentUser,
    updateUser,
    updateAvatar,
    updateCover,

    // 社交
    toggleFollow,
    isFollowing,

    // 认证申请
    submitVerification,
    getVerificationStatus,
    getVerificationHistory,

    // 密码找回
    sendPasswordResetCode,
    resetPassword,

    // 设置
    updatePrivacy,
    updateSettings,

    // 初始化
    initUser
  }
})
