import { useState, useEffect } from 'react'
import { Table, Button, Tag, Popconfirm } from 'antd'
import { getUsers, disableUser, enableUser } from '../../api/admin'
import { showMessage } from '../../utils/messageHolder'
import type { User } from '../../api/admin'

const statusMap: Record<string, { text: string; color: string }> = {
  ACTIVE: { text: '正常', color: 'green' },
  DISABLED: { text: '禁用', color: 'red' },
}

const roleMap: Record<string, string> = {
  USER: '普通用户',
  ADMIN: '管理员',
}

const AdminUsers = () => {
  const [users, setUsers] = useState<User[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [page, setPage] = useState(1)

  const fetchUsers = async () => {
    setLoading(true)
    try {
      const result = await getUsers(page, 10)
      setUsers(result.list)
      setTotal(result.total)
    } catch {
      // 错误已在拦截器中处理
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchUsers()
  }, [page])

  const handleDisable = async (userId: number) => {
    try {
      await disableUser(userId)
      showMessage.success('禁用成功')
      fetchUsers()
    } catch {
      // 错误已在拦截器中处理
    }
  }

  const handleEnable = async (userId: number) => {
    try {
      await enableUser(userId)
      showMessage.success('启用成功')
      fetchUsers()
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
      title: '用户名',
      dataIndex: 'username',
    },
    {
      title: '昵称',
      dataIndex: 'nickname',
    },
    {
      title: '角色',
      dataIndex: 'role',
      render: (role: string) => roleMap[role] || role,
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
      title: '注册时间',
      dataIndex: 'createdAt',
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      render: (_: unknown, record: User) => (
        record.status === 'ACTIVE' ? (
          <Popconfirm title="确定禁用该用户吗？" onConfirm={() => handleDisable(record.id)}>
            <Button type="link" danger>禁用</Button>
          </Popconfirm>
        ) : (
          <Popconfirm title="确定启用该用户吗？" onConfirm={() => handleEnable(record.id)}>
            <Button type="link">启用</Button>
          </Popconfirm>
        )
      ),
    },
  ]

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>用户管理</h2>
      <Table
        dataSource={users}
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

export default AdminUsers
