import request from '@/utils/request'

export const userApi = {
    // 登录
    login(data) {
        return request.post('/user/login', data)
    },

    // 注册
    register(data) {
        return request.post('/user/register', data)
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
    },

    // 更新用户
    updateUser(id, data) {
        return request.put(`/user/${id}`, data)
    },

    // 删除用户
    deleteUser(id) {
        return request.delete(`/user/${id}`)
    },

    // 修改密码
    changePassword(data) {
        return request.put('/user/password', data)
    },

    // 更新头像
    updateAvatar(avatarUrl) {
        return request({
            method: 'put',
            url: '/user/avatar',
            data: { avatarUrl }
        })
    },

    // 忘记密码
    forgotPassword(data) {
        return request.post('/user/forgot-password', data)
    },

    // 发送忘记密码邮箱验证码
    sendForgotPasswordCode(data) {
        return request.post('/user/forgot-password/code', data)
    },

    // 验证忘记密码邮箱验证码
    verifyForgotPasswordCode(data) {
        return request.post('/user/forgot-password/verify', data)
    },

    // 本地演示：模拟统一身份认证平台设置新密码
    identityPasswordReset(data) {
        return request.post('/user/identity-password-reset', data)
    }
}
