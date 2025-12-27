import { useState, useEffect } from 'react'
import { Table, Button, Tag, Modal, Select, message } from 'antd'
import { getAdminOrders, updateOrderStatus } from '../../api/admin'
import type { Order } from '../../api/order'

const { Option } = Select

const statusMap: Record<string, { text: string; color: string }> = {
  CREATED: { text: '待发货', color: 'blue' },
  SHIPPED: { text: '已发货', color: 'orange' },
  RECEIVED: { text: '已收货', color: 'green' },
  REVIEWED: { text: '已评价', color: 'default' },
  CANCELED: { text: '已取消', color: 'red' },
}

// 状态转换规则
const validTransitions: Record<string, string[]> = {
  CREATED: ['SHIPPED', 'CANCELED'],
  SHIPPED: ['RECEIVED', 'CANCELED'],
  RECEIVED: ['REVIEWED'],
  REVIEWED: [],
  CANCELED: [],
}

const AdminOrders = () => {
  const [orders, setOrders] = useState<Order[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)
  const [statusModalVisible, setStatusModalVisible] = useState(false)
  const [editingOrder, setEditingOrder] = useState<Order | null>(null)
  const [newStatus, setNewStatus] = useState<string>('')

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const result = await getAdminOrders(page, 10)
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

  const handleEditStatus = (order: Order) => {
    setEditingOrder(order)
    setNewStatus('')
    setStatusModalVisible(true)
  }

  const handleStatusSubmit = async () => {
    if (!editingOrder || !newStatus) return
    try {
      await updateOrderStatus(editingOrder.id, newStatus)
      message.success('状态更新成功')
      setStatusModalVisible(false)
      fetchOrders()
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
      title: '商品',
      render: (_: unknown, record: Order) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <img src={record.productImage || '/placeholder.png'} alt="" style={{ width: 40, height: 40, objectFit: 'cover' }} />
          <span>{record.productName}</span>
        </div>
      ),
    },
    {
      title: '买家',
      dataIndex: 'buyerNickname',
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
      render: (_: unknown, record: Order) => {
        const transitions = validTransitions[record.status] || []
        return transitions.length > 0 ? (
          <Button type="link" onClick={() => handleEditStatus(record)}>修改状态</Button>
        ) : null
      },
    },
  ]

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>订单管理</h2>
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
        title="修改订单状态"
        open={statusModalVisible}
        onCancel={() => setStatusModalVisible(false)}
        onOk={handleStatusSubmit}
        okButtonProps={{ disabled: !newStatus }}
      >
        <div style={{ marginBottom: 16 }}>
          当前状态：<Tag color={statusMap[editingOrder?.status || '']?.color}>
            {statusMap[editingOrder?.status || '']?.text}
          </Tag>
        </div>
        <Select
          style={{ width: '100%' }}
          placeholder="选择新状态"
          value={newStatus || undefined}
          onChange={setNewStatus}
        >
          {(validTransitions[editingOrder?.status || ''] || []).map(status => (
            <Option key={status} value={status}>
              {statusMap[status]?.text}
            </Option>
          ))}
        </Select>
      </Modal>
    </div>
  )
}

export default AdminOrders
