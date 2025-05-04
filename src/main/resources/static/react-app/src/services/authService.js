import apiService, { setAuthToken } from './apiService';

/**
 * 使用者認證服務
 */
const authService = {
    /**
     * 使用者登入
     * @param {string} username - 使用者名
     * @param {string} password - 密碼
     * @returns {Promise} - 登入結果
     */
    login: async (username, password) => {
        const response = await apiService.post('/auth/login', {username, password});
        console.log('登入回應完整數據:', response.data);

        // 如果登入成功，設置 token
        if (response.data && response.data.data && response.data.data.accessToken) {
            const {accessToken, tokenType, secureToken} = response.data.data;
            console.log('Token數據解析:', {accessToken, tokenType, secureToken});
            
            // 使用安全令牌（如果可用）
            if (secureToken) {
                console.log('使用加密安全令牌:', secureToken);
                // 保存加密令牌 - 不需要新增前綴
                setAuthToken(secureToken);
            } else {
                // 向後兼容，使用標準 JWT
                console.log('使用標準JWT令牌 (secureToken不存在)');
                const normalizedTokenType = (tokenType || 'Bearer').trim();
                const token = `${normalizedTokenType} ${accessToken}`;
                console.log('設置標準JWT:', token);
                setAuthToken(token);
            }
            
            // 保存使用者資訊
            if (response.data.data.user) {
                localStorage.setItem('user', JSON.stringify(response.data.data.user));
            }
        }

        return response.data;
    },

    /**
     * 使用者登出
     */
    logout: async () => {
        try {
            const token = localStorage.getItem('token');
            if (token) {
                // 呼叫後端登出 API，將令牌加入黑名單
                try {
                    await apiService.post('/auth/logout');
                } catch (e) {
                    console.error('登出 API 呼叫失敗', e);
                    // 繼續處理其餘登出邏輯
                }
            }

            // 清除本機存儲的認證訊息
            setAuthToken(null);
            localStorage.removeItem('user');

            return {success: true};
        } catch (error) {
            console.error('登出失敗', error);
            return {success: false, error};
        }
    },

    /**
     * 取得當前使用者訊息
     * @returns {Object|null} - 當前使用者訊息或 null
     */
    getCurrentUser: () => {
        const userStr = localStorage.getItem('user');
        return userStr ? JSON.parse(userStr) : null;
    }
};

export default authService;
