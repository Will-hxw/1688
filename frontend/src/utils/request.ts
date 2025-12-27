import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'
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
    message.error(msg || '请求失败')
    return Promise.reject(new Error(msg))
  },
  (error: AxiosError<{ code: number; message: string }>) => {
    const { response } = error
    if (response) {
      const { status, data } = response
      switch (status) {
        case 401:
          message.error('未登录或令牌已过期')
          useAuthStore.getState().logout()
          window.location.href = '/login'
          break
        case 403:
          message.error('权限不足')
          break
        case 404:
          message.error(data?.message || '资源不存在')
          break
        case 409:
          message.error(data?.message || '操作冲突')
          break
        default:
          message.error(data?.message || '服务器错误')
      }
    } else {
      message.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
