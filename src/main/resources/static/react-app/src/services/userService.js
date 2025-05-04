import apiService from './apiService';

/**
 * 使用者管理相關 API 服務
 */
const userService = {
    /**
     * 分頁查詢使用者列表
     * @param {Object} queryParams - 查詢參數
     * @returns {Promise<Object>} 分頁結果
     */
    getUsers: async (queryParams) => {
        console.log('發送使用者查詢參數:', queryParams);
        // axios 要求 GET 請求的參數應該在 config 對象的 params 屬性中
        const response = await apiService.get('/users', {params: queryParams});
        console.log('使用者查詢回應:', response.data);
        return response.data.data;
    },

    /**
     * 根據 ID 取得單一使用者
     * @param {number} id - 使用者 ID
     * @returns {Promise<Object>} 使用者資訊
     */
    getUserById: async (id) => {
        const response = await apiService.get(`/users/${id}`);
        return response.data.data;
    },

    /**
     * 取得使用者角色列表 (用於篩選)
     * @returns {Promise<Array>} 角色列表
     */
    getRoles: async () => {
        const response = await apiService.get('/roles');
        return response.data.data;
    },

    /**
     * 更新使用者資訊
     * @param {number} id - 使用者 ID
     * @param {Object} userData - 更新的使用者資訊
     * @returns {Promise<Object>} 更新後的使用者資訊
     */
    updateUser: async (id, userData) => {
        const response = await apiService.put(`/users/${id}`, userData);
        return response.data.data;
    },

    /**
     * 刪除使用者
     * @param {number} id - 使用者 ID
     * @returns {Promise<boolean>} 是否刪除成功
     */
    deleteUser: async (id) => {
        const response = await apiService.delete(`/users/${id}`);
        return response.data.success;
    }
};

export default userService;
