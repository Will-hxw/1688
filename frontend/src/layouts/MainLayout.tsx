import { useState } from 'react'
import { Outlet, useNavigate } from 'react-router-dom'
import { Layout, Input, Button, Dropdown, Avatar, Space } from 'antd'
import { UserOutlined, ShopOutlined, ShoppingCartOutlined, LogoutOutlined, SettingOutlined } from '@ant-design/icons'
import { showMessage } from '../utils/messageHolder'
import { useAuthStore } from '../stores/authStore'
import type { MenuProps } from 'antd'

const { Header, Content, Footer } = Layout
const { Search } = Input

const MainLayout = () => {
  const navigate = useNavigate()
  const { user, isLoggedIn, isAdmin, logout } = useAuthStore()
  const [searchValue, setSearchValue] = useState('')

  const handleSearch = (value: string) => {
    navigate(`/?keyword=${encodeURIComponent(value)}`)
  }

  const handleLogout = () => {
    logout()
    showMessage.success('已退出登录')
    navigate('/')
  }

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'my-products',
      icon: <ShopOutlined />,
      label: '我的商品',
      onClick: () => navigate('/my-products'),
    },
    {
      key: 'buyer-orders',
      icon: <ShoppingCartOutlined />,
      label: '我买到的',
      onClick: () => navigate('/buyer-orders'),
    },
    {
      key: 'seller-orders',
      icon: <ShopOutlined />,
      label: '我卖出的',
      onClick: () => navigate('/seller-orders'),
    },
    ...(isAdmin() ? [{
      key: 'admin',
      icon: <SettingOutlined />,
      label: '管理后台',
      onClick: () => navigate('/admin'),
    }] : []),
    { type: 'divider' as const },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ 
        background: '#fff', 
        padding: '0 50px', 
        display: 'flex', 
        alignItems: 'center',
        justifyContent: 'space-between',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
          <div 
            style={{ 
              fontSize: 24, 
              fontWeight: 'bold', 
              color: '#ff6600', 
              cursor: 'pointer' 
            }}
            onClick={() => navigate('/')}
          >
            CQU抽象集市
          </div>
          <Search
            placeholder="搜索商品"
            allowClear
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onSearch={handleSearch}
            style={{ width: 400 }}
          />
        </div>
        
        <Space size="middle">
          {isLoggedIn() ? (
            <>
              <Button type="primary" onClick={() => navigate('/publish')}>
                发布商品
              </Button>
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                <Space style={{ cursor: 'pointer' }}>
                  <Avatar icon={<UserOutlined />} src={user?.avatar} />
                  <span>{user?.nickname}</span>
                </Space>
              </Dropdown>
            </>
          ) : (
            <>
              <Button onClick={() => navigate('/login')}>登录</Button>
              <Button type="primary" onClick={() => navigate('/register')}>注册</Button>
            </>
          )}
        </Space>
      </Header>
      
      <Content style={{ padding: '24px 50px', background: '#f5f5f5' }}>
        <Outlet />
      </Content>
      
      <Footer style={{ textAlign: 'center', background: '#fff' }}>
        CQU抽象集市 ©2026 重庆大学计算机学院华晓蔚
      </Footer>
    </Layout>
  )
}

export default MainLayout
