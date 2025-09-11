package com.cheng.linegroup.dto.inventory;

import com.cheng.linegroup.enums.inventory.ReferenceTypeEnum;
import com.cheng.linegroup.enums.inventory.TransactionTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 庫存異動記錄資料傳輸物件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "庫存異動記錄資料傳輸物件")
public class InventoryTransactionDTO {

    @Schema(description = "異動記錄ID", example = "1")
    private Long id;

    @Schema(description = "異動單號", example = "IT202401010001")
    private String transactionNumber;

    @Schema(description = "物品ID", example = "1")
    private Long itemId;

    @Schema(description = "物品代碼", example = "LAPTOP001")
    private String itemCode;

    @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
    private String itemName;

    @Schema(description = "分類名稱", example = "電腦設備")
    private String categoryName;

    @Schema(description = "異動類型", example = "IN")
    private TransactionTypeEnum transactionType;

    @Schema(description = "異動數量", example = "5")
    private Integer quantity;

    @Schema(description = "異動前數量", example = "10")
    private Integer beforeQuantity;

    @Schema(description = "異動後數量", example = "15")
    private Integer afterQuantity;

    @Schema(description = "參考類型", example = "PURCHASE")
    private ReferenceTypeEnum referenceType;

    @Schema(description = "參考ID", example = "123")
    private Long referenceId;

    @Schema(description = "參考單號", example = "PO202401010001")
    private String referenceNumber;

    @Schema(description = "異動原因", example = "採購入庫")
    private String reason;

    @Schema(description = "備註", example = "新採購的筆記型電腦")
    private String notes;

    @Schema(description = "處理人ID", example = "1")
    private Long processedById;

    @Schema(description = "處理人姓名", example = "張三")
    private String processedByName;

    @Schema(description = "審核人ID", example = "2")
    private Long approvedById;

    @Schema(description = "審核人姓名", example = "李四")
    private String approvedByName;

