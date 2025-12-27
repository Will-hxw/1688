import { useState, useEffect } from 'react'
import { Table, Button, Tag, Rate, Popconfirm } from 'antd'
import { getAdminReviews, deleteAdminReview } from '../../api/admin'
import { showMessage } from '../../utils/messageHolder'
import type { Review } from '../../api/review'

const AdminReviews = () => {
  const [reviews, setReviews] = useState<Review[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)

  const fetchReviews = async () => {
    setLoading(true)
    try {
      const result = await getAdminReviews(page, 10)
      setReviews(result.list)
      setTotal(result.total)
    } catch {
      // 错误已在拦截器中处理
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchReviews()
  }, [page])

  const handleDelete = async (reviewId: number) => {
    try {
      await deleteAdminReview(reviewId)
      showMessage.success('删除成功')
      fetchReviews()
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
      dataIndex: 'productName',
    },
    {
      title: '评价者',
      dataIndex: 'buyerNickname',
    },
    {
      title: '评分',
      dataIndex: 'rating',
      render: (rating: number) => <Rate disabled value={rating} style={{ fontSize: 14 }} />,
    },
    {
      title: '内容',
      dataIndex: 'content',
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'deleted',
      render: (deleted: boolean) => (
        <Tag color={deleted ? 'red' : 'green'}>
          {deleted ? '已删除' : '正常'}
        </Tag>
      ),
    },
    {
      title: '评价时间',
      dataIndex: 'createdAt',
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      render: (_: unknown, record: Review) => (
        !record.deleted && (
          <Popconfirm title="确定删除该评价吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" danger>删除</Button>
          </Popconfirm>
        )
      ),
    },
  ]

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>评价管理</h2>
      <Table
        dataSource={reviews}
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
    </div>
  )
}

export default AdminReviews
