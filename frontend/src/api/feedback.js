import request from '@/utils/request'

export const feedbackApi = {
    // 提交反馈
    submit(data) {
        return request.post('/feedback/submit', data)
    },

    // 获取活动反馈列表
    getActivityFeedbacks(activityId) {
        return request.get(`/feedback/activity/${activityId}`)
    },

    // 获取我的反馈
    getMyFeedbacks(activityId) {
        return request.get('/feedback/my', { params: { activityId } })
    }
}