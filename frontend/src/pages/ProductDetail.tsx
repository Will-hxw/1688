import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Row, Col, Button, Descriptions, Avatar, Rate, List, Spin, message, Empty } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { getProductDetail } from '../api/product'
import type { Product } from '../api/product'
import { createOrder } from '../api/order'
import { getProductReviews } from '../api/review'
import type { Review } from '../api/review'
import { useAuthStore } from '../stores/authStore'
import { v4 as uuidv4 } from 'uuid'

const ProductDetail = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { isLoggedIn, user } = useAuthStore()
  const [product, setProduct] = useState<Product | null>(null)
  const [reviews, setReviews] = useState<Review[]>([])
  const [loading, setLoading] = useState(true)
  const [ordering, setOrdering] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      if (!id) return
      setLoading(true)
      try {
        const [productData, reviewsData] = await Promise.all([
          getProductDetail(Number(id)),
          getProductReviews(Number(id), 1, 10)
        ])
        setProduct(productData)
        setReviews(reviewsData.list)
      } catch {
        // 错误已在拦截器中处理
      } finally {
        setLoading(false)
      }
    }
    fetchData()
  }, [id])

  const handleBuy = async () => {
    if (!isLoggedIn()) {
      message.warning('请先登录')
      navigate('/login')
      return
    }
    
    if (product?.sellerId === user?.id) {
      message.warning('不能购买自己的商品')
      return
    }

    setOrdering(true)
    try {
      const idempotencyKey = uuidv4()
      await createOrder(Number(id), idempotencyKey)
      message.success('下单成功')
      navigate('/buyer-orders')
    } catch {
      // 错误已在拦截器中处理
    } finally {
      setOrdering(false)
    }
  }

  if (loading) {
    return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />
  }

  if (!product) {
    return <Empty description="商品不存在" />
  }

  return (
    <div>
      <Card>
        <Row gutter={24}>
          <Col span={10}>
            <img
              src={product.imageUrl || '/placeholder.png'}
              alt={product.name}
              style={{ width: '100%', borderRadius: 8 }}
            />
          </Col>
          <Col span={14}>
            <h1 style={{ marginBottom: 16 }}>{product.name}</h1>
            <div style={{ 
              fontSize: 32, 
              color: '#ff6600', 
              fontWeight: 'bold',
              marginBottom: 24 
            }}>
              ¥{product.price}
            </div>
            
            <Descriptions column={1} style={{ marginBottom: 24 }}>
              <Descriptions.Item label="分类">{product.category}</Descriptions.Item>
              <Descriptions.Item label="描述">{product.description}</Descriptions.Item>
              <Descriptions.Item label="发布时间">
                {new Date(product.createdAt).toLocaleString()}
              </Descriptions.Item>
            </Descriptions>
            
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: 12,
              padding: 16,
              background: '#f5f5f5',
              borderRadius: 8,
              marginBottom: 24
            }}>
              <Avatar icon={<UserOutlined />} />
              <span>{product.sellerNickname}</span>
            </div>
            
            {product.status === 'ON_SALE' ? (
              <Button 
                type="primary" 
                size="large" 
                onClick={handleBuy}
                loading={ordering}
                disabled={product.sellerId === user?.id}
              >
                {product.sellerId === user?.id ? '这是您的商品' : '立即购买'}
              </Button>
            ) : (
              <Button size="large" disabled>
                商品已售出
              </Button>
            )}
          </Col>
        </Row>
      </Card>

      {/* 评价列表 */}
      <Card title="商品评价" style={{ marginTop: 16 }}>
        {reviews.length > 0 ? (
          <List
            dataSource={reviews}
            renderItem={review => (
              <List.Item>
                <List.Item.Meta
                  avatar={<Avatar icon={<UserOutlined />} />}
                  title={
                    <div>
                      <span>{review.buyerNickname}</span>
                      <Rate disabled value={review.rating} style={{ marginLeft: 12, fontSize: 14 }} />
                    </div>
                  }
                  description={
                    <div>
                      <div>{review.content}</div>
                      <div style={{ color: '#999', fontSize: 12, marginTop: 4 }}>
                        {new Date(review.createdAt).toLocaleString()}
                      </div>
                    </div>
                  }
                />
              </List.Item>
            )}
          />
        ) : (
          <Empty description="暂无评价" />
        )}
      </Card>
    </div>
  )
}

export default ProductDetail
