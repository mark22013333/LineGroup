package com.cheng.linegroup.dto.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分類資料傳輸物件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分類資料傳輸物件")
public class CategoryDTO {

    @Schema(description = "分類ID", example = "1")
    private Long id;

    @NotBlank(message = "分類名稱不能為空")
    @Size(max = 100, message = "分類名稱長度不能超過100字元")
    @Schema(description = "分類名稱", example = "電子設備", required = true)
    private String name;

    @Size(max = 20, message = "分類代碼長度不能超過20字元")
    @Schema(description = "分類代碼", example = "ELEC")
    private String code;

    @Size(max = 500, message = "分類描述長度不能超過500字元")
    @Schema(description = "分類描述", example = "各種電子設備和器材")
    private String description;

    @Schema(description = "父分類ID", example = "0")
    private Long parentId;

    @Schema(description = "父分類名稱", example = "設備類")
    private String parentName;

    @Schema(description = "分類層級", example = "1")
    private Integer level;

    @Schema(description = "排序順序", example = "1")
    private Integer sortOrder;

    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled;

    @Schema(description = "子分類數量", example = "5")
    private Long childrenCount;

    @Schema(description = "物品數量", example = "25")
    private Long itemCount;

    @Schema(description = "子分類列表")
    private List<CategoryDTO> children;

    @Schema(description = "建立時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Schema(description = "建立者", example = "admin")
    private String createdBy;

    @Schema(description = "更新者", example = "admin")
    private String updatedBy;

    /**
     * 建立請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "建立分類請求")
    public static class CreateRequest {

        @NotBlank(message = "分類名稱不能為空")
        @Size(max = 100, message = "分類名稱長度不能超過100字元")
        @Schema(description = "分類名稱", example = "電子設備", required = true)
        private String name;

        @Size(max = 20, message = "分類代碼長度不能超過20字元")
        @Schema(description = "分類代碼", example = "ELEC")
        private String code;

        @Size(max = 500, message = "分類描述長度不能超過500字元")
        @Schema(description = "分類描述", example = "各種電子設備和器材")
        private String description;

        @Schema(description = "父分類ID", example = "0")
        private Long parentId;

        @Schema(description = "排序順序", example = "1")
        private Integer sortOrder;

        @Schema(description = "是否啟用", example = "true")
        private Boolean enabled = true;
    }

    /**
     * 更新請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "更新分類請求")
    public static class UpdateRequest {

        @NotBlank(message = "分類名稱不能為空")
        @Size(max = 100, message = "分類名稱長度不能超過100字元")
        @Schema(description = "分類名稱", example = "電子設備", required = true)
        private String name;

        @Size(max = 20, message = "分類代碼長度不能超過20字元")
        @Schema(description = "分類代碼", example = "ELEC")
        private String code;

        @Size(max = 500, message = "分類描述長度不能超過500字元")
        @Schema(description = "分類描述", example = "各種電子設備和器材")
        private String description;

        @Schema(description = "父分類ID", example = "0")
        private Long parentId;

        @Schema(description = "排序順序", example = "1")
        private Integer sortOrder;

        @Schema(description = "是否啟用", example = "true")
        private Boolean enabled;
    }

    /**
     * 查詢請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "查詢分類請求")
    public static class QueryRequest {

        @Schema(description = "分類名稱關鍵字", example = "電子")
        private String name;

        @Schema(description = "分類代碼", example = "ELEC")
        private String code;

        @Schema(description = "父分類ID", example = "0")
        private Long parentId;

        @Schema(description = "是否啟用", example = "true")
        private Boolean enabled;

        @Schema(description = "是否包含子分類", example = "true")
        private Boolean includeChildren = false;

        @Schema(description = "頁碼", example = "1")
        private Integer page = 1;

        @Schema(description = "每頁數量", example = "10")
        private Integer size = 10;

        @Schema(description = "排序欄位", example = "sortOrder")
        private String sortBy = "sortOrder";

        @Schema(description = "排序方向", example = "ASC")
        private String sortDir = "ASC";
    }

    /**
     * 樹狀結構 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分類樹狀結構")
    public static class TreeNode {

        @Schema(description = "分類ID", example = "1")
        private Long id;

        @Schema(description = "分類名稱", example = "電子設備")
        private String name;

        @Schema(description = "分類代碼", example = "ELEC")
        private String code;

        @Schema(description = "父分類ID", example = "0")
        private Long parentId;

        @Schema(description = "分類層級", example = "1")
        private Integer level;

        @Schema(description = "排序順序", example = "1")
        private Integer sortOrder;

        @Schema(description = "是否啟用", example = "true")
        private Boolean enabled;

        @Schema(description = "物品數量", example = "25")
        private Long itemCount;

        @Schema(description = "子分類列表")
        private List<TreeNode> children;
    }

    /**
     * 分類統計 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分類統計")
    public static class Statistics {

        @Schema(description = "總分類數", example = "50")
        private Long totalCategories;

        @Schema(description = "啟用分類數", example = "45")
        private Long enabledCategories;

        @Schema(description = "根分類數", example = "10")
        private Long rootCategories;
    }
}
