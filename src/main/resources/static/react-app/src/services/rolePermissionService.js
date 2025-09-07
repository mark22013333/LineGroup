import http from './httpService';

/**
 * 角色與權限管理相關的API服務
 */
const rolePermissionService = {
    // ===================== 角色管理 =====================
    
    /**
     * 分頁查詢角色列表
     * @param {object} params 查詢參數
     * @returns {Promise} 角色列表分頁數據
     */
    getRoles(params = {}) {
        return http.get('/api/system/roles', { params });
    },
    
    /**
     * 取得所有啟用的角色
     * @returns {Promise} 角色列表
     */
    getAllEnabledRoles() {
        return http.get('/api/system/roles/all');
    },
    
    /**
     * 根據ID取得角色詳情
     * @param {number} id 角色ID
     * @returns {Promise} 角色詳情
     */
    getRoleById(id) {
        return http.get(`/api/system/roles/${id}`);
    },
    
    /**
     * 新增角色
     * @param {object} role 角色資料
     * @returns {Promise} 新增後的角色
     */
    createRole(role) {
        return http.post('/api/system/roles', role);
    },
    
    /**
     * 更新角色
     * @param {number} id 角色ID
     * @param {object} role 角色資料
     * @returns {Promise} 更新後的角色
     */
    updateRole(id, role) {
        return http.put(`/api/system/roles/${id}`, role);
    },
    
    /**
     * 更新角色狀態
     * @param {number} id 角色ID
     * @param {number} status 狀態值
     * @returns {Promise} 更新後的角色
     */
    updateRoleStatus(id, status) {
        return http.patch(`/api/system/roles/${id}/status`, { status });
    },
    
    /**
     * 更新角色排序
     * @param {number} id 角色ID
     * @param {number} sort 排序值
     * @returns {Promise} 更新後的角色
     */
    updateRoleSort(id, sort) {
        return http.patch(`/api/system/roles/${id}/sort`, { sort });
    },
    
    /**
     * 刪除角色
     * @param {number} id 角色ID
     * @returns {Promise} 刪除結果
     */
    deleteRole(id) {
        return http.delete(`/api/system/roles/${id}`);
    },
    
    /**
     * 為角色分配權限
     * @param {number} roleId 角色ID
     * @param {array} permissionIds 權限ID列表
     * @returns {Promise} 操作結果
     */
    assignPermissions(roleId, permissionIds) {
        return http.post(`/api/system/roles/${roleId}/permissions`, permissionIds);
    },
    
    /**
     * 取得角色的權限ID列表
     * @param {number} roleId 角色ID
     * @returns {Promise} 權限ID列表
     */
    getRolePermissions(roleId) {
        return http.get(`/api/system/roles/${roleId}/permissions`);
    },
    
    // ===================== 權限管理 =====================
    
    /**
     * 分頁查詢權限列表
     * @param {object} params 查詢參數
     * @returns {Promise} 權限列表分頁數據
     */
    getPermissions(params = {}) {
        return http.get('/api/system/permissions', { params });
    },
    
    /**
     * 根據ID取得權限詳情
     * @param {number} id 權限ID
     * @returns {Promise} 權限詳情
     */
    getPermissionById(id) {
        return http.get(`/api/system/permissions/${id}`);
    },
    
    /**
     * 新增權限
     * @param {object} permission 權限資料
     * @returns {Promise} 新增後的權限
     */
    createPermission(permission) {
        return http.post('/api/system/permissions', permission);
    },
    
    /**
     * 更新權限
     * @param {number} id 權限ID
     * @param {object} permission 權限資料
     * @returns {Promise} 更新後的權限
     */
    updatePermission(id, permission) {
        return http.put(`/api/system/permissions/${id}`, permission);
    },
    
    /**
     * 刪除權限
     * @param {number} id 權限ID
     * @returns {Promise} 刪除結果
     */
    deletePermission(id) {
        return http.delete(`/api/system/permissions/${id}`);
    },
    
    /**
     * 取得所有模組名稱
     * @returns {Promise} 模組名稱列表
     */
    getAllModules() {
        return http.get('/api/system/permissions/modules');
    },
    
    /**
     * 取得權限樹
     * @returns {Promise} 權限樹數據
     */
    getPermissionTree() {
        return http.get('/api/system/permissions/tree');
    }
};

export default rolePermissionService;
