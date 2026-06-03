import { defineStore } from 'pinia'
import { userApi } from '@/api/user'
import { getToken, setToken, removeToken, setUser, getUser, clearAuth } from '@/utils/auth'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: getToken(),
        userInfo: getUser(),
        roles: []
    }),

    getters: {
        isLoggedIn: (state) => !!state.token,
        role: (state) => state.userInfo?.role || '',
        realName: (state) => state.userInfo?.realName || '',
        collegeId: (state) => state.userInfo?.collegeId || null,
        collegeName: (state) => state.userInfo?.collegeName || '',
        avatar: (state) => state.userInfo?.avatar || '',
        // 获取完整的头像URL，自动处理前缀
        avatarUrl: (state) => {
            const avatar = state.userInfo?.avatar
            if (!avatar) return ''
            // 如果已经是完整URL或者已包含前缀，直接返回
            if (avatar.startsWith('http') || avatar.startsWith('/api')) {
                return avatar
            }
            // 否则添加前缀
            return '/api/file/view/' + avatar
        }
    },

    actions: {
        async login(username, password) {
            try {
                const res = await userApi.login({ username, password })
                if (res.code === 200 && res.data) {
                    this.token = res.data
                    setToken(res.data)
                    await this.getUserInfo()
                    return true
                }
                return false
            } catch (error) {
                return false
            }
        },

        async getUserInfo() {
            try {
                const res = await userApi.getUserInfo()
                if (res.code === 200 && res.data) {
                    this.userInfo = res.data
                    this.roles = [res.data.role]
                    setUser(res.data)
                    return res.data
                }
                return null
            } catch (error) {
                return null
            }
        },

        async updateAvatar(avatarUrl) {
            try {
                await userApi.updateAvatar(avatarUrl)
                if (this.userInfo) {
                    this.userInfo.avatar = avatarUrl
                    setUser(this.userInfo)
                }
                return true
            } catch (error) {
                console.error('更新头像失败', error)
                return false
            }
        },

        logout() {
            this.token = ''
            this.userInfo = null
            this.roles = []
            clearAuth()
        },

        checkLogin() {
            if (this.token && !this.userInfo) {
                this.getUserInfo()
            }
        }
    }
})