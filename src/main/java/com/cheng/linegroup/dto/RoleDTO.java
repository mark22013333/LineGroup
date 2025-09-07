package com.cheng.linegroup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色資料傳輸物件
 *
 * @author cheng
 * @since 2025/5/15 00:53
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;
    
    /**
     * 角色名稱
     */
    private String name;
    
    /**
     * 角色代碼
     */
    private String code;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 狀態(0:停用, 1:啟用)
     */
    private Integer status;
    
    /**
     * 資料權限(0:所有資料, 1:部門及子部門資料, 2:本部門資料, 3:本人資料)
     */
    private Integer dataScope;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 權限ID列表
     */
    private List<Long> permissionIds;
    
    /**
     * 創建時間
     */
    private LocalDateTime createTime;
    
    /**
     * 更新時間
     */
    private LocalDateTime updateTime;
}
