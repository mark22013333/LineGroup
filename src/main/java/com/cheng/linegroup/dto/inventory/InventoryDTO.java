package com.cheng.linegroup.dto.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 庫存資料傳輸物件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "庫存資料傳輸物件")
public class InventoryDTO {

    @Schema(description = "庫存ID", example = "1")
    private Long id;

    @Schema(description = "物品ID", example = "1")
    private Long itemId;

    @Schema(description = "物品代碼", example = "LAPTOP001")
    private String itemCode;

    @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
    private String itemName;

    @Schema(description = "分類名稱", example = "電腦設備")
    private String categoryName;

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

    @Schema(description = "最低庫存警告", example = "5")
    private Integer minStockLevel;

    @Schema(description = "最高庫存警告", example = "50")
    private Integer maxStockLevel;

    @Schema(description = "是否有庫存", example = "true")
    private Boolean inStock;

    @Schema(description = "是否低庫存", example = "false")
    private Boolean lowStock;

    @Schema(description = "是否缺貨", example = "false")
    private Boolean outOfStock;

    @Schema(description = "庫存狀態", example = "正常")
    private String stockStatus;

    @Schema(description = "最後異動時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTransactionDate;

    @Schema(description = "建立時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 庫存調整請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存調整請求")
    public static class AdjustRequest {

        @NotNull(message = "物品ID不能為空")
        @Schema(description = "物品ID", example = "1", required = true)
        private Long itemId;

        @NotNull(message = "調整數量不能為空")
        @Schema(description = "調整數量（正數為增加，負數為減少）", example = "5", required = true)
        private Integer quantity;

        @Schema(description = "調整原因", example = "盤點調整")
        private String reason;

        @Schema(description = "備註", example = "年度盤點發現差異")
        private String notes;
    }

    /**
     * 庫存設定請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存設定請求")
    public static class SettingRequest {

        @NotNull(message = "物品ID不能為空")
        @Schema(description = "物品ID", example = "1", required = true)
        private Long itemId;

        @Min(value = 0, message = "最低庫存警告不能為負數")
        @Schema(description = "最低庫存警告", example = "5")
        private Integer minStockLevel;

        @Min(value = 0, message = "最高庫存警告不能為負數")
        @Schema(description = "最高庫存警告", example = "50")
        private Integer maxStockLevel;
    }

    /**
     * 庫存查詢請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存查詢請求")
    public static class QueryRequest {

        @Schema(description = "物品代碼關鍵字", example = "LAPTOP")
        private String itemCode;

        @Schema(description = "物品名稱關鍵字", example = "Dell")
        private String itemName;

        @Schema(description = "分類ID", example = "1")
        private Long categoryId;

        @Schema(description = "是否有庫存", example = "true")
        private Boolean inStock;

        @Schema(description = "是否低庫存", example = "false")
        private Boolean lowStock;

        @Schema(description = "是否缺貨", example = "false")
        private Boolean outOfStock;

        @Schema(description = "最小總數量", example = "1")
        private Integer minTotalQuantity;

        @Schema(description = "最大總數量", example = "100")
        private Integer maxTotalQuantity;

        @Schema(description = "關鍵字搜尋", example = "筆記型電腦")
        private String keyword;

        @Schema(description = "頁碼", example = "1")
        private Integer page = 1;

        @Schema(description = "每頁數量", example = "10")
        private Integer size = 10;

        @Schema(description = "排序欄位", example = "totalQuantity")
        private String sortBy = "totalQuantity";

        @Schema(description = "排序方向", example = "DESC")
        private String sortDir = "DESC";
    }

    /**
     * 庫存統計 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存統計")
    public static class Statistics {

        @Schema(description = "總物品種類數", example = "150")
        private Long totalItemTypes;

        @Schema(description = "總庫存數量", example = "1500")
        private Long totalQuantity;

        @Schema(description = "總可用數量", example = "1200")
        private Long totalAvailableQuantity;

        @Schema(description = "總借出數量", example = "250")
        private Long totalBorrowedQuantity;

        @Schema(description = "總損壞數量", example = "50")
        private Long totalDamagedQuantity;

        @Schema(description = "低庫存物品數", example = "10")
        private Long lowStockItemCount;

        @Schema(description = "缺貨物品數", example = "3")
        private Long outOfStockItemCount;

        @Schema(description = "庫存週轉率", example = "2.5")
        private Double turnoverRate;

        @Schema(description = "統計時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }

    /**
     * 庫存警告 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存警告")
    public static class Alert {

        @Schema(description = "物品ID", example = "1")
        private Long itemId;

        @Schema(description = "物品代碼", example = "LAPTOP001")
        private String itemCode;

        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
        private String itemName;

        @Schema(description = "分類名稱", example = "電腦設備")
        private String categoryName;

        @Schema(description = "當前數量", example = "3")
        private Integer currentQuantity;

        @Schema(description = "最低庫存警告", example = "5")
        private Integer minStockLevel;

        @Schema(description = "警告類型", example = "LOW_STOCK")
        private String alertType;

        @Schema(description = "警告訊息", example = "庫存不足，請及時補貨")
        private String alertMessage;

        @Schema(description = "警告時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime alertTime;
    }
}
