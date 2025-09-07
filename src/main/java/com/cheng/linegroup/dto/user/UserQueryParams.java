package com.cheng.linegroup.dto.user;

import com.cheng.linegroup.dto.common.BaseQueryParams;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 使用者查詢參數
 * 
 * @author cheng
 * @since 2025/5/3 11:45
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "使用者查詢參數")
public class UserQueryParams extends BaseQueryParams {
    
    @Schema(description = "角色ID", example = "1")
    private Long roleId;
    
    @Schema(description = "狀態 (0:停用, 1:啟用)", example = "1")
    private Integer status;
    
    @Schema(description = "開始日期 (格式: yyyy-MM-dd)", example = "2025-01-01")
    private String startDate;
    
    @Schema(description = "結束日期 (格式: yyyy-MM-dd)", example = "2025-12-31")
    private String endDate;
}