    @Schema(description = "異動時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate;

    @Schema(description = "審核時間", example = "2024-01-01 11:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime approvedDate;

    @Schema(description = "建立時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 建立異動請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "建立庫存異動請求")
    public static class CreateRequest {

        @NotNull(message = "物品ID不能為空")
        @Schema(description = "物品ID", example = "1", required = true)
        private Long itemId;

        @NotNull(message = "異動類型不能為空")
        @Schema(description = "異動類型", example = "IN", required = true)
        private TransactionTypeEnum transactionType;

        @NotNull(message = "異動數量不能為空")
        @Schema(description = "異動數量", example = "5", required = true)
        private Integer quantity;

        @Schema(description = "參考類型", example = "PURCHASE")
        private ReferenceTypeEnum referenceType;

        @Schema(description = "參考ID", example = "123")
        private Long referenceId;

        @Size(max = 100, message = "參考單號長度不能超過100字元")
        @Schema(description = "參考單號", example = "PO202401010001")
        private String referenceNumber;

        @Size(max = 500, message = "異動原因長度不能超過500字元")
        @Schema(description = "異動原因", example = "採購入庫")
        private String reason;

        @Size(max = 1000, message = "備註長度不能超過1000字元")
        @Schema(description = "備註", example = "新採購的筆記型電腦")
        private String notes;
    }

    /**
     * 查詢請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "查詢庫存異動請求")
    public static class QueryRequest {

        @Schema(description = "異動單號", example = "IT202401010001")
        private String transactionNumber;

        @Schema(description = "物品ID", example = "1")
        private Long itemId;

        @Schema(description = "物品代碼關鍵字", example = "LAPTOP")
        private String itemCode;

        @Schema(description = "物品名稱關鍵字", example = "Dell")
        private String itemName;

        @Schema(description = "分類ID", example = "1")
        private Long categoryId;

        @Schema(description = "異動類型", example = "IN")
        private TransactionTypeEnum transactionType;

        @Schema(description = "參考類型", example = "PURCHASE")
        private ReferenceTypeEnum referenceType;

        @Schema(description = "處理人ID", example = "1")
        private Long processedById;

        @Schema(description = "審核人ID", example = "2")
        private Long approvedById;

        @Schema(description = "異動開始時間", example = "2024-01-01 00:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime transactionStartDate;

        @Schema(description = "異動結束時間", example = "2024-01-31 23:59:59")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime transactionEndDate;

        @Schema(description = "是否已審核", example = "true")
        private Boolean approved;

        @Schema(description = "關鍵字搜尋", example = "採購")
        private String keyword;

        @Schema(description = "頁碼", example = "1")
        private Integer page = 1;

        @Schema(description = "每頁數量", example = "10")
        private Integer size = 10;

        @Schema(description = "排序欄位", example = "transactionDate")
        private String sortBy = "transactionDate";

        @Schema(description = "排序方向", example = "DESC")
        private String sortDir = "DESC";
    }

    /**
     * 異動統計 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存異動統計")
    public static class Statistics {

        @Schema(description = "總異動次數", example = "1000")
        private Long totalTransactionCount;

        @Schema(description = "入庫次數", example = "400")
        private Long inboundCount;

        @Schema(description = "出庫次數", example = "350")
        private Long outboundCount;

        @Schema(description = "調整次數", example = "200")
        private Long adjustmentCount;

        @Schema(description = "損壞次數", example = "30")
        private Long damagedCount;

        @Schema(description = "遺失次數", example = "20")
        private Long lostCount;

        @Schema(description = "總入庫數量", example = "5000")
        private Long totalInboundQuantity;

        @Schema(description = "總出庫數量", example = "4500")
        private Long totalOutboundQuantity;

        @Schema(description = "淨異動數量", example = "500")
        private Long netQuantity;

        @Schema(description = "統計時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }

    /**
     * 異動趨勢 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存異動趨勢")
    public static class Trend {

        @Schema(description = "日期", example = "2024-01-01")
        private String date;

        @Schema(description = "異動類型", example = "IN")
        private TransactionTypeEnum transactionType;

        @Schema(description = "異動次數", example = "10")
        private Long transactionCount;

        @Schema(description = "異動數量", example = "50")
        private Long totalQuantity;
    }

    /**
     * 審核請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "審核異動請求")
    public static class ApprovalRequest {

        @Schema(description = "是否通過審核", example = "true", required = true)
        private Boolean approved;

        @Size(max = 1000, message = "審核備註長度不能超過1000字元")
        @Schema(description = "審核備註", example = "審核通過")
        private String approvalNotes;
    }

    /**
     * 批次異動請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "批次庫存異動請求")
    public static class BatchRequest {

        @Schema(description = "異動項目列表", required = true)
        private java.util.List<CreateRequest> transactions;

        @Size(max = 500, message = "批次原因長度不能超過500字元")
        @Schema(description = "批次原因", example = "年度盤點調整")
        private String batchReason;

        @Size(max = 1000, message = "批次備註長度不能超過1000字元")
        @Schema(description = "批次備註", example = "根據盤點結果進行庫存調整")
        private String batchNotes;
    }

    /**
     * 簡化版異動記錄 DTO（用於列表顯示）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "簡化版庫存異動記錄")
    public static class SimpleTransaction {

        @Schema(description = "異動記錄ID", example = "1")
        private Long id;

        @Schema(description = "異動單號", example = "IT202401010001")
        private String transactionNumber;

        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
        private String itemName;

        @Schema(description = "異動類型", example = "IN")
        private TransactionTypeEnum transactionType;

        @Schema(description = "異動數量", example = "5")
        private Integer quantity;

        @Schema(description = "處理人姓名", example = "張三")
        private String processedByName;

        @Schema(description = "異動時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime transactionDate;

        @Schema(description = "是否已審核", example = "true")
        private Boolean approved;
    }
}
