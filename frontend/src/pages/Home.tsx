import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Card, Row, Col, Select, InputNumber, Button, Pagination, Empty, Spin, Space } from 'antd'
import { searchProducts } from '../api/product'
import type { Product, SearchParams } from '../api/product'

const { Option } = Select

const categories = ['电子产品', '书籍教材', '生活用品', '服饰鞋包', '运动户外', '其他']

const Home = () => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [filters, setFilters] = useState<SearchParams>({
    keyword: searchParams.get('keyword') || '',
    category: undefined,
    minPrice: undefined,
    maxPrice: undefined,
    sortBy: 'createdAt',
    sortOrder: 'desc',
    page: 1,
    pageSize: 12,
  })

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const result = await searchProducts(filters)
      setProducts(result.list)
      setTotal(result.total)
    } catch {
      // 错误已在拦截器中处理
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProducts()
  }, [filters])

  useEffect(() => {
    const keyword = searchParams.get('keyword')
    if (keyword !== filters.keyword) {
      setFilters(prev => ({ ...prev, keyword: keyword || '', page: 1 }))
    }
  }, [searchParams])

  const handleFilterChange = (key: keyof SearchParams, value: unknown) => {
    setFilters(prev => ({ ...prev, [key]: value, page: 1 }))
  }

  const handlePageChange = (page: number) => {
    setFilters(prev => ({ ...prev, page }))
  }

  return (
    <div>
      {/* 筛选栏 */}
      <Card style={{ marginBottom: 16 }}>
        <Space wrap size="middle">
          <span>分类：</span>
          <Select
            style={{ width: 150 }}
            placeholder="全部分类"
            allowClear
            value={filters.category}
            onChange={(v) => handleFilterChange('category', v)}
          >
            {categories.map(cat => (
              <Option key={cat} value={cat}>{cat}</Option>
            ))}
          </Select>
          
          <span>价格：</span>
          <InputNumber
            style={{ width: 100 }}
            placeholder="最低"
            min={0}
            value={filters.minPrice}
            onChange={(v) => handleFilterChange('minPrice', v)}
          />
          <span>-</span>
          <InputNumber
            style={{ width: 100 }}
            placeholder="最高"
            min={0}
            value={filters.maxPrice}
            onChange={(v) => handleFilterChange('maxPrice', v)}
          />
          
          <span>排序：</span>
          <Select
            style={{ width: 120 }}
            value={filters.sortBy}
            onChange={(v) => handleFilterChange('sortBy', v)}
          >
            <Option value="createdAt">最新发布</Option>
            <Option value="price">价格</Option>
          </Select>
          <Select
            style={{ width: 100 }}
            value={filters.sortOrder}
            onChange={(v) => handleFilterChange('sortOrder', v)}
          >
            <Option value="desc">降序</Option>
            <Option value="asc">升序</Option>
          </Select>
          
          <Button onClick={() => setFilters({
            keyword: '',
            category: undefined,
            minPrice: undefined,
            maxPrice: undefined,
            sortBy: 'createdAt',
            sortOrder: 'desc',
            page: 1,
            pageSize: 12,
          })}>
            重置
          </Button>
        </Space>
      </Card>

      {/* 商品列表 */}
      <Spin spinning={loading}>
        {products.length > 0 ? (
          <>
            <Row gutter={[16, 16]}>
              {products.map(product => (
                <Col key={product.id} xs={24} sm={12} md={8} lg={6}>
                  <Card
                    hoverable
                    cover={
                      <img
                        alt={product.name}
                        src={product.imageUrl || '/placeholder.png'}
                        style={{ height: 200, objectFit: 'cover' }}
                      />
                    }
                    onClick={() => navigate(`/products/${product.id}`)}
                  >
                    <Card.Meta
                      title={product.name}
                      description={
                        <div>
                          <div style={{ color: '#ff6600', fontSize: 18, fontWeight: 'bold' }}>
                            ¥{product.price}
                          </div>
                          <div style={{ color: '#999', fontSize: 12, marginTop: 4 }}>
                            {product.sellerNickname} · {product.category}
                          </div>
                        </div>
                      }
                    />
                  </Card>
                </Col>
              ))}
            </Row>
            
            <div style={{ textAlign: 'center', marginTop: 24 }}>
              <Pagination
                current={filters.page}
                pageSize={filters.pageSize}
                total={total}
                onChange={handlePageChange}
                showSizeChanger={false}
              />
            </div>
          </>
        ) : (
          <Empty description="暂无商品" />
        )}
      </Spin>
    </div>
  )
}

export default Home
