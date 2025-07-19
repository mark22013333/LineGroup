package com.cheng.linegroup.dto;

import lombok.Data;

/**
 * 角色查詢參數
 *
 * @author cheng
 * @since 2025/5/15 01:00
 */
@Data
public class RoleQueryParams {

    /**
     * 關鍵字（用於搜索名稱或代碼）
     */
    private String keyword;
    
    /**
     * 狀態(0:停用, 1:啟用)
     */
    private Integer status;
    
    /**
     * 是否包含已刪除的角色
     */
    private Boolean includeDeleted = false;
    
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
    private String sortField = "sort";
    
    /**
     * 排序方向
     */
    private String sortDirection = "asc";
}
