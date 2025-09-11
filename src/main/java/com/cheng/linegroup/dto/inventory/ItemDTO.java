package com.cheng.linegroup.dto.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物品資料傳輸物件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "物品資料傳輸物件")
public class ItemDTO {

    @Schema(description = "物品ID", example = "1")
    private Long id;

    @NotBlank(message = "物品代碼不能為空")
    @Size(max = 50, message = "物品代碼長度不能超過50字元")
    @Schema(description = "物品代碼", example = "LAPTOP001", required = true)
    private String itemCode;

    @NotBlank(message = "物品名稱不能為空")
    @Size(max = 200, message = "物品名稱長度不能超過200字元")
    @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦", required = true)
    private String name;

    @Size(max = 1000, message = "物品描述長度不能超過1000字元")
    @Schema(description = "物品描述", example = "15.6吋商用筆記型電腦，Intel i7處理器")
    private String description;

    @Size(max = 100, message = "條碼長度不能超過100字元")
    @Schema(description = "條碼", example = "1234567890123")
    private String barcode;

    @NotNull(message = "分類不能為空")
    @Schema(description = "分類ID", example = "1", required = true)
    private Long categoryId;

    @Schema(description = "分類名稱", example = "電腦設備")
    private String categoryName;

    @Size(max = 100, message = "品牌長度不能超過100字元")
    @Schema(description = "品牌", example = "Dell")
    private String brand;

    @Size(max = 100, message = "型號長度不能超過100字元")
    @Schema(description = "型號", example = "Latitude 5520")
    private String model;

    @Size(max = 2000, message = "規格長度不能超過2000字元")
    @Schema(description = "規格", example = "Intel i7-1165G7, 16GB RAM, 512GB SSD")
    private String specifications;

    @Size(max = 50, message = "單位長度不能超過50字元")
    @Schema(description = "單位", example = "台")
    private String unit;

    @DecimalMin(value = "0.00", message = "單價不能為負數")
    @Digits(integer = 10, fraction = 2, message = "單價格式不正確")
    @Schema(description = "單價", example = "35000.00")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.00", message = "總價不能為負數")
    @Digits(integer = 12, fraction = 2, message = "總價格式不正確")
    @Schema(description = "總價", example = "35000.00")
    private BigDecimal totalPrice;

    @Size(max = 100, message = "供應商長度不能超過100字元")
    @Schema(description = "供應商", example = "某某科技公司")
    private String supplier;

