package com.cheng.linegroup.dto.common;

import com.cheng.linegroup.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用查詢參數基礎類別
 * 
 * @author cheng
 * @since 2025/5/3 11:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通用查詢參數")
public class BaseQueryParams extends BaseDto {

    @Schema(description = "頁碼 (從0開始)", example = "0")
    private Integer page = 0;

    @Schema(description = "每頁筆數", example = "10")
    private Integer size = 10;

    @Schema(description = "排序欄位", example = "id")
    private String sortField;

    @Schema(description = "排序方向 (asc 或 desc)", example = "desc")
    private String sortDirection = "desc";

    @Schema(description = "關鍵字搜尋", example = "test")
    private String keyword;
}
