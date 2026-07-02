import { uploadApi } from '../api'

// 是否使用后端上传 (接入后端后设为 true)
const USE_BACKEND_UPLOAD = false

// 图片压缩配置
const COMPRESS_OPTIONS = {
  maxWidth: 1920,
  maxHeight: 1080,
  quality: 0.8
}

/**
 * 压缩图片
 * @param {File} file - 图片文件
 * @param {Object} options - 压缩选项
 * @returns {Promise<Blob>}
 */
export function compressImage(file, options = COMPRESS_OPTIONS) {
  return new Promise((resolve, reject) => {
    const img = new Image()
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')

    img.onload = () => {
      let { width, height } = img
      const { maxWidth, maxHeight, quality } = options

      // 计算缩放比例
      if (width > maxWidth || height > maxHeight) {
        const ratio = Math.min(maxWidth / width, maxHeight / height)
        width = Math.round(width * ratio)
        height = Math.round(height * ratio)
      }

      canvas.width = width
      canvas.height = height
      ctx.drawImage(img, 0, 0, width, height)

      canvas.toBlob(
        (blob) => resolve(blob),
        file.type || 'image/jpeg',
        quality
      )
    }

    img.onerror = reject
    img.src = URL.createObjectURL(file)
  })
}

/**
 * 上传单个图片
 * @param {File} file - 图片文件
 * @param {Function} onProgress - 进度回调
 * @returns {Promise<{success: boolean, url?: string, error?: string}>}
 */
export async function uploadImage(file, onProgress) {
  try {
    // 验证文件类型
    if (!file.type.startsWith('image/')) {
      return { success: false, error: '请选择图片文件' }
    }

    // 验证文件大小 (10MB)
    const maxSize = parseInt(import.meta.env.VITE_MAX_FILE_SIZE) || 10485760
    if (file.size > maxSize) {
      return { success: false, error: '图片大小不能超过 10MB' }
    }

    if (USE_BACKEND_UPLOAD) {
      // 压缩图片
      const compressedBlob = await compressImage(file)
      const compressedFile = new File([compressedBlob], file.name, { type: file.type })
      
      // 上传到服务器
      const res = await uploadApi.uploadImage(compressedFile, onProgress)
      return { success: true, url: res.data.url }
    } else {
      // 使用本地 blob URL (开发模式)
      const url = URL.createObjectURL(file)
      return { success: true, url }
    }
  } catch (e) {
    return { success: false, error: e.message || '上传失败' }
  }
}

/**
 * 批量上传图片
 * @param {FileList|File[]} files - 图片文件列表
 * @param {Function} onProgress - 进度回调
 * @param {number} maxCount - 最大数量
 * @returns {Promise<{success: boolean, urls?: string[], errors?: string[]}>}
 */
export async function uploadImages(files, onProgress, maxCount = 9) {
  const fileArray = Array.from(files).slice(0, maxCount)
  const urls = []
  const errors = []

  for (let i = 0; i < fileArray.length; i++) {
    const result = await uploadImage(fileArray[i], (progress) => {
      onProgress?.({
        current: i + 1,
        total: fileArray.length,
        progress
      })
    })

    if (result.success) {
      urls.push(result.url)
    } else {
      errors.push(`${fileArray[i].name}: ${result.error}`)
    }
  }

  return {
    success: errors.length === 0,
    urls,
    errors: errors.length > 0 ? errors : undefined
  }
}

/**
 * 上传文件 (PDF等)
 * @param {File} file - 文件
 * @param {Function} onProgress - 进度回调
 * @returns {Promise<{success: boolean, url?: string, error?: string}>}
 */
export async function uploadFile(file, onProgress) {
  try {
    if (USE_BACKEND_UPLOAD) {
      const res = await uploadApi.uploadFile(file, onProgress)
      return { success: true, url: res.data.url }
    } else {
      const url = URL.createObjectURL(file)
      return { success: true, url }
    }
  } catch (e) {
    return { success: false, error: e.message || '上传失败' }
  }
}

/**
 * 上传头像
 * @param {File} file - 图片文件
 * @returns {Promise<{success: boolean, url?: string, error?: string}>}
 */
export async function uploadAvatar(file) {
  // 头像大小限制 2MB
  const maxSize = parseInt(import.meta.env.VITE_AVATAR_MAX_SIZE) || 2097152
  if (file.size > maxSize) {
    return { success: false, error: '头像大小不能超过 2MB' }
  }

  // 压缩为 200x200
  const compressedBlob = await compressImage(file, {
    maxWidth: 200,
    maxHeight: 200,
    quality: 0.9
  })

  if (USE_BACKEND_UPLOAD) {
    const compressedFile = new File([compressedBlob], file.name, { type: file.type })
    const res = await uploadApi.uploadImage(compressedFile)
    return { success: true, url: res.data.url }
  } else {
    const url = URL.createObjectURL(compressedBlob)
    return { success: true, url }
  }
}

/**
 * 上传封面图
 * @param {File} file - 图片文件
 * @returns {Promise<{success: boolean, url?: string, error?: string}>}
 */
export async function uploadCover(file) {
  // 封面大小限制 5MB
  const maxSize = parseInt(import.meta.env.VITE_COVER_MAX_SIZE) || 5242880
  if (file.size > maxSize) {
    return { success: false, error: '封面大小不能超过 5MB' }
  }

  // 压缩为 1200x400
  const compressedBlob = await compressImage(file, {
    maxWidth: 1200,
    maxHeight: 400,
    quality: 0.85
  })

  if (USE_BACKEND_UPLOAD) {
    const compressedFile = new File([compressedBlob], file.name, { type: file.type })
    const res = await uploadApi.uploadImage(compressedFile)
    return { success: true, url: res.data.url }
  } else {
    const url = URL.createObjectURL(compressedBlob)
    return { success: true, url }
  }
}

/**
 * 释放 blob URL (避免内存泄漏)
 * @param {string|string[]} urls - URL 或 URL 数组
 */
export function revokeUrls(urls) {
  const urlArray = Array.isArray(urls) ? urls : [urls]
  urlArray.forEach(url => {
    if (url && url.startsWith('blob:')) {
      URL.revokeObjectURL(url)
    }
  })
}

/**
 * 判断是否为 blob URL
 * @param {string} url 
 * @returns {boolean}
 */
export function isBlobUrl(url) {
  return url?.startsWith('blob:') || false
}

/**
 * 获取文件扩展名
 * @param {string} filename 
 * @returns {string}
 */
export function getFileExtension(filename) {
  return filename.slice(filename.lastIndexOf('.')).toLowerCase()
}

/**
 * 格式化文件大小
 * @param {number} bytes 
 * @returns {string}
 */
export function formatFileSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
