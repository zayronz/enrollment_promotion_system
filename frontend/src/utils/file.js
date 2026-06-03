
// 处理文件URL的工具函数
const API_BASE = '/api/file/view'

/**
 * 获取完整的文件访问URL
 * @param {string} path - 相对路径或完整URL
 * @returns {string} - 完整的文件访问URL
 */
export function getFileUrl(path) {
  if (!path) return ''

  // 如果已经是完整URL，直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  // 如果已经是 /api/file/view/ 开头，直接返回
  if (path.startsWith(API_BASE)) {
    return path
  }

  // 拼接完整的访问路径
  return `${API_BASE}/${path.replace(/^\//, '')}`
}

/**
 * 检查是否是图片文件
 * @param {string} path - 文件路径
 * @returns {boolean}
 */
export function isImage(path) {
  if (!path) return false
  const ext = path.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'webp', 'svg', 'bmp'].includes(ext)
}

/**
 * 检查是否是视频文件
 * @param {string} path - 文件路径
 * @returns {boolean}
 */
export function isVideo(path) {
  if (!path) return false
  const ext = path.split('.').pop().toLowerCase()
  return ['mp4', 'webm', 'ogg', 'avi', 'mov', 'mkv'].includes(ext)
}
