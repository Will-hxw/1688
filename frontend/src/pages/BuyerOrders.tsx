import { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Tag, Modal, Form, Rate, Input, message, Popconfirm } from 'antd'
import { getBuyerOrders, receiveOrder, cancelOrder } from '../api/order'
import type { Order } from '../api/order'
import { createReview } from '../api/review'
import type { CreateReviewParams } from '../api/review'

const { TextArea } = Input

const statusMap: Record<string, { text: string; color: string }> = {
  CREATED: { text: '待发货', color: 'blue' },
  SHIPPED: { text: '已发货', color: 'orange' },
  RECEIVED: { text: '已收货', color: 'green' },
  REVIEWED: { text: '已评价', color: 'default' },
  CANCELED: { text: '已取消', color: 'red' },
}

const BuyerOrders = () => {
  const [orders, setOrders] = useState<Order[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [reviewModalVisible, setReviewModalVisible] = useState(false)
  const [reviewingOrder, setReviewingOrder] = useState<Order | null>(null)
  const [form] = Form.useForm()

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const result = await getBuyerOrders(page, 10)
      setOrders(result.list)
      setTotal(result.total)
    } catch {
      // 错误已在拦截器中处理
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchOrders()
  }, [page])

  const handleReceive = async (orderId: number) => {
    try {
      await receiveOrder(orderId)
      message.success('确认收货成功')
      fetchOrders()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleCancel = async (orderId: number) => {
    try {
      await cancelOrder(orderId)
      message.success('取消订单成功')
      fetchOrders()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleReview = (order: Order) => {
    setReviewingOrder(order)
    form.resetFields()
    setReviewModalVisible(true)
  }

  const handleReviewSubmit = async (values: Omit<CreateReviewParams, 'orderId'>) => {
    if (!reviewingOrder) return
    try {
      await createReview({ ...values, orderId: reviewingOrder.id })
      message.success('评价成功')
      setReviewModalVisible(false)
      fetchOrders()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const columns = [
    {
      title: '商品',
      render: (_: unknown, record: Order) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <img 
            src={record.productImage || '/placeholder.png'} 
            alt="" 
            style={{ width: 60, height: 60, objectFit: 'cover' }} 
          />
          <span>{record.productName}</span>
        </div>
      ),
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
      title: '状态',
      dataIndex: 'status',
      render: (status: string) => {
        const config = statusMap[status] || { text: status, color: 'default' }
        return <Tag color={config.color}>{config.text}</Tag>
      },
    },
    {
      title: '下单时间',
      dataIndex: 'createdAt',
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      render: (_: unknown, record: Order) => (
        <Space>
          {record.status === 'CREATED' && (
            <Popconfirm title="确定取消订单吗？" onConfirm={() => handleCancel(record.id)}>
              <Button type="link" danger>取消</Button>
            </Popconfirm>
          )}
          {record.status === 'SHIPPED' && (
            <Popconfirm title="确定收货吗？" onConfirm={() => handleReceive(record.id)}>
              <Button type="link">确认收货</Button>
            </Popconfirm>
          )}
          {record.status === 'RECEIVED' && (
            <Button type="link" onClick={() => handleReview(record)}>评价</Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <Card title="我买到的">
      <Table
        dataSource={orders}
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
        title="评价商品"
        open={reviewModalVisible}
        onCancel={() => setReviewModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleReviewSubmit}>
          <Form.Item
            name="rating"
            label="评分"
            rules={[{ required: true, message: '请选择评分' }]}
          >
            <Rate />
          </Form.Item>
          <Form.Item
            name="content"
            label="评价内容"
          >
            <TextArea rows={4} placeholder="请输入评价内容（选填）" />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              提交评价
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}

export default BuyerOrders
