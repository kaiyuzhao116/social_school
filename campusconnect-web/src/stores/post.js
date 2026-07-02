import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { postApi, commentApi } from '../api'
import { createPost, createComment, formatTimeAgo, PostStatus } from '../types'

// 配置：设为true使用后端API，false使用本地模拟数据
const USE_REAL_API = true

export const usePostStore = defineStore('post', () => {
  // ========================================
  // 状态
  // ========================================
  const posts = ref([])
  const hotPosts = ref([])
  const currentPost = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const searchQuery = ref('')

  // 分页状态
  const pagination = ref({
    page: 1,
    pageSize: 10,
    total: 0,
    hasMore: true
  })

  // ========================================
  // 计算属性
  // ========================================
  const postCount = computed(() => posts.value.length)
  const hasMore = computed(() => pagination.value.hasMore)
  const topPosts = computed(() => hotPosts.value)

  // ========================================
  // 工具函数：转换后端帖子数据
  // ========================================
  function transformPost(data) {
    if (!data) return null
    return createPost({
      id: String(data.id),
      content: data.content,
      images: data.images || [],
      tags: data.tags || [],
      user: data.author ? {
        id: String(data.author.id),
        name: data.author.nickname || data.author.username,
        avatar: data.author.avatar || 'https://picsum.photos/100/100',
        role: data.author.role?.toLowerCase() || 'student',
        verifyType: data.author.verifyType
      } : { id: '0', name: '匿名用户', avatar: 'https://picsum.photos/100/100' },
      likes: data.likeCount || 0,
      comments: data.commentCount || 0,
      shares: data.shareCount || 0,
      isLiked: data.isLiked || false,
      isPinned: data.isPinned || false,
      timeAgo: formatTimeAgo(data.createdAt),
      createdAt: data.createdAt,
      updatedAt: data.updatedAt,
      timestamp: new Date(data.createdAt).getTime(), // Used for 12h edit check
      updatedTimestamp: data.updatedAt ? new Date(data.updatedAt).getTime() : null
    })
  }

  // ========================================
  // 获取帖子
  // ========================================

  // 获取帖子列表
  async function fetchPosts(params = {}, append = false) {
    if (loading.value) return

    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        const res = await postApi.getPosts({
          page: pagination.value.page,
          size: pagination.value.pageSize,
          ...params
        })
        const data = res.data
        const newPosts = (data.records || data.list || []).map(p => transformPost(p))
        pagination.value.total = data.total || 0

        if (append) {
          posts.value = [...posts.value, ...newPosts]
        } else {
          posts.value = newPosts
        }
        pagination.value.hasMore = posts.value.length < pagination.value.total
        return { success: true, data: posts.value }
      }

      // 模拟数据
      if (!append || posts.value.length === 0) {
        posts.value = generateMockPosts()
      }
      return { success: true, data: posts.value }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '获取帖子失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 加载更多
  async function loadMore() {
    if (!hasMore.value || loading.value) return

    pagination.value.page++
    return fetchPosts({}, true)
  }

  // 刷新列表
  async function refresh() {
    pagination.value.page = 1
    pagination.value.hasMore = true
    posts.value = []
    return fetchPosts()
  }

  // 获取热门帖子
  async function fetchHotPosts() {
    try {
      if (USE_REAL_API) {
        const res = await postApi.getHotPosts({ limit: 5 })
        hotPosts.value = (res.data || []).map(p => transformPost(p))
        return { success: true, data: hotPosts.value }
      }

      // 模拟数据
      hotPosts.value = [
        { id: 'h1', content: '图书馆占座神器分享，考研党必备！', likes: 328, comments: 56, user: { id: 'u1', name: '学习达人', avatar: 'https://picsum.photos/seed/h1/40' } },
        { id: 'h2', content: '食堂新开的麻辣烫真的绝了，强烈推荐！', likes: 256, comments: 89, user: { id: 'u2', name: '美食家', avatar: 'https://picsum.photos/seed/h2/40' } },
        { id: 'h3', content: '有人一起组队参加编程马拉松吗？', likes: 189, comments: 34, user: { id: 'u3', name: '程序员', avatar: 'https://picsum.photos/seed/h3/40' } },
        { id: 'h4', content: '校园里的樱花开了，太美了！', likes: 445, comments: 78, user: { id: 'u4', name: '摄影爱好者', avatar: 'https://picsum.photos/seed/h4/40' } },
        { id: 'h5', content: '期末考试复习资料整理，需要的来', likes: 567, comments: 123, user: { id: 'u5', name: '学霸', avatar: 'https://picsum.photos/seed/h5/40' } }
      ]
      return { success: true, data: hotPosts.value }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // 获取个性化推荐帖子
  async function fetchRecommendedPosts(limit = 20) {
    try {
      if (USE_REAL_API) {
        const res = await postApi.getRecommendedPosts({ limit })
        const recommended = (res.data || []).map(p => transformPost(p))
        return { success: true, data: recommended }
      }
      // 模拟数据：返回热门帖子
      return { success: true, data: hotPosts.value }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // 获取单个帖子
  async function fetchPostById(id) {
    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        const res = await postApi.getPostById(id)
        currentPost.value = transformPost(res.data)
        return { success: true, data: currentPost.value }
      }

      currentPost.value = posts.value.find(p => p.id === id) || null
      return { success: true, data: currentPost.value }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '获取帖子失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // ========================================
  // 创建/编辑帖子
  // ========================================

  // 创建帖子
  async function createNewPost(data, user) {
    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        const res = await postApi.createPost({
          content: data.content,
          images: data.images || [],
          tags: data.tags || [],
          isAnonymous: data.isAnonymous || false
        })
        const newPost = transformPost(res.data)
        posts.value.unshift(newPost)
        return { success: true, data: newPost }
      }

      // 模拟创建
      const newPost = createPost({
        id: 'post-' + Date.now(),
        content: data.content,
        images: data.images || [],
        tags: data.tags || [],
        user: { id: user.id, name: user.name, avatar: user.avatar, role: user.role },
        likes: 0, comments: 0, shares: 0,
        timeAgo: '刚刚',
        createdAt: new Date().toISOString()
      })
      posts.value.unshift(newPost)
      return { success: true, data: newPost }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '发布失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 更新帖子
  async function updatePost(id, data) {
    loading.value = true
    error.value = null

    try {
      if (USE_REAL_API) {
        await postApi.updatePost(id, data)
      }

      const index = posts.value.findIndex(p => p.id === id || p.id === String(id))
      if (index !== -1) {
        posts.value[index] = { ...posts.value[index], ...data }
      }
      return { success: true }
    } catch (e) {
      error.value = e.response?.data?.message || e.message || '更新失败'
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  // 删除帖子
  async function deletePost(id) {
    try {
      if (USE_REAL_API) {
        await postApi.deletePost(id)
      }
      posts.value = posts.value.filter(p => p.id !== id && p.id !== String(id))
      return { success: true }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // ========================================
  // 互动功能
  // ========================================

  // 点赞
  async function likePost(id) {
    const post = posts.value.find(p => p.id === id || p.id === String(id))
    if (!post) return { success: false }

    // 乐观更新
    const wasLiked = post.isLiked
    post.isLiked = !wasLiked
    post.likes += post.isLiked ? 1 : -1

    try {
      if (USE_REAL_API) {
        if (wasLiked) {
          await postApi.unlikePost(id)
        } else {
          await postApi.likePost(id)
        }
      }
      return { success: true, isLiked: post.isLiked }
    } catch (e) {
      // 回滚
      post.isLiked = wasLiked
      post.likes += wasLiked ? 1 : -1
      return { success: false, error: e.message }
    }
  }

  // 分享
  async function sharePost(id) {
    const post = posts.value.find(p => p.id === id || p.id === String(id))
    if (!post) return { success: false }

    try {
      if (USE_REAL_API) {
        await postApi.sharePost(id)
      }
      post.shares++
      return { success: true }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // 切换点赞 (兼容旧接口)
  function toggleLike(id) {
    return likePost(id)
  }

  // 设置搜索关键词
  function setSearchQuery(query) {
    searchQuery.value = query
  }

  // ========================================
  // 评论功能
  // ========================================

  // 获取评论
  async function fetchComments(postId) {
    try {
      if (USE_REAL_API) {
        const res = await commentApi.getComments(postId)
        const data = res.data?.records || res.data || []
        return {
          success: true,
          data: data.map(c => createComment({
            id: String(c.id),
            content: c.content,
            user: c.author ? {
              id: String(c.author.id),
              name: c.author.nickname || c.author.username,
              avatar: c.author.avatar,
              role: c.author.role,
              verifyType: c.author.verifyType
            } : { name: '匿名', avatar: 'https://picsum.photos/40/40' },
            timeAgo: formatTimeAgo(c.createdAt),
            likes: c.likeCount || 0
          }))
        }
      }

      return {
        success: true,
        data: [
          { id: 'c1', content: '写得真好！', user: { name: '张明', avatar: 'https://picsum.photos/seed/c1/40' }, timeAgo: '10分钟前', likes: 5 },
          { id: 'c2', content: '同感同感', user: { name: '李华', avatar: 'https://picsum.photos/seed/c2/40' }, timeAgo: '30分钟前', likes: 2 }
        ]
      }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // 添加评论
  async function addComment(postId, content, user) {
    try {
      if (USE_REAL_API) {
        const res = await commentApi.addComment(postId, { content })
        const post = posts.value.find(p => p.id === postId || p.id === String(postId))
        if (post) post.comments++
        return { success: true, data: res.data }
      }

      const newComment = createComment({
        id: 'comment-' + Date.now(),
        content,
        user: { id: user.id, name: user.name, avatar: user.avatar },
        postId,
        timeAgo: '刚刚',
        createdAt: new Date().toISOString()
      })
      const post = posts.value.find(p => p.id === postId)
      if (post) post.comments++
      return { success: true, data: newComment }
    } catch (e) {
      return { success: false, error: e.message }
    }
  }

  // ========================================
  // 辅助方法
  // ========================================

  // 更新帖子中的用户信息
  function updateUserPosts(userData) {
    posts.value.forEach(post => {
      if (post.user.id === userData.id || post.user.id === String(userData.id)) {
        post.user = { ...post.user, name: userData.name, avatar: userData.avatar }
      }
    })
  }

  // 获取用户帖子
  function getUserPosts(userId) {
    return posts.value.filter(p => p.user.id === userId || p.user.id === String(userId))
  }

  // ========================================
  // 模拟数据生成（仅USE_REAL_API=false时使用）
  // ========================================
  function generateMockPosts() {
    return [
      {
        id: '1',
        content: '今天在图书馆学习了一整天，感觉效率超高！分享一下我的学习方法：番茄工作法真的很有用，25分钟专注学习，5分钟休息。大家可以试试看！📚',
        images: ['https://picsum.photos/seed/post1/800/600'],
        tags: ['学习', '图书馆', '分享'],
        user: { id: '001', name: '学习达人', avatar: 'https://picsum.photos/seed/user1/100', role: 'student' },
        likes: 128, comments: 23, shares: 5, isLiked: false, timeAgo: '2小时前'
      },
      {
        id: '2',
        content: '食堂二楼新开了一家麻辣烫，味道超级棒！推荐大家去尝尝，性价比很高～ 🍜',
        images: ['https://picsum.photos/seed/food1/800/600', 'https://picsum.photos/seed/food2/800/600'],
        tags: ['美食', '食堂', '推荐'],
        user: { id: '002', name: '美食家小王', avatar: 'https://picsum.photos/seed/user2/100', role: 'student' },
        likes: 256, comments: 45, shares: 12, isLiked: true, timeAgo: '3小时前'
      },
      {
        id: '3',
        content: '校园的樱花开了！趁着好天气去拍了一组照片，春天真的太美了 🌸',
        images: ['https://picsum.photos/seed/sakura1/800/600', 'https://picsum.photos/seed/sakura2/800/600', 'https://picsum.photos/seed/sakura3/800/600'],
        tags: ['校园', '樱花', '摄影'],
        user: { id: '003', name: '摄影爱好者', avatar: 'https://picsum.photos/seed/user3/100', role: 'student' },
        likes: 512, comments: 67, shares: 34, isLiked: false, timeAgo: '5小时前'
      },
      {
        id: '4',
        content: '有没有一起准备考研的小伙伴？我准备报考计算机专业，想找几个研友互相监督学习！有意向的可以私信我～ 💪',
        images: [],
        tags: ['考研', '计算机', '组队'],
        user: { id: '004', name: '考研人', avatar: 'https://picsum.photos/seed/user4/100', role: 'student' },
        likes: 89, comments: 34, shares: 8, isLiked: false, timeAgo: '昨天'
      },
      {
        id: '5',
        content: '【失物招领】在3号教学楼A301捡到一个黑色钱包，里面有学生证和一些现金。请失主看到后联系我认领！',
        images: [],
        tags: ['失物招领', '钱包'],
        user: { id: '005', name: '热心同学', avatar: 'https://picsum.photos/seed/user5/100', role: 'student' },
        likes: 45, comments: 12, shares: 28, isLiked: false, timeAgo: '昨天'
      }
    ]
  }

  return {
    // 状态
    posts,
    hotPosts,
    currentPost,
    loading,
    error,
    pagination,
    searchQuery,

    // 计算属性
    postCount,
    hasMore,
    topPosts,

    // 获取帖子
    fetchPosts,
    loadMore,
    refresh,
    fetchHotPosts,
    fetchRecommendedPosts,
    fetchPostById,

    // 创建/编辑
    createPost: createNewPost,
    updatePost,
    deletePost,

    // 互动
    likePost,
    sharePost,
    toggleLike,

    // 搜索
    setSearchQuery,

    // 评论
    fetchComments,
    addComment,

    // 辅助
    updateUserPosts,
    getUserPosts
  }
})
