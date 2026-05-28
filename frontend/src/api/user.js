import request from '@/utils/request'

export const userApi = {
    // 登录
    login(data) {
        return request.post('/user/login', data)
    },

    // 获取用户信息
    getUserInfo() {
        return request.get('/user/info')
    },

    // 获取用户列表
    getUserList(params) {
        return request.get('/user/list', { params })
    },

    // 创建用户
    createUser(data) {
        return request.post('/user/create', data)
    }
}