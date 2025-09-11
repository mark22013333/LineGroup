package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.inventory.ReportDTO;
import com.cheng.linegroup.entity.inventory.Item;
import com.cheng.linegroup.entity.inventory.Inventory;
import com.cheng.linegroup.entity.inventory.BorrowRecord;
import com.cheng.linegroup.repository.inventory.CategoryRepository;
import com.cheng.linegroup.repository.inventory.ItemRepository;
import com.cheng.linegroup.repository.inventory.InventoryRepository;
import com.cheng.linegroup.repository.inventory.BorrowRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 報表服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    /**
     * 產生庫存報表 Excel
     */
    public ReportDTO.ExportResponse generateInventoryExcelReport(ReportDTO.InventoryReportRequest request) {
        try {
            // 查詢資料
            List<Item> items = getItemsForReport(request);
            
            // 建立 Excel 工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("庫存報表");
            
            // 建立樣式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 建立標題行
            createInventoryReportHeader(sheet, headerStyle);
            
            // 填入資料
            fillInventoryReportData(sheet, items, dataStyle);
            
            // 自動調整欄寬
            autoSizeColumns(sheet, 12);
            
            // 轉換為位元組陣列
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] excelData = outputStream.toByteArray();
            outputStream.close();
            
            // 產生檔案資訊
            String fileName = "inventory_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            
            return ReportDTO.ExportResponse.builder()
                    .fileName(fileName)
                    .fileSize((long) excelData.length)
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .downloadUrl("/api/inventory/reports/download/" + generateFileId())
                    .fileId(generateFileId())
                    .expiryTime(LocalDateTime.now().plusHours(24))
                    .generateTime(LocalDateTime.now())
                    .build();
            
        } catch (IOException e) {
            log.error("產生庫存 Excel 報表失敗", e);
            throw new RuntimeException("產生庫存 Excel 報表失敗: " + e.getMessage());
        }
    }

    /**
     * 產生借還記錄報表 Excel
     */
    public ReportDTO.ExportResponse generateBorrowExcelReport(ReportDTO.BorrowReportRequest request) {
        try {
            // 查詢資料
            List<BorrowRecord> records = getBorrowRecordsForReport(request);
            
            // 建立 Excel 工作簿
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("借還記錄報表");
            
            // 建立樣式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 建立標題行
            createBorrowReportHeader(sheet, headerStyle);
            
            // 填入資料
            fillBorrowReportData(sheet, records, dataStyle);
            
            // 自動調整欄寬
            autoSizeColumns(sheet, 10);
            
            // 轉換為位元組陣列
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            byte[] excelData = outputStream.toByteArray();
            outputStream.close();
            
            // 產生檔案資訊
            String fileName = "borrow_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
            
            return ReportDTO.ExportResponse.builder()
                    .fileName(fileName)
                    .fileSize((long) excelData.length)
                    .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .downloadUrl("/api/inventory/reports/download/" + generateFileId())
                    .fileId(generateFileId())
                    .expiryTime(LocalDateTime.now().plusHours(24))
                    .generateTime(LocalDateTime.now())
                    .build();
            
        } catch (IOException e) {
            log.error("產生借還記錄 Excel 報表失敗", e);
            throw new RuntimeException("產生借還記錄 Excel 報表失敗: " + e.getMessage());
        }
    }

    /**
     * 取得儀表板統計資料
     */
    public ReportDTO.DashboardStatistics getDashboardStatistics() {
        try {
            // 統計物品總數
            long totalItemTypes = itemRepository.count();
            
            // 統計總庫存數量
            long totalQuantity = inventoryRepository.findAll().stream()
                    .mapToLong(Inventory::getCurrentQuantity)
                    .sum();
            
            // 統計低庫存物品數量
            long lowStockItemCount = inventoryRepository.countLowStockItems();
            
            // 統計缺貨物品數量
            long outOfStockItemCount = inventoryRepository.countOutOfStockItems();
            
            // 統計庫存總價值
            BigDecimal totalStockValue = calculateTotalStockValue();
            
            // 統計當前借用數量
            long currentBorrowedCount = borrowRecordRepository.countCurrentBorrows();
            
            // 統計今日借用數量
            long todayBorrowCount = borrowRecordRepository.countTodayBorrows();
            
            // 統計今日歸還數量
            long todayReturnCount = borrowRecordRepository.countTodayReturns();
            
            // 統計逾期數量
            long overdueCount = borrowRecordRepository.countOverdueRecords();
            
            // 統計即將到期數量
            long dueSoonCount = borrowRecordRepository.countRecordsDueSoon(3);
            
            // 統計本月借用數量
            long monthlyBorrowCount = borrowRecordRepository.countMonthlyBorrows();
            
            // 統計上月借用數量
            long lastMonthBorrowCount = borrowRecordRepository.countLastMonthBorrows();
            
            // 計算借用趨勢
            double borrowTrend = calculateBorrowTrend(monthlyBorrowCount, lastMonthBorrowCount);
            
            return ReportDTO.DashboardStatistics.builder()
                    .totalItemTypes(totalItemTypes)
                    .totalQuantity(totalQuantity)
                    .lowStockItemCount(lowStockItemCount)
                    .outOfStockItemCount(outOfStockItemCount)
                    .totalStockValue(totalStockValue)
                    .currentBorrowedCount(currentBorrowedCount)
                    .todayBorrowCount(todayBorrowCount)
                    .todayReturnCount(todayReturnCount)
                    .overdueCount(overdueCount)
                    .dueSoonCount(dueSoonCount)
                    .monthlyBorrowCount(monthlyBorrowCount)
                    .lastMonthBorrowCount(lastMonthBorrowCount)
                    .borrowTrend(borrowTrend)
                    .popularItems(List.of()) // TODO: 實作熱門物品統計
                    .activeUsers(List.of())  // TODO: 實作活躍使用者統計
                    .statisticsTime(LocalDateTime.now())
                    .build();
            
        } catch (Exception e) {
            log.error("取得儀表板統計資料失敗", e);
            throw new RuntimeException("取得儀表板統計資料失敗: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private List<Item> getItemsForReport(ReportDTO.InventoryReportRequest request) {
        // TODO: 根據請求條件查詢物品
        return itemRepository.findAll();
    }

    private List<BorrowRecord> getBorrowRecordsForReport(ReportDTO.BorrowReportRequest request) {
        // TODO: 根據請求條件查詢借還記錄
        return borrowRecordRepository.findAll();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createInventoryReportHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"物品代碼", "物品名稱", "分類", "品牌", "型號", "單位", "單價", 
                           "當前庫存", "可用庫存", "最低庫存", "存放位置", "狀態"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillInventoryReportData(Sheet sheet, List<Item> items, CellStyle dataStyle) {
        int rowNum = 1;
        for (Item item : items) {
            Row row = sheet.createRow(rowNum++);
            Inventory inventory = item.getInventory();
            
            createCell(row, 0, item.getCode(), dataStyle);
            createCell(row, 1, item.getName(), dataStyle);
            createCell(row, 2, item.getCategory().getName(), dataStyle);
            createCell(row, 3, item.getBrand(), dataStyle);
            createCell(row, 4, item.getModel(), dataStyle);
            createCell(row, 5, item.getUnit(), dataStyle);
            createCell(row, 6, item.getUnitPrice() != null ? item.getUnitPrice().toString() : "", dataStyle);
            createCell(row, 7, inventory != null ? inventory.getCurrentQuantity().toString() : "0", dataStyle);
            createCell(row, 8, inventory != null ? inventory.getAvailableQuantity().toString() : "0", dataStyle);
            createCell(row, 9, inventory != null ? inventory.getMinStockLevel().toString() : "0", dataStyle);
            createCell(row, 10, item.getLocation(), dataStyle);
            createCell(row, 11, item.getStatus().name(), dataStyle);
        }
    }

    private void createBorrowReportHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"借還單號", "物品代碼", "物品名稱", "借用人", "部門", 
                           "借用數量", "借用時間", "預計歸還", "實際歸還", "狀態"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillBorrowReportData(Sheet sheet, List<BorrowRecord> records, CellStyle dataStyle) {
        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (BorrowRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            
            createCell(row, 0, record.getRecordNumber(), dataStyle);
            createCell(row, 1, record.getItem().getCode(), dataStyle);
            createCell(row, 2, record.getItem().getName(), dataStyle);
            createCell(row, 3, record.getBorrowerName(), dataStyle);
            createCell(row, 4, record.getBorrowerDepartment(), dataStyle);
            createCell(row, 5, record.getQuantity().toString(), dataStyle);
            createCell(row, 6, record.getBorrowDate().format(formatter), dataStyle);
            createCell(row, 7, record.getExpectedReturnDate() != null ? 
                      record.getExpectedReturnDate().format(formatter) : "", dataStyle);
            createCell(row, 8, record.getActualReturnDate() != null ? 
                      record.getActualReturnDate().format(formatter) : "", dataStyle);
            createCell(row, 9, record.getStatus().name(), dataStyle);
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private BigDecimal calculateTotalStockValue() {
        return inventoryRepository.findAll().stream()
                .filter(inventory -> inventory.getItem().getUnitPrice() != null)
                .map(inventory -> inventory.getItem().getUnitPrice()
                        .multiply(BigDecimal.valueOf(inventory.getCurrentQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateBorrowTrend(long currentMonth, long lastMonth) {
        if (lastMonth == 0) {
            return currentMonth > 0 ? 100.0 : 0.0;
        }
        return ((double) (currentMonth - lastMonth) / lastMonth) * 100.0;
    }

    private String generateFileId() {
        return "FILE_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 1000);
    }
}
