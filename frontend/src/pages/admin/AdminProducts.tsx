import { useState, useEffect } from 'react'
import { Table, Button, Tag, Modal, Form, Input, InputNumber, Select, message, Popconfirm, Space } from 'antd'
import { getAdminProducts, updateAdminProduct, deleteAdminProduct } from '../../api/admin'
import type { Product, UpdateProductParams } from '../../api/product'

const { TextArea } = Input
const { Option } = Select

const categories = ['电子产品', '书籍教材', '生活用品', '服饰鞋包', '运动户外', '其他']

const statusMap: Record<string, { text: string; color: string }> = {
  ON_SALE: { text: '在售', color: 'green' },
  SOLD: { text: '已售', color: 'orange' },
}

const AdminProducts = () => {
  const [products, setProducts] = useState<Product[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [editingProduct, setEditingProduct] = useState<Product | null>(null)
  const [form] = Form.useForm()

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const result = await getAdminProducts(page, 10)
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
  }, [page])

  const handleEdit = (product: Product) => {
    setEditingProduct(product)
    form.setFieldsValue({
      name: product.name,
      description: product.description,
      price: product.price,
      category: product.category,
    })
    setEditModalVisible(true)
  }

  const handleEditSubmit = async (values: UpdateProductParams) => {
    if (!editingProduct) return
    try {
      await updateAdminProduct(editingProduct.id, values)
      message.success('更新成功')
      setEditModalVisible(false)
      fetchProducts()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteAdminProduct(id)
      message.success('删除成功')
      fetchProducts()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '图片',
      dataIndex: 'imageUrl',
      width: 80,
      render: (url: string) => (
        <img src={url || '/placeholder.png'} alt="" style={{ width: 60, height: 60, objectFit: 'cover' }} />
      ),
    },
    {
      title: '商品名称',
      dataIndex: 'name',
    },
    {
      title: '卖家',
      dataIndex: 'sellerNickname',
    },
    {
      title: '价格',
      dataIndex: 'price',
      render: (price: number) => <span style={{ color: '#ff6600' }}>¥{price}</span>,
    },
    {
      title: '分类',
      dataIndex: 'category',
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status: string) => {
        const config = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      },
    },
    {
      title: '操作',
      render: (_: unknown, record: Product) => (
        <Space>
          <Button type="link" onClick={() => handleEdit(record)}>编辑</Button>
          {record.status === 'ON_SALE' && (
            <Popconfirm title="确定删除该商品吗？" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" danger>删除</Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>商品管理</h2>
      <Table
        dataSource={products}
        columns={columns}
        rowKey="id"
        loading={loading}
        pagination={{
          current: page,
          total,
          pageSize: 10,
          onChange: setPage,
        }}
      />

      <Modal
        title="编辑商品"
        open={editModalVisible}
        onCancel={() => setEditModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleEditSubmit}>
          <Form.Item name="name" label="商品名称" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="商品描述" rules={[{ required: true }]}>
            <TextArea rows={3} />
          </Form.Item>
          <Form.Item name="price" label="价格" rules={[{ required: true }]}>
            <InputNumber style={{ width: '100%' }} prefix="¥" precision={2} />
          </Form.Item>
          <Form.Item name="category" label="分类" rules={[{ required: true }]}>
            <Select>
              {categories.map(cat => (
                <Option key={cat} value={cat}>{cat}</Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>保存</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default AdminProducts
