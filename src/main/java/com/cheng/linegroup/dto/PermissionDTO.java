package com.cheng.linegroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 權限資料傳輸物件
 *
 * @author cheng
 * @since 2025/5/15 00:59
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    /**
     * 主鍵ID
     */
    private Long id;
    
    /**
     * 模組名稱
     */
    private String module;
    
    /**
     * 功能名稱
     */
    private String feature;
    
    /**
     * 權限鍵值，對應按鈕的唯一代碼
     */
    private String actionKey;
    
    /**
     * 描述說明
     */
    private String description;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 創建時間
     */
    private LocalDateTime createTime;
    
    /**
     * 更新時間
     */
    private LocalDateTime updateTime;
}
