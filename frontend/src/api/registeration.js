import request from '@/utils/request'

export const registrationApi = {
    // 提交报名
    submit(data) {
        return request.post('/registration/submit', data)
    },

    // 获取我的报名列表（支持分页参数）
    getMyRegistrations(params = {}) {
        return request.get('/registration/my', { params })
    },

    // 获取我的分组和组员
    getMyTeams() {
        return request.get('/registration/my-teams')
    },

    // 获取当前用户可加入的其他招宣组
    getAvailableTeams() {
        return request.get('/registration/available-teams')
    },

    // 获取报名详情
    getRegistrationDetail(id) {
        return request.get(`/registration/${id}`)
    },

    // 撤回报名
    withdraw(id) {
        return request.put(`/registration/${id}/withdraw`)
    },

    // 退出当前报名所在分组
    exitTeam(id) {
        return request.put(`/registration/${id}/exit-team`)
    },

    // 加入其他招宣组
    joinTeam(id, groupName) {
        return request.put(`/registration/${id}/join-team`, { groupName })
    },

    // 获取待审核列表（支持分页、筛选等参数）
    getPendingAudit(params = {}) {
        return request.get('/registration/pending', { params })
    }
}
