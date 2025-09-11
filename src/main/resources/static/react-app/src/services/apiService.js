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
        console.log('[ApiService] 發送請求:', {
            url: config.url,
            method: config.method,
            baseURL: config.baseURL,
            fullURL: `${config.baseURL}${config.url}`
        });
        
        // 取得儲存的 token
        const token = localStorage.getItem('token');
        if (token) {
            console.log('[ApiService] 使用令牌:', token.substring(0, 20) + '...');

            // 確保不重複新增Bearer前綴
            config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
            console.log('[ApiService] 請求標頭:', config.headers.Authorization.substring(0, 30) + '...');
        } else {
            console.warn('[ApiService] 警告: 沒有找到 token');
        }
        return config;
    },
    (error) => {
        console.error('[ApiService] 請求攔截器錯誤:', error);
        return Promise.reject(error);
    }
);

/**
 * 回應攔截器，統一處理錯誤和 token 過期
 */
api.interceptors.response.use(
    (response) => {
        console.log('[ApiService] 請求成功:', {
            status: response.status,
            url: response.config.url,
            data: response.data
        });
        return response;
    },
    (error) => {
        console.error('[ApiService] 請求失敗:', {
            status: error.response?.status,
            statusText: error.response?.statusText,
            url: error.config?.url,
            fullURL: `${error.config?.baseURL}${error.config?.url}`,
            message: error.message,
            responseData: error.response?.data
        });
        
        // 如果是401錯誤，清除 token 並重定向到登入頁面
        if (error.response && error.response.status === 401) {
            console.warn('[ApiService] 401 未授權，清除 token 並跳轉到登入頁');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        } else if (error.response?.status === 404) {
            console.error('[ApiService] 404 找不到資源，可能是路徑配置問題');
        } else if (!error.response) {
            console.error('[ApiService] 網路錯誤或伺服器無回應');
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
