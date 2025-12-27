import { Navigate } from 'react-router-dom'
import { useAuthStore } from '../stores/authStore'

interface PrivateRouteProps {
  children: React.ReactNode
}

const PrivateRoute = ({ children }: PrivateRouteProps) => {
  const { isLoggedIn } = useAuthStore()
  
  if (!isLoggedIn()) {
    return <Navigate to="/login" replace />
  }
  
  return <>{children}</>
}

export default PrivateRoute
