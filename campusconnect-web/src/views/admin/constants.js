import { UserRole, VerificationStatus } from './types'

export const MOCK_USERS = [
  { id: '1', name: '林雨晴', avatar: 'https://picsum.photos/100/100?random=1', email: 'lin.yuqing@uni.edu', role: UserRole.NORMAL, status: '正常', joinDate: '2023-09-01' },
  { id: '2', name: '张伟', avatar: 'https://picsum.photos/100/100?random=2', email: 'zhang.wei@uni.edu', role: UserRole.MODERATOR, status: '正常', joinDate: '2023-08-15' },
  { id: '3', name: '王志强', avatar: 'https://picsum.photos/100/100?random=3', email: 'wang.zhiqiang@uni.edu', role: UserRole.NORMAL, status: '封禁', joinDate: '2023-10-02' },
  { id: '4', name: '陈思思', avatar: 'https://picsum.photos/100/100?random=4', email: 'chen.sisi@uni.edu', role: UserRole.NORMAL, status: '正常', joinDate: '2023-10-25' },
]

export const MOCK_VERIFICATIONS = [
  { id: 'v1', userId: '4', userName: '陈思思', identityType: '学生', idNumber: '20240012', department: '计算机科学与技术', idCardImage: 'https://picsum.photos/400/250?random=4', status: VerificationStatus.PENDING, submittedAt: '2023-10-25 09:30' },
  { id: 'v2', userId: '5', userName: '李浩然', identityType: '教师', idNumber: 'T20240088', department: '法学院', idCardImage: 'https://picsum.photos/400/250?random=5', status: VerificationStatus.PENDING, submittedAt: '2023-10-25 10:15' },
  { id: 'v3', userId: '6', userName: '校吉他社', identityType: '部门', idNumber: 'ORG-003', department: '校团委社团部', idCardImage: 'https://picsum.photos/400/250?random=6', status: VerificationStatus.PENDING, submittedAt: '2023-10-26 14:00' },
]

export const MOCK_POSTS = [
  { 
    id: 'p1', userId: '1', userName: '林雨晴', userAvatar: 'https://picsum.photos/100/100?random=1', 
    content: '求助：有人捡到我的设计概论笔记吗？可能是落在第三教学楼201教室了。', 
    timestamp: '10分钟前', status: '已发布', isPinned: true
  },
  { 
    id: 'p2', userId: '99', userName: '匿名用户', userAvatar: 'https://picsum.photos/100/100?random=99', 
    content: '二食堂的阿姨手抖得太厉害了，这饭根本没法吃，建议大家都别去！垃圾食堂！', 
    timestamp: '2分钟前', status: '审核中', isPinned: false
  },
  {
    id: 'p3', userId: '3', userName: '王志强', userAvatar: 'https://picsum.photos/100/100?random=3',
    content: '兼职代课，需要的私聊，价格公道，包过。',
    timestamp: '1小时前', status: '审核中', isPinned: false
  }
]

export const MOCK_REPORTS = [
  { id: 'r1', targetId: 'p2', targetType: '帖子', reporterName: '张伟', reason: '言语辱骂 / 恶意攻击', status: '待处理' },
  { id: 'r2', targetId: 'p3', targetType: '帖子', reporterName: '系统自动', reason: '疑似违规广告', status: '待处理' },
]

export const MOCK_NOTIFICATIONS = [
  { id: 'n1', title: '高流量预警', message: '检测到社区访问量激增，请注意服务器状态。', type: 'warning', time: '10分钟前', read: false },
  { id: 'n2', title: '新举报', message: '收到3条新的违规内容举报，请及时处理。', type: 'info', time: '30分钟前', read: false },
  { id: 'n3', title: '系统更新', message: '后台管理系统已更新至 v2.1.0 版本。', type: 'success', time: '1天前', read: true },
]

export const MOCK_ANNOUNCEMENTS = [
  {
    id: 'a1',
    title: '关于2024年春季学期校园文化节的通知',
    content: '各位同学，2024年春季校园文化节将于下周三正式启动。届时将有社团展示、创意集市等丰富多彩的活动，欢迎大家踊跃参加。详情请查看附件文档。',
    publisher: '校团委',
    date: '2023-10-24',
    status: '已发布',
    priority: '置顶',
    views: 1254
  },
  {
    id: 'a2',
    title: '图书馆系统维护公告',
    content: '为了提供更好的服务，图书馆管理系统将于本周六凌晨 00:00 至 04:00 进行升级维护，期间将暂停借阅服务。',
    publisher: '图书馆',
    date: '2023-10-25',
    status: '草稿',
    priority: '普通',
    views: 0
  },
  {
    id: 'a3',
    title: '严禁在宿舍使用大功率电器的提醒',
    content: '近期发现部分寝室违规使用大功率电器，存在严重安全隐患。宿管科将进行突击检查，违者将按校规处理。',
    publisher: '后勤部',
    date: '2023-10-20',
    status: '已发布',
    priority: '普通',
    views: 890
  }
]

export const MOCK_ACTIVITIES = [
  {
    id: 'act1',
    title: '校园十佳歌手大赛海选',
    coverImage: 'https://picsum.photos/400/300?random=50',
    location: '大学生活动中心 301',
    startTime: '2023-11-10 18:00',
    organizer: '校学生会文艺部',
    status: '报名中',
    participants: 156
  },
  {
    id: 'act2',
    title: '人工智能前沿技术讲座',
    coverImage: 'https://picsum.photos/400/300?random=51',
    location: '图书馆报告厅',
    startTime: '2023-11-05 14:30',
    organizer: '计算机学院',
    status: '进行中',
    participants: 300
  }
]
