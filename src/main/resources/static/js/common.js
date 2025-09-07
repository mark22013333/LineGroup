/**
 * LineGroup 應用程式通用 JavaScript 工具函數
 */

// 應用程式的固定 context path
const APP_CONTEXT_PATH = '/apps';

/**
 * 取得應用程式 context path
 * @returns {string} 應用的 context-path，始終為 "/apps"
 */
function getContextPath() {
    return APP_CONTEXT_PATH;
}

/**
 * 構建完整的 API URL，自動新增 context path
 * @param {string} endpoint - API 端點路徑，例如 "/api/maps/key"
 * @returns {string} 完整的 URL，例如 "/apps/api/maps/key"
 */
function buildApiUrl(endpoint) {
    // 處理 endpoint 開頭的斜線，確保路徑正確
    if (endpoint.startsWith('/')) {
        return APP_CONTEXT_PATH + endpoint;
    } else {
        return APP_CONTEXT_PATH + '/' + endpoint;
    }
}
