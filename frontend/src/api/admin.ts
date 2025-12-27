import request from '../utils/request'
import type { PageResult, Product, UpdateProductParams } from './product'
import type { Order } from './order'
import type { Review } from './review'

export interface User {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: string
  status: string
  createdAt: string
}

// 用户管理
export const getUsers = (page: number, pageSize: number): Promise<PageResult<User>> => {
  return request.get('/admin/users', { params: { page, pageSize } })
}

export const disableUser = (userId: number): Promise<void> => {
  return request.put(`/admin/users/${userId}/disable`)
}

export const enableUser = (userId: number): Promise<void> => {
  return request.put(`/admin/users/${userId}/enable`)
}

// 商品管理
export const getAdminProducts = (page: number, pageSize: number): Promise<PageResult<Product>> => {
  return request.get('/admin/products', { params: { page, pageSize } })
}

export const updateAdminProduct = (id: number, params: UpdateProductParams): Promise<void> => {
  return request.put(`/admin/products/${id}`, params)
}

export const deleteAdminProduct = (id: number): Promise<void> => {
  return request.delete(`/admin/products/${id}`)
}

// 订单管理
export const getAdminOrders = (page: number, pageSize: number): Promise<PageResult<Order>> => {
  return request.get('/admin/orders', { params: { page, pageSize } })
}

export const updateOrderStatus = (orderId: number, status: string): Promise<void> => {
  return request.put(`/admin/orders/${orderId}/status`, { status })
}

// 评价管理
export const getAdminReviews = (page: number, pageSize: number): Promise<PageResult<Review>> => {
  return request.get('/admin/reviews', { params: { page, pageSize } })
}

export const deleteAdminReview = (reviewId: number): Promise<void> => {
  return request.delete(`/admin/reviews/${reviewId}`)
}
