package com.cheng.linegroup.dto.inventory;

import com.cheng.linegroup.enums.inventory.BorrowStatusEnum;
import com.cheng.linegroup.enums.inventory.ReturnConditionEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借還記錄資料傳輸物件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "借還記錄資料傳輸物件")
public class BorrowRecordDTO {

    @Schema(description = "借還記錄ID", example = "1")
    private Long id;

    @Schema(description = "借還單號", example = "BR202401010001")
    private String recordNumber;

    @Schema(description = "物品ID", example = "1")
    private Long itemId;

    @Schema(description = "物品代碼", example = "LAPTOP001")
    private String itemCode;

    @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
    private String itemName;

    @Schema(description = "分類名稱", example = "電腦設備")
    private String categoryName;

    @Schema(description = "借用人ID", example = "1")
    private Long borrowerId;

    @Schema(description = "借用人姓名", example = "張三")
    private String borrowerName;

    @Schema(description = "借用人部門", example = "IT部")
    private String borrowerDepartment;

    @Schema(description = "借用數量", example = "1")
    private Integer quantity;

    @Schema(description = "借用日期", example = "2024-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate borrowDate;

    @Schema(description = "預計歸還日期", example = "2024-01-31")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate expectedReturnDate;

    @Schema(description = "實際歸還日期", example = "2024-01-30")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualReturnDate;

    @Schema(description = "歸還數量", example = "1")
    private Integer returnQuantity;

    @Schema(description = "歸還狀況", example = "GOOD")
    private ReturnConditionEnum returnCondition;

    @Schema(description = "借還狀態", example = "BORROWED")
    private BorrowStatusEnum status;

    @Schema(description = "借用目的", example = "出差使用")
    private String purpose;

    @Schema(description = "借用備註", example = "需要安裝特定軟體")
    private String borrowNotes;

    @Schema(description = "歸還備註", example = "設備狀況良好")
    private String returnNotes;

    @Schema(description = "是否逾期", example = "false")
    private Boolean overdue;

    @Schema(description = "逾期天數", example = "0")
    private Integer overdueDays;

    @Schema(description = "罰款金額", example = "0.00")
    private BigDecimal penaltyAmount;

    @Schema(description = "是否已支付罰款", example = "false")
    private Boolean penaltyPaid;

    @Schema(description = "處理人ID", example = "2")
    private Long processedById;

    @Schema(description = "處理人姓名", example = "李四")
    private String processedByName;

    @Schema(description = "歸還處理人ID", example = "2")
    private Long returnProcessedById;

    @Schema(description = "歸還處理人姓名", example = "李四")
    private String returnProcessedByName;

    @Schema(description = "建立時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "更新時間", example = "2024-01-01 10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 建立借用請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "建立借用請求")
    public static class CreateRequest {

        @NotNull(message = "物品ID不能為空")
        @Schema(description = "物品ID", example = "1", required = true)
        private Long itemId;

        @NotNull(message = "借用人ID不能為空")
        @Schema(description = "借用人ID", example = "1", required = true)
        private Long borrowerId;

        @NotNull(message = "借用數量不能為空")
        @Min(value = 1, message = "借用數量必須大於0")
        @Schema(description = "借用數量", example = "1", required = true)
        private Integer quantity;

        @NotNull(message = "預計歸還日期不能為空")
        @Future(message = "預計歸還日期必須是未來日期")
        @Schema(description = "預計歸還日期", example = "2024-01-31", required = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expectedReturnDate;

        @Size(max = 500, message = "借用目的長度不能超過500字元")
        @Schema(description = "借用目的", example = "出差使用")
        private String purpose;

        @Size(max = 1000, message = "借用備註長度不能超過1000字元")
        @Schema(description = "借用備註", example = "需要安裝特定軟體")
        private String borrowNotes;
    }

    /**
     * 歸還請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "歸還請求")
    public static class ReturnRequest {

        @NotNull(message = "歸還數量不能為空")
        @Min(value = 1, message = "歸還數量必須大於0")
        @Schema(description = "歸還數量", example = "1", required = true)
        private Integer returnQuantity;

        @NotNull(message = "歸還狀況不能為空")
        @Schema(description = "歸還狀況", example = "GOOD", required = true)
        private ReturnConditionEnum returnCondition;

        @Size(max = 1000, message = "歸還備註長度不能超過1000字元")
        @Schema(description = "歸還備註", example = "設備狀況良好")
        private String returnNotes;

        @DecimalMin(value = "0.00", message = "罰款金額不能為負數")
        @Digits(integer = 10, fraction = 2, message = "罰款金額格式不正確")
        @Schema(description = "罰款金額", example = "0.00")
        private BigDecimal penaltyAmount;
    }

    /**
     * 查詢請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "查詢借還記錄請求")
    public static class QueryRequest {

        @Schema(description = "借還單號", example = "BR202401010001")
        private String recordNumber;

        @Schema(description = "物品ID", example = "1")
        private Long itemId;

        @Schema(description = "物品代碼關鍵字", example = "LAPTOP")
        private String itemCode;

        @Schema(description = "物品名稱關鍵字", example = "Dell")
        private String itemName;

        @Schema(description = "借用人ID", example = "1")
        private Long borrowerId;

        @Schema(description = "借用人姓名關鍵字", example = "張")
        private String borrowerName;

        @Schema(description = "部門", example = "IT部")
        private String department;

        @Schema(description = "借還狀態", example = "BORROWED")
        private BorrowStatusEnum status;

        @Schema(description = "是否逾期", example = "false")
        private Boolean overdue;

        @Schema(description = "借用開始日期", example = "2024-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate borrowStartDate;

        @Schema(description = "借用結束日期", example = "2024-01-31")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate borrowEndDate;

        @Schema(description = "歸還開始日期", example = "2024-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate returnStartDate;

        @Schema(description = "歸還結束日期", example = "2024-01-31")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate returnEndDate;

        @Schema(description = "關鍵字搜尋", example = "筆記型電腦")
        private String keyword;

        @Schema(description = "頁碼", example = "1")
        private Integer page = 1;

        @Schema(description = "每頁數量", example = "10")
        private Integer size = 10;

        @Schema(description = "排序欄位", example = "borrowDate")
        private String sortBy = "borrowDate";

        @Schema(description = "排序方向", example = "DESC")
        private String sortDir = "DESC";
    }

    /**
     * 借還統計 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "借還統計")
    public static class Statistics {

        @Schema(description = "總借用次數", example = "500")
        private Long totalBorrowCount;

        @Schema(description = "總歸還次數", example = "450")
        private Long totalReturnCount;

        @Schema(description = "當前借出數量", example = "50")
        private Long currentBorrowedCount;

        @Schema(description = "逾期記錄數", example = "5")
        private Long overdueCount;

        @Schema(description = "平均借用天數", example = "15.5")
        private Double averageBorrowDays;

        @Schema(description = "總罰款金額", example = "1500.00")
        private BigDecimal totalPenaltyAmount;

        @Schema(description = "已支付罰款金額", example = "1200.00")
        private BigDecimal paidPenaltyAmount;

        @Schema(description = "統計時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }

    /**
     * 條碼掃描借用請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼掃描借用請求")
    public static class BarcodeBorrowRequest {

        @NotBlank(message = "條碼不能為空")
        @Schema(description = "條碼", example = "1234567890123", required = true)
        private String barcode;

        @NotNull(message = "借用人ID不能為空")
        @Schema(description = "借用人ID", example = "1", required = true)
        private Long borrowerId;

        @NotNull(message = "借用數量不能為空")
        @Min(value = 1, message = "借用數量必須大於0")
        @Schema(description = "借用數量", example = "1", required = true)
        private Integer quantity;

        @NotNull(message = "預計歸還日期不能為空")
        @Future(message = "預計歸還日期必須是未來日期")
        @Schema(description = "預計歸還日期", example = "2024-01-31", required = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expectedReturnDate;

        @Size(max = 500, message = "借用目的長度不能超過500字元")
        @Schema(description = "借用目的", example = "出差使用")
        private String purpose;

        @Size(max = 1000, message = "借用備註長度不能超過1000字元")
        @Schema(description = "借用備註", example = "需要安裝特定軟體")
        private String borrowNotes;
    }

    /**
     * 條碼掃描歸還請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "條碼掃描歸還請求")
    public static class BarcodeReturnRequest {

        @NotBlank(message = "條碼不能為空")
        @Schema(description = "條碼", example = "1234567890123", required = true)
        private String barcode;

        @NotNull(message = "借用記錄ID不能為空")
        @Schema(description = "借用記錄ID", example = "1", required = true)
        private Long borrowRecordId;

        @NotNull(message = "歸還數量不能為空")
        @Min(value = 1, message = "歸還數量必須大於0")
        @Schema(description = "歸還數量", example = "1", required = true)
        private Integer returnQuantity;

        @NotNull(message = "歸還狀況不能為空")
        @Schema(description = "歸還狀況", example = "GOOD", required = true)
        private ReturnConditionEnum returnCondition;

        @Size(max = 1000, message = "歸還備註長度不能超過1000字元")
        @Schema(description = "歸還備註", example = "設備狀況良好")
        private String returnNotes;
    }

    /**
     * 簡化版借還記錄 DTO（用於列表顯示）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "簡化版借還記錄")
    public static class SimpleRecord {

        @Schema(description = "借還記錄ID", example = "1")
        private Long id;

        @Schema(description = "借還單號", example = "BR202401010001")
        private String recordNumber;

        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
        private String itemName;

        @Schema(description = "借用人姓名", example = "張三")
        private String borrowerName;

        @Schema(description = "借用數量", example = "1")
        private Integer quantity;

        @Schema(description = "借用日期", example = "2024-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate borrowDate;

        @Schema(description = "預計歸還日期", example = "2024-01-31")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expectedReturnDate;

        @Schema(description = "借還狀態", example = "BORROWED")
        private BorrowStatusEnum status;

        @Schema(description = "是否逾期", example = "false")
        private Boolean overdue;

        @Schema(description = "逾期天數", example = "0")
        private Integer overdueDays;
    }
}
