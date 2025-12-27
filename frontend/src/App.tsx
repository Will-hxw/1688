import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { ConfigProvider, App as AntdApp } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { useEffect } from 'react'
import { setMessageApi } from './utils/messageHolder'
import MainLayout from './layouts/MainLayout'
import AdminLayout from './layouts/AdminLayout'
import PrivateRoute from './components/PrivateRoute'
import AdminRoute from './components/AdminRoute'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import ProductDetail from './pages/ProductDetail'
import PublishProduct from './pages/PublishProduct'
import MyProducts from './pages/MyProducts'
import BuyerOrders from './pages/BuyerOrders'
import SellerOrders from './pages/SellerOrders'
import Profile from './pages/Profile'
import AdminUsers from './pages/admin/AdminUsers'
import AdminProducts from './pages/admin/AdminProducts'
import AdminOrders from './pages/admin/AdminOrders'
import AdminReviews from './pages/admin/AdminReviews'

// 内部组件，用于获取 message API（必须在 AntdApp 内部使用）
const AppContent = () => {
  // 使用 AntdApp 提供的 useApp hook 获取 message API
  const { message: messageApi } = AntdApp.useApp()
  
  useEffect(() => {
    // 立即设置 messageApi，确保在任何组件使用前就已经可用
    setMessageApi(messageApi)
  }, [messageApi])
  
  return (
    <BrowserRouter>
      <Routes>
        {/* 登录/注册页面 */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* 主布局 */}
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Home />} />
          <Route path="products/:id" element={<ProductDetail />} />
          
          {/* 需要登录的页面 */}
          <Route path="publish" element={
            <PrivateRoute><PublishProduct /></PrivateRoute>
          } />
          <Route path="my-products" element={
            <PrivateRoute><MyProducts /></PrivateRoute>
          } />
          <Route path="buyer-orders" element={
            <PrivateRoute><BuyerOrders /></PrivateRoute>
          } />
          <Route path="seller-orders" element={
            <PrivateRoute><SellerOrders /></PrivateRoute>
          } />
          <Route path="profile" element={
            <PrivateRoute><Profile /></PrivateRoute>
          } />
        </Route>
        
        {/* 管理后台布局 */}
        <Route path="/admin" element={
          <AdminRoute><AdminLayout /></AdminRoute>
        }>
          <Route index element={<Navigate to="/admin/users" replace />} />
          <Route path="users" element={<AdminUsers />} />
          <Route path="products" element={<AdminProducts />} />
          <Route path="orders" element={<AdminOrders />} />
          <Route path="reviews" element={<AdminReviews />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

const App = () => {
  return (
    <ConfigProvider
      locale={zhCN}
      theme={{
        token: {
          colorPrimary: '#ff6600',
        },
      }}
    >
      <AntdApp>
        <AppContent />
      </AntdApp>
    </ConfigProvider>
  )
}

export default App
