import request from '../utils/request'

export interface Product {
  id: number
  sellerId: number
  sellerNickname: string
  name: string
  description: string
  price: number
  imageUrl: string
  category: string
  status: string
  createdAt: string
}

export interface PageResult<T> {
  page: number
  pageSize: number
  total: number
  list: T[]
}

export interface SearchParams {
  keyword?: string
  category?: string
  minPrice?: number
  maxPrice?: number
  sortBy?: 'price' | 'createdAt'
  sortOrder?: 'asc' | 'desc'
  page?: number
  pageSize?: number
}

export interface CreateProductParams {
  name: string
  description: string
  price: number
  imageUrl: string
  category: string
}

export interface UpdateProductParams {
  name?: string
  description?: string
  price?: number
  imageUrl?: string
  category?: string
}

// 搜索商品
export const searchProducts = (params: SearchParams): Promise<PageResult<Product>> => {
  return request.get('/products', { params })
}

// 获取商品详情
export const getProductDetail = (id: number): Promise<Product> => {
  return request.get(`/products/${id}`)
}

// 创建商品
export const createProduct = (params: CreateProductParams): Promise<number> => {
  return request.post('/products', params)
}

// 更新商品
export const updateProduct = (id: number, params: UpdateProductParams): Promise<void> => {
  return request.put(`/products/${id}`, params)
}

// 删除商品
export const deleteProduct = (id: number): Promise<void> => {
  return request.delete(`/products/${id}`)
}

// 获取我的商品
export const getMyProducts = (page: number, pageSize: number): Promise<PageResult<Product>> => {
  return request.get('/users/me/products', { params: { page, pageSize } })
}
