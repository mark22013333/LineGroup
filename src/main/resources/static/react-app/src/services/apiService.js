import axios from 'axios';

/**
 * 建立並配置 axios 實體，用於所有 API 請求
 */
const api = axios.create({
    baseURL: '/api/v1',
    headers: {
        'Content-Type': 'application/json'
    }
});

/**
 * 設置身份驗證令牌
 */
export const setAuthToken = (token) => {
    if (token) {
        localStorage.setItem('token', token);
    } else {
        localStorage.removeItem('token');
    }
};

/**
 * 請求攔截器，自動新增 Authorization 頭
 */
api.interceptors.request.use(
    (config) => {
        // 取得存儲的 token
        const token = localStorage.getItem('token');
        if (token) {
            console.log('使用令牌:', token.substring(0, 20) + '...');
            
            // 檢查是否是加密JWT (不包含點號並且較長)
            if (!token.includes('.') && token.length > 100) {
                console.log('檢測到加密JWT格式，直接發送');
                config.headers.Authorization = token;
            } 
            // 標準JWT但沒有Bearer前綴
            else if (token.includes('.') && !token.startsWith('Bearer ') && !token.startsWith('bearer ')) {
                console.log('檢測到標準JWT格式，新增Bearer前綴');
                config.headers.Authorization = `Bearer ${token}`;
            } 
            // 已經格式化好的令牌
            else {
                console.log('使用已格式化的令牌');
                config.headers.Authorization = token;
            }
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

/**
 * 回應攔截器，統一處理錯誤和 token 過期
 */
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        // 如果是401錯誤，清除 token 並重定向到登入頁面
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

// 對外暴露API方法
export default {
    setAuthToken(token) {
        setAuthToken(token);
    },
    
    async get(url, config = {}) {
        console.log('API GET請求:', url, config);
        return api.get(url, config);
    },
    
    async post(url, data = {}, config = {}) {
        return api.post(url, data, config);
    },
    
    async put(url, data = {}, config = {}) {
        return api.put(url, data, config);
    },
    
    async delete(url, config = {}) {
        return api.delete(url, config);
    }
};
