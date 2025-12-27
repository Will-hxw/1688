import request from '../utils/request'

export interface UpdateProfileParams {
  nickname?: string
  avatar?: string
}

// 更新个人信息
export const updateProfile = (params: UpdateProfileParams): Promise<void> => {
  return request.put('/users/me', params)
}
