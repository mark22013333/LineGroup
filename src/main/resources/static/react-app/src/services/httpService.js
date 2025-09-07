import axios from 'axios';

// 建立 axios 實例
const instance = axios.create({
  baseURL: '', // 使用相對路徑，不需要環境變數
  timeout: 15000, // 請求超時時間
  headers: {
    'Content-Type': 'application/json',
  },
});

// 請求攔截器
instance.interceptors.request.use(
  (config) => {
    // 從 localStorage 取得 token（與登入流程統一使用 'token' 作為 key）
    const token = localStorage.getItem('token');
    if (token) {
      // 確保不重複新增 Bearer 前綴
      const value = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      config.headers['Authorization'] = value;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 回應攔截器
instance.interceptors.response.use(
  (response) => {
    // 直接返回資料部分
    return response.data;
  },
  (error) => {
    const { response } = error;
    
    // 未授權時（例如 token 過期）執行登出
    if (response && response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // 如果不是登入頁面則跳轉到登入頁
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }

    // 全域錯誤處理
    const errorMessage = response?.data?.message || '系統錯誤，請稍後再試';
    
    // 將處理後的錯誤訊息傳遞給調用者
    return Promise.reject({
      message: errorMessage,
      status: response?.status,
      data: response?.data
    });
  }
);

// 封裝 HTTP 方法
const http = {
  get: (url, config) => instance.get(url, config),
  post: (url, data, config) => instance.post(url, data, config),
  put: (url, data, config) => instance.put(url, data, config),
  patch: (url, data, config) => instance.patch(url, data, config),
  delete: (url, config) => instance.delete(url, config),
};

export default http;
