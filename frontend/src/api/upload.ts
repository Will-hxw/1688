import request from '../utils/request'

export interface UploadResult {
  imageUrl: string
}

// 上传图片
export const uploadImage = (file: File): Promise<UploadResult> => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
