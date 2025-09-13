import axios from 'axios';

// 設定 API 基礎 URL
const API_BASE_URL = '/api/inventory';

// 建立 axios 實例
const apiClient = axios.create({
    baseURL: API_BASE_URL,
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 請求攔截器 - 加入認證 token
apiClient.interceptors.request.use(
    (config) => {
        console.log('[InventoryAPI] 發送請求:', {
            url: config.url,
            method: config.method,
            baseURL: config.baseURL,
            fullURL: `${config.baseURL}${config.url}`
        });
        
        const token = localStorage.getItem('token');
        if (token) {
            // 確保不重複新增Bearer前綴
            config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
            console.log('[InventoryAPI] 使用 token:', token.substring(0, 20) + '...');
        } else {
            console.warn('[InventoryAPI] 警告: 沒有找到 token');
        }
        return config;
    },
    (error) => {
        console.error('[InventoryAPI] 請求攔截器錯誤:', error);
        return Promise.reject(error);
    }
);

// 回應攔截器 - 統一處理錯誤
apiClient.interceptors.response.use(
    (response) => {
        console.log('[InventoryAPI] 請求成功:', {
            status: response.status,
            url: response.config.url,
            data: response.data
        });
        return response.data;
    },
    (error) => {
        console.error('[InventoryAPI] 請求失敗:', {
            status: error.response?.status,
            statusText: error.response?.statusText,
            url: error.config?.url,
            fullURL: `${error.config?.baseURL}${error.config?.url}`,
            message: error.message,
            responseData: error.response?.data
        });
        
        if (error.response?.status === 401) {
            console.warn('[InventoryAPI] 401 未授權，清除 token 並跳轉到登入頁');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '/login';
        } else if (error.response?.status === 404) {
            console.error('[InventoryAPI] 404 找不到資源，可能是路徑配置問題');
        } else if (!error.response) {
            console.error('[InventoryAPI] 網路錯誤或伺服器無回應');
        }
        
        return Promise.reject(error);
    }
);

// 庫存管理 API
export const inventoryAPI = {
    // ==================== 儀表板 ====================
    getDashboardStatistics: () => apiClient.get('/reports/dashboard'),

    // ==================== 分類管理 ====================
    getCategories: (params) => apiClient.get('/categories', { params }),
    getCategoryById: (id) => apiClient.get(`/categories/${id}`),
    createCategory: (data) => apiClient.post('/categories', data),
    updateCategory: (id, data) => apiClient.put(`/categories/${id}`, data),
    deleteCategory: (id) => apiClient.delete(`/categories/${id}`),
    getCategoryTree: () => apiClient.get('/categories/tree'),
    getCategoryStatistics: () => apiClient.get('/categories/statistics'),

    // ==================== 物品管理 ====================
    getItems: (params) => apiClient.get('/items', { params }),
    getItemById: (id) => apiClient.get(`/items/${id}`),
    createItem: (data) => apiClient.post('/items', data),
    updateItem: (id, data) => apiClient.put(`/items/${id}`, data),
    deleteItem: (id) => apiClient.delete(`/items/${id}`),
    searchItemsByBarcode: (barcode) => apiClient.get(`/items/search/barcode/${barcode}`),
    searchItemsByCode: (code) => apiClient.get(`/items/search/code/${code}`),
    getItemsByCategory: (categoryId, params) => apiClient.get(`/items/category/${categoryId}`, { params }),
    getLowStockItems: () => apiClient.get('/items/low-stock'),
    getOutOfStockItems: () => apiClient.get('/items/out-of-stock'),
    getItemStatistics: () => apiClient.get('/items/statistics'),

    // ==================== 庫存管理 ====================
    getInventories: (params) => apiClient.get('/inventories', { params }),
    getInventoryById: (id) => apiClient.get(`/inventories/${id}`),
    adjustInventory: (data) => apiClient.post('/inventories/adjust', data),
    batchAdjustInventory: (data) => apiClient.post('/inventories/batch-adjust', data),
    setInventoryAlert: (data) => apiClient.post('/inventories/alert', data),
    stockTake: (data) => apiClient.post('/inventories/stock-take', data),
    getInventoryAlerts: () => apiClient.get('/inventories/alerts'),
    getInventoryStatistics: () => apiClient.get('/inventories/statistics'),

    // ==================== 借還記錄管理 ====================
    getBorrowRecords: (params) => apiClient.get('/borrow-records', { params }),
    getBorrowRecordById: (id) => apiClient.get(`/borrow-records/${id}`),
    getBorrowRecordByNumber: (recordNumber) => apiClient.get(`/borrow-records/number/${recordNumber}`),
    createBorrowRecord: (data) => apiClient.post('/borrow-records', data),
    returnItem: (id, data) => apiClient.put(`/borrow-records/${id}/return`, data),
    barcodeBorrow: (data) => apiClient.post('/borrow-records/barcode/borrow', data),
    barcodeReturn: (data) => apiClient.post('/borrow-records/barcode/return', data),
    getCurrentBorrowsByUser: (userId) => apiClient.get(`/borrow-records/user/${userId}/current`),
    getOverdueRecords: () => apiClient.get('/borrow-records/overdue'),
    getRecordsDueSoon: (days = 3) => apiClient.get('/borrow-records/due-soon', { params: { days } }),
    getBorrowStatistics: (params) => apiClient.get('/borrow-records/statistics', { params }),

    // ==================== 庫存異動記錄 ====================
    getTransactions: (params) => apiClient.get('/transactions', { params }),
    getTransactionById: (id) => apiClient.get(`/transactions/${id}`),
    createTransaction: (data) => apiClient.post('/transactions', data),
    createBatchTransactions: (data) => apiClient.post('/transactions/batch', data),
    approveTransaction: (id, data) => apiClient.put(`/transactions/${id}/approve`, data),
    getItemTransactionHistory: (itemId) => apiClient.get(`/transactions/item/${itemId}/history`),
    getPendingApprovalTransactions: () => apiClient.get('/transactions/pending-approval'),
    getTransactionStatistics: (params) => apiClient.get('/transactions/statistics', { params }),
    getTransactionTrends: (params) => apiClient.get('/transactions/trends', { params }),

    // ==================== 條碼管理 ====================
    generateQRCode: (data) => apiClient.post('/barcode/qrcode/generate', data, { responseType: 'blob' }),
    generateBarCode: (data) => apiClient.post('/barcode/barcode/generate', data, { responseType: 'blob' }),
    scanBarcode: (data) => apiClient.post('/barcode/scan', data),
    batchGenerateItemBarcodes: (data) => apiClient.post('/barcode/batch/generate', data),
    validateBarcode: (data) => apiClient.post('/barcode/validate', data),

    // ==================== 報表管理 ====================
    generateInventoryReport: (data) => apiClient.post('/reports/inventory', data),
    generateBorrowReport: (data) => apiClient.post('/reports/borrow', data),
    downloadReport: (fileId) => apiClient.get(`/reports/download/${fileId}`, { responseType: 'blob' }),

    // ==================== 使用者管理 ====================
    getUsers: (params) => apiClient.get('/users', { params }),
    getUserById: (id) => apiClient.get(`/users/${id}`),
    getUserByUsername: (username) => apiClient.get(`/users/username/${username}`),
    getEnabledUsers: () => apiClient.get('/users/enabled'),
    getUsersByDepartment: (deptId) => apiClient.get(`/users/department/${deptId}`),
    searchUsers: (keyword) => apiClient.get('/users/search', { params: { keyword } }),
    createUser: (data) => apiClient.post('/users', data),
    updateUser: (id, data) => apiClient.put(`/users/${id}`, data),
    toggleUserStatus: (id) => apiClient.put(`/users/${id}/toggle-status`),
    deleteUser: (id) => apiClient.delete(`/users/${id}`),
    getUserStatistics: () => apiClient.get('/users/statistics'),
};

export default inventoryAPI;
