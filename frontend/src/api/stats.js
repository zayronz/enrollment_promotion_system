import request from '@/utils/request'

export const statsApi = {
    // 学校端仪表盘数据
    getDashboard() {
        return request.get('/stats/dashboard')
    },

    // 学院端统计数据
    getCollegeStats() {
        return request.get('/stats/college')
    }
}
