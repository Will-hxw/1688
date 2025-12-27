import { Card, Descriptions, Avatar, Tag } from 'antd'
import { UserOutlined } from '@ant-design/icons'
import { useAuthStore } from '../stores/authStore'

const Profile = () => {
  const { user, token } = useAuthStore()

  if (!user || !token) {
    return null
  }

  return (
    <div style={{ maxWidth: 600, margin: '0 auto' }}>
      <Card title="个人信息">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Avatar size={80} icon={<UserOutlined />} src={user.avatar} />
          <h2 style={{ marginTop: 8, marginBottom: 0 }}>{user.nickname}</h2>
          <Tag color={user.role === 'ADMIN' ? 'red' : 'blue'} style={{ marginTop: 8 }}>
            {user.role === 'ADMIN' ? '管理员' : '普通用户'}
          </Tag>
        </div>
        
        <Descriptions column={1} bordered>
          <Descriptions.Item label="用户ID">{user.id}</Descriptions.Item>
          <Descriptions.Item label="用户名">{user.username}</Descriptions.Item>
          <Descriptions.Item label="昵称">{user.nickname}</Descriptions.Item>
          <Descriptions.Item label="角色">
            {user.role === 'ADMIN' ? '管理员' : '普通用户'}
          </Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  )
}

export default Profile
