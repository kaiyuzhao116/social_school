// 默认头像
export const DEFAULT_AVATAR = 'https://ui-avatars.com/api/?name=User&background=6366f1&color=fff&size=200'

/**
 * 获取有效的头像URL
 * @param {string} avatar - 原始头像URL
 * @param {string} name - 用户名（用于生成默认头像）
 * @returns {string} 有效的头像URL
 */
export function getAvatarUrl(avatar, name) {
  // 有有效头像且不是随机URL，优先使用
  if (avatar && avatar.trim() && !avatar.includes('random=')) {
    return avatar
  }
  // 生成基于用户名的默认头像
  const displayName = name || 'User'
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=6366f1&color=fff&size=200`
}

/**
 * 头像加载失败时的处理函数
 * @param {Event} e - 图片加载错误事件
 */
export function handleAvatarError(e) {
  e.target.src = DEFAULT_AVATAR
}

/**
 * 检查封面URL是否有效
 * @param {string} url - 封面URL
 * @returns {boolean}
 */
export function isValidCoverUrl(url) {
  if (!url || !url.trim()) return false
  // 支持 blob: URL（本地预览）和普通 URL
  if (url.startsWith('blob:')) return true
  // 排除随机 URL
  if (url.includes('random=')) return false
  return true
}

/**
 * 封面加载失败时的处理函数
 * @param {Event} e - 图片加载错误事件
 */
export function handleCoverError(e) {
  e.target.style.display = 'none'
}
