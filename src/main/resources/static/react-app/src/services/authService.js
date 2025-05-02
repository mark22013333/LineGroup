import axios from 'axios';

// 創建 axios 實例
const api = axios.create({
  baseURL: '/api/v1', // 根據您的 API 路徑進行調整
  headers: {
    'Content-Type': 'application/json'
  }
});

// 請求攔截器，添加 token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = token;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 回應攔截器，處理 token 過期等情況
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // token 過期，清除本地存儲並重定向到登入頁面
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

const authService = {
  // 用戶登入
  login: async (username, password) => {
    const response = await api.post('/auth/login', { username, password });
    return response.data;
  },

  // 用戶登出
  logout: async () => {
    try {
      // 如果後端有登出 API，可以在這裡調用
      // await api.post('/auth/logout');
      
      // 清除本地存儲
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return { success: true };
    } catch (error) {
      console.error('登出錯誤:', error);
      throw error;
    }
  },

  // 取得當前用戶資訊
  getCurrentUser: async () => {
    const response = await api.get('/auth/current-user');
    return response.data;
  }
};

export default authService;
