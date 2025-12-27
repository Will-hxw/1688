import request from '../utils/request'
import type { PageResult } from './product'

export interface Order {
  id: number
  buyerId: number
  buyerNickname: string
  sellerId: number
  sellerNickname: string
  productId: number
  productName: string
  productImage: string
  price: number
  status: string
  canceledBy?: string
  createdAt: string
}

// 创建订单
export const createOrder = (productId: number, idempotencyKey: string): Promise<number> => {
  return request.post('/orders', { productId }, {
    headers: { 'Idempotency-Key': idempotencyKey }
  })
}

// 发货
export const shipOrder = (orderId: number): Promise<void> => {
  return request.post(`/orders/${orderId}/ship`)
}

// 确认收货
export const receiveOrder = (orderId: number): Promise<void> => {
  return request.post(`/orders/${orderId}/receive`)
}

// 取消订单
export const cancelOrder = (orderId: number): Promise<void> => {
  return request.post(`/orders/${orderId}/cancel`)
}

// 获取买家订单
export const getBuyerOrders = (page: number, pageSize: number): Promise<PageResult<Order>> => {
  return request.get('/orders/buyer', { params: { page, pageSize } })
}

// 获取卖家订单
export const getSellerOrders = (page: number, pageSize: number): Promise<PageResult<Order>> => {
  return request.get('/orders/seller', { params: { page, pageSize } })
}
