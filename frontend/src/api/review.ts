import request from '../utils/request'
import type { PageResult } from './product'

export interface Review {
  id: number
  orderId: number
  productId: number
  productName: string
  buyerId: number
  buyerNickname: string
  rating: number
  content: string
  deleted: boolean
  createdAt: string
}

export interface CreateReviewParams {
  orderId: number
  rating: number
  content?: string
}

// 创建评价
export const createReview = (params: CreateReviewParams): Promise<number> => {
  return request.post('/reviews', params)
}

// 获取商品评价
export const getProductReviews = (productId: number, page: number, pageSize: number): Promise<PageResult<Review>> => {
  return request.get(`/products/${productId}/reviews`, { params: { page, pageSize } })
}

// 获取我的评价
export const getMyReviews = (page: number, pageSize: number): Promise<PageResult<Review>> => {
  return request.get('/reviews/my', { params: { page, pageSize } })
}
