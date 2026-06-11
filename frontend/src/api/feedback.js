import request from '@/utils/request'

export const feedbackApi = {
    submit(data) {
        return request.post('/feedback/submit', data)
    },

    getAllFeedbacks() {
        return request.get('/feedback/all')
    },

    getActivityFeedbacks(activityId) {
        return request.get(`/feedback/activity/${activityId}`)
    },

    getMyFeedbacks(activityId) {
        const params = activityId ? { activityId } : {}
        return request.get('/feedback/my', { params })
    },

    getCollegeFeedbacks(activityId) {
        const params = activityId ? { activityId } : {}
        return request.get('/feedback/college', { params })
    }
}