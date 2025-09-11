package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.inventory.ReportDTO;
import com.cheng.linegroup.service.inventory.ReportService;
import com.cheng.linegroup.service.inventory.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 報表管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/reports")
@RequiredArgsConstructor
@Validated
@Tag(name = "報表管理", description = "庫存報表管理相關 API")
public class ReportController {

    private final ReportService reportService;
    private final FileService fileService;

    /**
     * 產生庫存報表
     */
    @PostMapping("/inventory")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "產生庫存報表", description = "產生庫存狀況報表")
    public ApiResponse<ReportDTO.ExportResponse> generateInventoryReport(@Valid @RequestBody ReportDTO.InventoryReportRequest request) {
        try {
            ReportDTO.ExportResponse response = reportService.generateInventoryExcelReport(request);
            return ApiResponse.success("庫存報表產生成功", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("產生庫存報表失敗", e);
            return ApiResponse.error("產生庫存報表失敗: " + e.getMessage());
        }
    }

    /**
     * 產生借還報表
     */
    @PostMapping("/borrow")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "產生借還報表", description = "產生借還記錄報表")
    public ApiResponse<ReportDTO.ExportResponse> generateBorrowReport(@Valid @RequestBody ReportDTO.BorrowReportRequest request) {
        try {
            ReportDTO.ExportResponse response = reportService.generateBorrowExcelReport(request);
            return ApiResponse.success("借還報表產生成功", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("產生借還報表失敗", e);
            return ApiResponse.error("產生借還報表失敗: " + e.getMessage());
        }
    }

    /**
     * 取得儀表板統計資料
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得儀表板統計", description = "取得儀表板統計資料")
    public ApiResponse<ReportDTO.DashboardStatistics> getDashboardStatistics() {
        try {
            ReportDTO.DashboardStatistics statistics = reportService.getDashboardStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("取得儀表板統計失敗", e);
            return ApiResponse.error("取得儀表板統計失敗: " + e.getMessage());
        }
    }

    /**
     * 下載報表檔案
     */
    @GetMapping("/download/{fileId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "下載報表檔案", description = "下載產生的報表檔案")
    public ResponseEntity<Resource> downloadReport(@Parameter(description = "檔案ID") @PathVariable String fileId) {
        try {
            Resource resource = fileService.downloadFile(fileId);
            FileService.FileInfo fileInfo = fileService.getFileInfo(fileId);
            
            if (fileInfo == null) {
                return ResponseEntity.notFound().build();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + fileInfo.getOriginalName() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, fileInfo.getContentType());
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileInfo.getFileSize()));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (IllegalArgumentException e) {
            log.warn("檔案不存在或已過期，檔案ID: {}", fileId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("下載報表檔案失敗，檔案ID: {}", fileId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
