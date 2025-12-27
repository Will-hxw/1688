import request from '../utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface RegisterParams {
  username: string
  password: string
  nickname: string
}

export interface LoginResult {
  token: string
  user: {
    id: number
    username: string
    nickname: string
    avatar?: string
    role: 'USER' | 'ADMIN'
  }
}

// 登录
export const login = (params: LoginParams): Promise<LoginResult> => {
  return request.post('/auth/login', params)
}

// 注册
export const register = (params: RegisterParams): Promise<void> => {
  return request.post('/auth/register', params)
}
