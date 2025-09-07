import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../utils/AuthContext';

const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    // 如果尚在確認身份，可以顯示載入動畫
    return <div>載入中...</div>;
  }

  if (!isAuthenticated) {
    // 使用者未認證，重定向到登入頁面，並保留原本想前往的 URL
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 認證成功，顯示受保護的內容
  return children;
};

export default PrivateRoute;
