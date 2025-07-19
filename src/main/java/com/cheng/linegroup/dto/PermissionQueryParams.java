package com.cheng.linegroup.dto;

import lombok.Data;

/**
 * 權限查詢參數
 *
 * @author cheng
 * @since 2025/5/15 01:02
 */
@Data
public class PermissionQueryParams {

    /**
     * 模組名稱
     */
    private String module;
    
    /**
     * 功能名稱
     */
    private String feature;
    
    /**
     * 關鍵字（用於搜索權限代碼或描述）
     */
    private String keyword;
    
    /**
     * 頁碼（從0開始）
     */
    private Integer page = 0;
    
    /**
     * 每頁記錄數
     */
    private Integer size = 10;
    
    /**
     * 排序欄位
     */
    private String sortField = "module";
    
    /**
     * 排序方向
     */
    private String sortDirection = "asc";
}
