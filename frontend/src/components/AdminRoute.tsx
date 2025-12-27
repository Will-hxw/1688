import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../stores/authStore'

interface AdminRouteProps {
  children: React.ReactNode
}

const AdminRoute = ({ children }: AdminRouteProps) => {
  const { isLoggedIn, isAdmin } = useAuthStore()
  
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />
  }
  
  if (!isAdmin()) {
    return <Navigate to="/" replace />
  }
  
  return <>{children}</>
}

export default AdminRoute
