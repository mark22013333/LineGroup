import apiService, {setAuthToken} from './apiService';

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
        if (response.data && response.data.data) {
            const {secureToken} = response.data.data;

            // 確保有加密令牌
            if (secureToken) {
                console.log('使用加密安全令牌');
                // 儲存加密令牌 - 不加Bearer前綴，讓apiService統一處理
                setAuthToken(secureToken);

                // 儲存使用者資訊
                if (response.data.data.user) {
                    localStorage.setItem('user', JSON.stringify(response.data.data.user));
                }
            } else {
                // 找不到加密令牌，視為登入失敗
                console.error('登入失敗: 系統沒有返回加密令牌');
                throw new Error('系統錯誤: 無法取得安全令牌');
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

            // 清除本機儲存的認證訊息
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
