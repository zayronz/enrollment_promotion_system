import request from '@/utils/request'

export const auditApi = {
    // 单条审核
    audit(data) {
        return request.post('/audit/single', data)
    },

    // 批量审核
    batchAudit(data) {
        return request.post('/audit/batch', data)
    },

    // 获取审核历史
    getHistory(params) {
        return request.get('/audit/history', { params })
    }
}