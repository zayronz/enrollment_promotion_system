import axios from 'axios'
import { MessagePlugin } from 'tdesign-vue-next'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            MessagePlugin.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    },
    error => {
        if (error.response?.status === 401) {
            MessagePlugin.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            window.location.href = '/login'
        } else if (error.response?.data?.message) {
            // 优先使用后端返回的具体错误信息
            MessagePlugin.error(error.response.data.message)
        } else {
            MessagePlugin.error(error.message || '网络错误')
        }
        return Promise.reject(error)
    }
)

export default request