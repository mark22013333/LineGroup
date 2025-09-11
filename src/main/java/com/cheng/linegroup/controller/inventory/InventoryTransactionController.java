package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.InventoryTransactionDTO;
import com.cheng.linegroup.service.inventory.InventoryTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 庫存異動記錄管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/transactions")
@RequiredArgsConstructor
@Validated
@Tag(name = "庫存異動記錄管理", description = "庫存異動記錄管理相關 API")
public class InventoryTransactionController {

    private final InventoryTransactionService transactionService;

    /**
     * 建立庫存異動記錄
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "建立庫存異動記錄", description = "建立新的庫存異動記錄")
    public ApiResponse<InventoryTransactionDTO> createTransaction(@Valid @RequestBody InventoryTransactionDTO.CreateRequest request) {
        try {
            InventoryTransactionDTO transaction = transactionService.createTransaction(request);
            return ApiResponse.success("庫存異動記錄建立成功", transaction);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("建立庫存異動記錄失敗", e);
            return ApiResponse.error("建立庫存異動記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 批次建立庫存異動記錄
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "批次建立庫存異動記錄", description = "批次建立多個庫存異動記錄")
    public ApiResponse<List<InventoryTransactionDTO>> createBatchTransactions(@Valid @RequestBody InventoryTransactionDTO.BatchRequest request) {
        try {
            List<InventoryTransactionDTO> transactions = transactionService.createBatchTransactions(request);
            return ApiResponse.success("批次庫存異動記錄建立成功", transactions);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("批次建立庫存異動記錄失敗", e);
            return ApiResponse.error("批次建立庫存異動記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 審核異動記錄
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "審核異動記錄", description = "審核庫存異動記錄")
    public ApiResponse<InventoryTransactionDTO> approveTransaction(
            @Parameter(description = "異動記錄ID") @PathVariable Long id,
            @Valid @RequestBody InventoryTransactionDTO.ApprovalRequest request) {
        try {
            InventoryTransactionDTO transaction = transactionService.approveTransaction(id, request);
            return ApiResponse.success("異動記錄審核完成", transaction);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("審核異動記錄失敗，ID: {}", id, e);
            return ApiResponse.error("審核異動記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 根據ID查詢異動記錄
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "查詢異動記錄詳情", description = "根據ID查詢異動記錄詳細資訊")
    public ApiResponse<InventoryTransactionDTO> getTransactionById(@Parameter(description = "異動記錄ID") @PathVariable Long id) {
        try {
            InventoryTransactionDTO transaction = transactionService.getTransactionById(id);
            return ApiResponse.success(transaction);
        } catch (IllegalArgumentException e) {
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("查詢異動記錄失敗，ID: {}", id, e);
            return ApiResponse.error("查詢異動記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 分頁查詢異動記錄
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "分頁查詢異動記錄", description = "根據條件分頁查詢異動記錄列表")
    public ApiResponse<PageResponse<InventoryTransactionDTO>> getTransactions(
            @Parameter(description = "異動單號") @RequestParam(required = false) String transactionNumber,
            @Parameter(description = "物品ID") @RequestParam(required = false) Long itemId,
            @Parameter(description = "物品代碼關鍵字") @RequestParam(required = false) String itemCode,
            @Parameter(description = "物品名稱關鍵字") @RequestParam(required = false) String itemName,
            @Parameter(description = "分類ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "異動類型") @RequestParam(required = false) String transactionType,
            @Parameter(description = "參考類型") @RequestParam(required = false) String referenceType,
            @Parameter(description = "處理人ID") @RequestParam(required = false) Long processedById,
            @Parameter(description = "審核人ID") @RequestParam(required = false) Long approvedById,
            @Parameter(description = "異動開始時間") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime transactionStartDate,
            @Parameter(description = "異動結束時間") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime transactionEndDate,
            @Parameter(description = "是否已審核") @RequestParam(required = false) Boolean approved,
            @Parameter(description = "關鍵字搜尋") @RequestParam(required = false) String keyword,
            @Parameter(description = "頁碼") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每頁數量") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(required = false, defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        try {
            InventoryTransactionDTO.QueryRequest request = InventoryTransactionDTO.QueryRequest.builder()
                    .transactionNumber(transactionNumber)
                    .itemId(itemId)
                    .itemCode(itemCode)
                    .itemName(itemName)
                    .transactionType(transactionType != null ? com.cheng.linegroup.enums.inventory.TransactionTypeEnum.valueOf(transactionType) : null)
                    .referenceType(referenceType != null ? com.cheng.linegroup.enums.inventory.ReferenceTypeEnum.valueOf(referenceType) : null)
                    .processedById(processedById)
                    .approvedById(approvedById)
                    .transactionStartDate(transactionStartDate)
                    .transactionEndDate(transactionEndDate)
                    .approved(approved)
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDir(sortDir)
                    .build();

            PageResponse<InventoryTransactionDTO> transactions = transactionService.getTransactions(request);
            return ApiResponse.success(transactions);
        } catch (Exception e) {
            log.error("查詢異動記錄列表失敗", e);
            return ApiResponse.error("查詢異動記錄列表失敗: " + e.getMessage());
        }
    }

    /**
     * 取得物品的異動歷史
     */
    @GetMapping("/item/{itemId}/history")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得物品異動歷史", description = "取得指定物品的異動歷史記錄")
    public ApiResponse<List<InventoryTransactionDTO>> getItemTransactionHistory(@Parameter(description = "物品ID") @PathVariable Long itemId) {
        try {
            List<InventoryTransactionDTO> transactions = transactionService.getItemTransactionHistory(itemId);
            return ApiResponse.success(transactions);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("取得物品異動歷史失敗，物品ID: {}", itemId, e);
            return ApiResponse.error("取得異動歷史失敗: " + e.getMessage());
        }
    }

    /**
     * 取得待審核的異動記錄
     */
    @GetMapping("/pending-approval")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得待審核異動記錄", description = "取得所有待審核的異動記錄")
    public ApiResponse<List<InventoryTransactionDTO>> getPendingApprovalTransactions() {
        try {
            List<InventoryTransactionDTO> transactions = transactionService.getPendingApprovalTransactions();
            return ApiResponse.success(transactions);
        } catch (Exception e) {
            log.error("取得待審核異動記錄失敗", e);
            return ApiResponse.error("取得待審核異動記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 取得異動統計資訊
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得異動統計", description = "取得異動相關統計資訊")
    public ApiResponse<InventoryTransactionDTO.Statistics> getTransactionStatistics(
            @Parameter(description = "開始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            // 預設查詢最近30天
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            InventoryTransactionDTO.Statistics statistics = transactionService.getTransactionStatistics(startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("取得異動統計失敗", e);
            return ApiResponse.error("取得異動統計失敗: " + e.getMessage());
        }
    }

    /**
     * 取得異動趨勢
     */
    @GetMapping("/trends")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得異動趨勢", description = "取得異動趨勢分析資料")
    public ApiResponse<List<InventoryTransactionDTO.Trend>> getTransactionTrends(
            @Parameter(description = "開始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "結束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            // 預設查詢最近30天
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }

            List<InventoryTransactionDTO.Trend> trends = transactionService.getTransactionTrends(startDate, endDate);
            return ApiResponse.success(trends);
        } catch (Exception e) {
            log.error("取得異動趨勢失敗", e);
            return ApiResponse.error("取得異動趨勢失敗: " + e.getMessage());
        }
    }
}
