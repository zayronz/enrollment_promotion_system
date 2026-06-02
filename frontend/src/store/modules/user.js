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
        avatar: (state) => state.userInfo?.avatar || ''
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