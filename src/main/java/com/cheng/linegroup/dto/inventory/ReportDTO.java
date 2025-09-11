package com.cheng.linegroup.dto.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 報表資料傳輸物件
 */
@Data
@Builder
@Schema(description = "報表資料傳輸物件")
public class ReportDTO {

    /**
     * 庫存報表請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存報表請求")
    public static class InventoryReportRequest {

        @Schema(description = "分類ID列表")
        private List<Long> categoryIds;

        @Schema(description = "是否包含子分類", example = "true")
        private Boolean includeSubCategories = true;

        @Schema(description = "是否只顯示有庫存的物品", example = "false")
        private Boolean onlyInStock = false;

        @Schema(description = "是否只顯示低庫存物品", example = "false")
        private Boolean onlyLowStock = false;

        @Schema(description = "匯出格式", example = "EXCEL")
        private String exportFormat = "EXCEL"; // EXCEL, PDF

        @Schema(description = "報表標題", example = "庫存狀況報表")
        private String reportTitle = "庫存狀況報表";

        @Schema(description = "產生時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generateTime = LocalDateTime.now();
    }

    /**
     * 借還報表請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "借還報表請求")
    public static class BorrowReportRequest {

        @NotNull(message = "開始日期不能為空")
        @Schema(description = "開始日期", example = "2024-01-01", required = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @NotNull(message = "結束日期不能為空")
        @Schema(description = "結束日期", example = "2024-01-31", required = true)
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        @Schema(description = "分類ID列表")
        private List<Long> categoryIds;

        @Schema(description = "借用人ID列表")
        private List<Long> borrowerIds;

        @Schema(description = "部門列表")
        private List<String> departments;

        @Schema(description = "是否包含逾期記錄", example = "true")
        private Boolean includeOverdue = true;

        @Schema(description = "是否包含已歸還記錄", example = "true")
        private Boolean includeReturned = true;

        @Schema(description = "匯出格式", example = "EXCEL")
        private String exportFormat = "EXCEL"; // EXCEL, PDF

        @Schema(description = "報表標題", example = "借還記錄報表")
        private String reportTitle = "借還記錄報表";

        @Schema(description = "產生時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generateTime = LocalDateTime.now();
    }

    /**
     * 庫存報表資料 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "庫存報表資料")
    public static class InventoryReportData {

        @Schema(description = "物品代碼", example = "LAPTOP001")
        private String itemCode;

        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
        private String itemName;

        @Schema(description = "分類名稱", example = "電腦設備")
        private String categoryName;

        @Schema(description = "品牌", example = "Dell")
        private String brand;

        @Schema(description = "型號", example = "Latitude 5520")
        private String model;

        @Schema(description = "單位", example = "台")
        private String unit;

        @Schema(description = "單價", example = "35000.00")
        private BigDecimal unitPrice;

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

        @Schema(description = "庫存價值", example = "350000.00")
        private BigDecimal stockValue;

        @Schema(description = "庫存狀態", example = "正常")
        private String stockStatus;

        @Schema(description = "存放位置", example = "A區-1樓-IT室")
        private String location;

        @Schema(description = "最後異動時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastTransactionDate;
    }

    /**
     * 借還報表資料 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "借還報表資料")
    public static class BorrowReportData {

        @Schema(description = "借還單號", example = "BR202401010001")
        private String recordNumber;

        @Schema(description = "物品代碼", example = "LAPTOP001")
        private String itemCode;

        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
        private String itemName;

        @Schema(description = "分類名稱", example = "電腦設備")
        private String categoryName;

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

        @Schema(description = "借用天數", example = "30")
        private Integer borrowDays;

        @Schema(description = "借還狀態", example = "已歸還")
        private String status;

        @Schema(description = "是否逾期", example = "false")
        private Boolean overdue;

        @Schema(description = "逾期天數", example = "0")
        private Integer overdueDays;

        @Schema(description = "罰款金額", example = "0.00")
        private BigDecimal penaltyAmount;

        @Schema(description = "借用目的", example = "出差使用")
        private String purpose;

        @Schema(description = "處理人姓名", example = "李四")
        private String processedByName;
    }

    /**
     * 報表摘要統計 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "報表摘要統計")
    public static class ReportSummary {

        @Schema(description = "總物品種類數", example = "150")
        private Long totalItemTypes;

        @Schema(description = "總庫存數量", example = "1500")
        private Long totalQuantity;

        @Schema(description = "總庫存價值", example = "5250000.00")
        private BigDecimal totalStockValue;

        @Schema(description = "總借用次數", example = "500")
        private Long totalBorrowCount;

        @Schema(description = "當前借出數量", example = "50")
        private Long currentBorrowedCount;

        @Schema(description = "逾期記錄數", example = "5")
        private Long overdueCount;

        @Schema(description = "總罰款金額", example = "1500.00")
        private BigDecimal totalPenaltyAmount;

        @Schema(description = "平均借用天數", example = "15.5")
        private Double averageBorrowDays;

        @Schema(description = "庫存週轉率", example = "2.5")
        private Double turnoverRate;

        @Schema(description = "統計期間", example = "2024-01-01 至 2024-01-31")
        private String statisticsPeriod;
    }

    /**
     * 報表匯出回應 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "報表匯出回應")
    public static class ExportResponse {

        @Schema(description = "檔案名稱", example = "inventory_report_20240101.xlsx")
        private String fileName;

        @Schema(description = "檔案大小（位元組）", example = "1024000")
        private Long fileSize;

        @Schema(description = "檔案類型", example = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        private String contentType;

        @Schema(description = "下載連結", example = "/api/reports/download/abc123def456")
        private String downloadUrl;

        @Schema(description = "檔案ID", example = "abc123def456")
        private String fileId;

        @Schema(description = "過期時間", example = "2024-01-02 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime expiryTime;

        @Schema(description = "產生時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generateTime;
    }

    /**
     * 儀表板統計 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "儀表板統計")
    public static class DashboardStatistics {

        // 庫存統計
        @Schema(description = "總物品種類數", example = "150")
        private Long totalItemTypes;

        @Schema(description = "總庫存數量", example = "1500")
        private Long totalQuantity;

        @Schema(description = "低庫存物品數", example = "10")
        private Long lowStockItemCount;

        @Schema(description = "缺貨物品數", example = "3")
        private Long outOfStockItemCount;

        @Schema(description = "總庫存價值", example = "5250000.00")
        private BigDecimal totalStockValue;

        // 借還統計
        @Schema(description = "當前借出數量", example = "50")
        private Long currentBorrowedCount;

        @Schema(description = "今日借用次數", example = "5")
        private Long todayBorrowCount;

        @Schema(description = "今日歸還次數", example = "3")
        private Long todayReturnCount;

        @Schema(description = "逾期記錄數", example = "5")
        private Long overdueCount;

        @Schema(description = "即將到期記錄數", example = "8")
        private Long dueSoonCount;

        // 趨勢統計
        @Schema(description = "本月借用次數", example = "120")
        private Long monthlyBorrowCount;

        @Schema(description = "上月借用次數", example = "100")
        private Long lastMonthBorrowCount;

        @Schema(description = "借用趨勢", example = "20.0")
        private Double borrowTrend; // 百分比變化

        @Schema(description = "熱門物品列表")
        private List<PopularItem> popularItems;

        @Schema(description = "活躍使用者列表")
        private List<ActiveUser> activeUsers;

        @Schema(description = "統計時間", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }

    /**
     * 熱門物品 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "熱門物品")
    public static class PopularItem {

        @Schema(description = "物品名稱", example = "Dell Latitude 5520 筆記型電腦")
        private String itemName;

        @Schema(description = "借用次數", example = "25")
        private Long borrowCount;

        @Schema(description = "可用數量", example = "8")
        private Integer availableQuantity;
    }

    /**
     * 活躍使用者 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "活躍使用者")
    public static class ActiveUser {

        @Schema(description = "使用者姓名", example = "張三")
        private String userName;

        @Schema(description = "部門", example = "IT部")
        private String department;

        @Schema(description = "借用次數", example = "15")
        private Long borrowCount;

        @Schema(description = "當前借用數量", example = "3")
        private Integer currentBorrowedCount;
    }
}