    @Schema(description = "購買日期", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime purchaseDate;

    @Schema(description = "保固到期日", example = "2027-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime warrantyExpiry;

    @Size(max = 200, message = "存放位置長度不能超過200字元")
    @Schema(description = "存放位置", example = "A區-1樓-IT室")
    private String location;

    @Schema(description = "是否啟用", example = "true")
    private Boolean enabled;

    @Schema(description = "是否可借用", example = "true")
    private Boolean borrowable;

    @Schema(description = "最大借用天數", example = "30")
    private Integer maxBorrowDays;

    @Size(max = 1000, message = "備註長度不能超過1000字元")
    @Schema(description = "備註", example = "IT部門專用設備")
    private String notes;

    // 庫存相關資訊
    @Schema(description = "總數量", example = "10")
    private Integer totalQuantity;

    @Schema(description = "可用數量", example = "8")
    private Integer availableQuantity;

    @Schema(description = "已借出數量", example = "2")
    private Integer borrowedQuantity;

    @Schema(description = "預約數量", example = "0")
    private Integer reservedQuantity;

    @Schema(description = "損壞數量", example = "0")
    private Integer damagedQuantity;

    @Schema(description = "是否有庫存", example = "true")
    private Boolean inStock;

    @Schema(description = "是否低庫存", example = "false")
    private Boolean lowStock;

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
    @Schema(description = "建立物品請求")
    public static class CreateRequest {

        @NotBlank(message = "物品代碼不能為空")
        @Size(max = 50, message = "物品代碼長度不能超過50字元")
        @Schema(description = "物品代碼", example = "LAPTOP001", required = true)
        private String itemCode;

        @NotBlank(message = "物品名稱不能為空")
        @Size(max = 200, message = "物品名稱長度不能超過200字元")
        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦", required = true)
        private String name;

        @Size(max = 1000, message = "物品描述長度不能超過1000字元")
        @Schema(description = "物品描述", example = "15.6吋商用筆記型電腦，Intel i7處理器")
        private String description;

        @Size(max = 100, message = "條碼長度不能超過100字元")
        @Schema(description = "條碼", example = "1234567890123")
        private String barcode;

        @NotNull(message = "分類不能為空")
        @Schema(description = "分類ID", example = "1", required = true)
        private Long categoryId;

        @Size(max = 100, message = "品牌長度不能超過100字元")
        @Schema(description = "品牌", example = "Dell")
        private String brand;

        @Size(max = 100, message = "型號長度不能超過100字元")
        @Schema(description = "型號", example = "Latitude 5520")
        private String model;

        @Size(max = 2000, message = "規格長度不能超過2000字元")
        @Schema(description = "規格", example = "Intel i7-1165G7, 16GB RAM, 512GB SSD")
        private String specifications;

        @Size(max = 50, message = "單位長度不能超過50字元")
        @Schema(description = "單位", example = "台")
        private String unit;

        @DecimalMin(value = "0.00", message = "單價不能為負數")
        @Digits(integer = 10, fraction = 2, message = "單價格式不正確")
        @Schema(description = "單價", example = "35000.00")
        private BigDecimal unitPrice;

        @Size(max = 100, message = "供應商長度不能超過100字元")
        @Schema(description = "供應商", example = "某某科技公司")
        private String supplier;

        @Schema(description = "購買日期", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime purchaseDate;

        @Schema(description = "保固到期日", example = "2027-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime warrantyExpiry;

        @Size(max = 200, message = "存放位置長度不能超過200字元")
        @Schema(description = "存放位置", example = "A區-1樓-IT室")
        private String location;

        @Schema(description = "是否啟用", example = "true")
        private Boolean enabled = true;

        @Schema(description = "是否可借用", example = "true")
        private Boolean borrowable = true;

        @Schema(description = "最大借用天數", example = "30")
        private Integer maxBorrowDays;

        @Size(max = 1000, message = "備註長度不能超過1000字元")
        @Schema(description = "備註", example = "IT部門專用設備")
        private String notes;

        @NotNull(message = "初始數量不能為空")
        @Min(value = 0, message = "初始數量不能為負數")
        @Schema(description = "初始數量", example = "10", required = true)
        private Integer initialQuantity;
    }

    /**
     * 更新請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "更新物品請求")
    public static class UpdateRequest {

        @NotBlank(message = "物品名稱不能為空")
        @Size(max = 200, message = "物品名稱長度不能超過200字元")
        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦", required = true)
        private String name;

        @Size(max = 1000, message = "物品描述長度不能超過1000字元")
        @Schema(description = "物品描述", example = "15.6吋商用筆記型電腦，Intel i7處理器")
        private String description;

        @Size(max = 100, message = "條碼長度不能超過100字元")
        @Schema(description = "條碼", example = "1234567890123")
        private String barcode;

        @NotNull(message = "分類不能為空")
        @Schema(description = "分類ID", example = "1", required = true)
        private Long categoryId;

        @Size(max = 100, message = "品牌長度不能超過100字元")
        @Schema(description = "品牌", example = "Dell")
        private String brand;

        @Size(max = 100, message = "型號長度不能超過100字元")
        @Schema(description = "型號", example = "Latitude 5520")
        private String model;

        @Size(max = 2000, message = "規格長度不能超過2000字元")
        @Schema(description = "規格", example = "Intel i7-1165G7, 16GB RAM, 512GB SSD")
        private String specifications;

        @Size(max = 50, message = "單位長度不能超過50字元")
        @Schema(description = "單位", example = "台")
        private String unit;

        @DecimalMin(value = "0.00", message = "單價不能為負數")
        @Digits(integer = 10, fraction = 2, message = "單價格式不正確")
        @Schema(description = "單價", example = "35000.00")
        private BigDecimal unitPrice;

        @Size(max = 100, message = "供應商長度不能超過100字元")
        @Schema(description = "供應商", example = "某某科技公司")
        private String supplier;

        @Schema(description = "購買日期", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime purchaseDate;

        @Schema(description = "保固到期日", example = "2027-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime warrantyExpiry;

        @Size(max = 200, message = "存放位置長度不能超過200字元")
        @Schema(description = "存放位置", example = "A區-1樓-IT室")
        private String location;

        @Schema(description = "是否啟用", example = "true")
        private Boolean enabled;

        @Schema(description = "是否可借用", example = "true")
        private Boolean borrowable;

        @Schema(description = "最大借用天數", example = "30")
        private Integer maxBorrowDays;

        @Size(max = 1000, message = "備註長度不能超過1000字元")
        @Schema(description = "備註", example = "IT部門專用設備")
        private String notes;
    }

    /**
     * 查詢請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "查詢物品請求")
    public static class QueryRequest {

        @Schema(description = "物品代碼關鍵字", example = "LAPTOP")
        private String itemCode;

        @Schema(description = "物品名稱關鍵字", example = "Dell")
        private String name;

        @Schema(description = "條碼", example = "1234567890123")
        private String barcode;

        @Schema(description = "分類ID", example = "1")
        private Long categoryId;

        @Schema(description = "品牌", example = "Dell")
        private String brand;

        @Schema(description = "型號", example = "Latitude")
        private String model;

        @Schema(description = "是否啟用", example = "true")
        private Boolean enabled;

        @Schema(description = "是否可借用", example = "true")
        private Boolean borrowable;

        @Schema(description = "是否有庫存", example = "true")
        private Boolean inStock;

        @Schema(description = "是否低庫存", example = "false")
        private Boolean lowStock;

        @Schema(description = "關鍵字搜尋", example = "筆記型電腦")
        private String keyword;

        @Schema(description = "頁碼", example = "1")
        private Integer page = 1;

        @Schema(description = "每頁數量", example = "10")
        private Integer size = 10;

        @Schema(description = "排序欄位", example = "createdAt")
        private String sortBy = "createdAt";

        @Schema(description = "排序方向", example = "DESC")
        private String sortDir = "DESC";
    }

    /**
     * 簡化版物品 DTO（用於下拉選單等）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "簡化版物品資訊")
    public static class SimpleItem {

        @Schema(description = "物品ID", example = "1")
        private Long id;

        @Schema(description = "物品代碼", example = "LAPTOP001")
        private String itemCode;

        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
        private String name;

        @Schema(description = "條碼", example = "1234567890123")
        private String barcode;

        @Schema(description = "分類名稱", example = "電腦設備")
        private String categoryName;

        @Schema(description = "可用數量", example = "8")
        private Integer availableQuantity;

        @Schema(description = "是否可借用", example = "true")
        private Boolean borrowable;
    }

    /**
     * 物品統計 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "物品統計")
    public static class Statistics {

        @Schema(description = "總物品數", example = "150")
        private Long totalItems;

        @Schema(description = "啟用物品數", example = "140")
        private Long enabledItems;

        @Schema(description = "可借用物品數", example = "120")
        private Long borrowableItems;

        @Schema(description = "低庫存物品數", example = "10")
        private Long lowStockItems;

        @Schema(description = "缺貨物品數", example = "3")
        private Long outOfStockItems;

        @Schema(description = "總庫存價值", example = "5250000.00")
        private BigDecimal totalStockValue;

        @Schema(description = "平均單價", example = "35000.00")
        private BigDecimal averageUnitPrice;

        @Schema(description = "統計時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }
}
