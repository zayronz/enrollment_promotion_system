import request from '@/utils/request'

export const activityApi = {
    // 获取活动列表
    getActivityList(params) {
        return request.get('/activity/list', { params })
    },

    // 获取公开活动（无需登录）
    getOpenActivities() {
        return request.get('/activity/open')
    },

    // 获取活动详情
    getActivityDetail(id) {
        return request.get(`/activity/${id}`)
    },

    // 创建活动
    createActivity(data) {
        return request.post('/activity/create', data)
    },

    // 更新活动
    updateActivity(id, data) {
        return request.put(`/activity/${id}`, data)
    },

    // 删除活动
    deleteActivity(id) {
        return request.delete(`/activity/${id}`)
    },

    // 发布活动
    publishActivity(id) {
        return request.put(`/activity/${id}/publish`)
    },

    // 设置首页展示
    setHomeShow(id, show) {
        return request.put(`/activity/${id}/home-show`, null, { params: { show } })
    }
}