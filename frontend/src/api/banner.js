import request from '@/utils/request'

/**
 * 轮播图 API
 */

// 创建轮播图
export function createBanner(data) {
  return request({
    url: '/api/banner',
    method: 'post',
    data
  })
}

// 更新轮播图
export function updateBanner(id, data) {
  return request({
    url: `/api/banner/${id}`,
    method: 'put',
    data
  })
}

// 删除轮播图
export function deleteBanner(id) {
  return request({
    url: `/api/banner/${id}`,
    method: 'delete'
  })
}

// 查询轮播图详情
export function getBannerById(id) {
  return request({
    url: `/api/banner/${id}`,
    method: 'get'
  })
}

// 分页查询轮播图列表
export function getBannerPage(params) {
  return request({
    url: '/api/banner/page',
    method: 'get',
    params
  })
}

// 获取所有启用的轮播图（用于前端展示）
export function getActiveBanners() {
  return request({
    url: '/api/banner/active',
    method: 'get'
  })
}

// 根据活动ID获取轮播图列表
export function getBannersByActivityId(activityId) {
  return request({
    url: `/api/banner/activity/${activityId}`,
    method: 'get'
  })
}

// 批量创建轮播图
export function batchCreateBanners(data) {
  return request({
    url: '/api/banner/batch',
    method: 'post',
    data
  })
}

// 更新轮播图状态
export function updateBannerStatus(id, status) {
  return request({
    url: `/api/banner/${id}/status`,
    method: 'patch',
    params: { status }
  })
}
