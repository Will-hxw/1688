import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { showMessage } from './messageHolder'
import { useAuthStore } from '../stores/authStore'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { code, message: msg, data } = response.data
    if (code === 200) {
      return data
    }
    showMessage.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  (error: AxiosError<{ code: number; message: string; data: unknown; timestamp: number }>) => {
    const { response } = error
    if (response) {
      const { status, data } = response
      // 获取当前路径，判断是否在登录/注册页面
      const isAuthPage = window.location.pathname === '/login' || window.location.pathname === '/register'
      // 从Result结构中获取message
      const errorMessage = data?.message || '请求失败'
      
      switch (status) {
        case 400:
          showMessage.error(errorMessage)
          break
        case 401:
          // 登录页面显示具体错误信息，其他页面跳转登录
          if (isAuthPage) {
            showMessage.error(errorMessage)
          } else {
            showMessage.error('未登录或令牌已过期')
            useAuthStore.getState().logout()
            window.location.href = '/login'
          }
          break
        case 403:
          showMessage.error(errorMessage)
          break
        case 404:
          showMessage.error(errorMessage)
          break
        case 409:
          // 注册时用户已存在
          showMessage.error(errorMessage)
          break
        case 500:
          showMessage.error(errorMessage)
          break
        default:
          showMessage.error(errorMessage)
      }
    } else {
      showMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
