import { useState, useEffect } from 'react'
import { Card, Table, Button, Space, Tag, Popconfirm } from 'antd'
import { getSellerOrders, shipOrder, cancelOrder } from '../api/order'
import type { Order } from '../api/order'
import { showMessage } from '../utils/messageHolder'

const statusMap: Record<string, { text: string; color: string }> = {
  CREATED: { text: '待发货', color: 'blue' },
  SHIPPED: { text: '已发货', color: 'orange' },
  RECEIVED: { text: '已收货', color: 'green' },
  REVIEWED: { text: '已评价', color: 'default' },
  CANCELED: { text: '已取消', color: 'red' },
}

const SellerOrders = () => {
  const [orders, setOrders] = useState<Order[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const result = await getSellerOrders(page, 10)
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

  const handleShip = async (orderId: number) => {
    try {
      await shipOrder(orderId)
      showMessage.success('发货成功')
      fetchOrders()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleCancel = async (orderId: number) => {
    try {
      await cancelOrder(orderId)
      showMessage.success('取消订单成功')
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
      title: '买家',
      dataIndex: 'buyerNickname',
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
            <>
              <Popconfirm title="确定发货吗？" onConfirm={() => handleShip(record.id)}>
                <Button type="link">发货</Button>
              </Popconfirm>
              <Popconfirm title="确定取消订单吗？" onConfirm={() => handleCancel(record.id)}>
                <Button type="link" danger>取消</Button>
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ]

  return (
    <Card title="我卖出的">
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
    </Card>
  )
}

export default SellerOrders
