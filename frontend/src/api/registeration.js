import request from '@/utils/request'

export const registrationApi = {
    // 提交报名
    submit(data) {
        return request.post('/registration/submit', data)
    },

    // 获取我的报名列表
    getMyRegistrations() {
        return request.get('/registration/my')
    },

    // 获取报名详情
    getRegistrationDetail(id) {
        return request.get(`/registration/${id}`)
    },

    // 撤回报名
    withdraw(id) {
        return request.put(`/registration/${id}/withdraw`)
    },

    // 获取待审核列表
    getPendingAudit(node) {
        return request.get('/registration/pending', { params: { node } })
    }
}