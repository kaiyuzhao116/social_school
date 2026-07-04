// 用户角色枚举
export const UserRole = {
  NORMAL: '普通用户',
  ADMIN: '管理员',
  MODERATOR: '协管员'
}

// 认证状态枚举
export const VerificationStatus = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已拒绝'
}

// 视图状态枚举
export const ViewState = {
  DASHBOARD: 'DASHBOARD',
  VERIFICATION: 'VERIFICATION',
  MODERATION: 'MODERATION',
  POST_MANAGEMENT: 'POST_MANAGEMENT',
  USERS: 'USERS',
  REPORTS: 'REPORTS',
  ANNOUNCEMENTS: 'ANNOUNCEMENTS',
  ACTIVITIES: 'ACTIVITIES',
  LOST_FOUND: 'LOST_FOUND',
  TRAFFIC_CONTROL: 'TRAFFIC_CONTROL',
}
