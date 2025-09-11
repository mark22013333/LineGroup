package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.BorrowRecordDTO;
import com.cheng.linegroup.service.inventory.BorrowRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 借還記錄管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/borrow-records")
@RequiredArgsConstructor
@Validated
@Tag(name = "借還記錄管理", description = "物品借還記錄管理相關 API")
public class BorrowRecordController {

    private final BorrowRecordService borrowRecordService;

    /**
     * 建立借用記錄
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "建立借用記錄", description = "建立新的物品借用記錄")
    public ApiResponse<BorrowRecordDTO> createBorrowRecord(@Valid @RequestBody BorrowRecordDTO.CreateRequest request) {
        try {
            BorrowRecordDTO record = borrowRecordService.createBorrowRecord(request);
            return ApiResponse.success("借用記錄建立成功", record);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("建立借用記錄失敗", e);
            return ApiResponse.error("建立借用記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 歸還物品
     */
    @PutMapping("/{id}/return")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "歸還物品", description = "處理物品歸還")
    public ApiResponse<BorrowRecordDTO> returnItem(
            @Parameter(description = "借用記錄ID") @PathVariable Long id,
            @Valid @RequestBody BorrowRecordDTO.ReturnRequest request) {
        try {
            BorrowRecordDTO record = borrowRecordService.returnItem(id, request);
            return ApiResponse.success("物品歸還成功", record);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("物品歸還失敗，記錄ID: {}", id, e);
            return ApiResponse.error("物品歸還失敗: " + e.getMessage());
        }
    }

    /**
     * 條碼掃描借用
     */
    @PostMapping("/barcode/borrow")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "條碼掃描借用", description = "透過條碼掃描快速借用物品")
    public ApiResponse<BorrowRecordDTO> barcodeBorrow(@Valid @RequestBody BorrowRecordDTO.BarcodeBorrowRequest request) {
        try {
            BorrowRecordDTO record = borrowRecordService.barcodeBorrow(request);
            return ApiResponse.success("條碼掃描借用成功", record);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("條碼掃描借用失敗", e);
            return ApiResponse.error("條碼掃描借用失敗: " + e.getMessage());
        }
    }

    /**
     * 條碼掃描歸還
     */
    @PostMapping("/barcode/return")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "條碼掃描歸還", description = "透過條碼掃描快速歸還物品")
    public ApiResponse<BorrowRecordDTO> barcodeReturn(@Valid @RequestBody BorrowRecordDTO.BarcodeReturnRequest request) {
        try {
            BorrowRecordDTO record = borrowRecordService.barcodeReturn(request);
            return ApiResponse.success("條碼掃描歸還成功", record);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("條碼掃描歸還失敗", e);
            return ApiResponse.error("條碼掃描歸還失敗: " + e.getMessage());
        }
    }

    /**
     * 根據ID查詢借還記錄
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "查詢借還記錄詳情", description = "根據ID查詢借還記錄詳細資訊")
    public ApiResponse<BorrowRecordDTO> getBorrowRecordById(@Parameter(description = "借還記錄ID") @PathVariable Long id) {
        try {
            BorrowRecordDTO record = borrowRecordService.getBorrowRecordById(id);
            return ApiResponse.success(record);
        } catch (IllegalArgumentException e) {
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("查詢借還記錄失敗，ID: {}", id, e);
            return ApiResponse.error("查詢借還記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 根據借還單號查詢記錄
     */
    @GetMapping("/number/{recordNumber}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "根據借還單號查詢記錄", description = "根據借還單號查詢記錄資訊")
    public ApiResponse<BorrowRecordDTO> getBorrowRecordByNumber(@Parameter(description = "借還單號") @PathVariable String recordNumber) {
        try {
            return borrowRecordService.getBorrowRecordByNumber(recordNumber)
                    .map(record -> ApiResponse.success(record))
                    .orElse(ApiResponse.notFound("借還單號對應的記錄不存在"));
        } catch (Exception e) {
            log.error("根據借還單號查詢記錄失敗，單號: {}", recordNumber, e);
            return ApiResponse.error("查詢記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 分頁查詢借還記錄
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "分頁查詢借還記錄", description = "根據條件分頁查詢借還記錄列表")
    public ApiResponse<PageResponse<BorrowRecordDTO>> getBorrowRecords(
            @Parameter(description = "借還單號") @RequestParam(required = false) String recordNumber,
            @Parameter(description = "物品ID") @RequestParam(required = false) Long itemId,
            @Parameter(description = "物品代碼關鍵字") @RequestParam(required = false) String itemCode,
            @Parameter(description = "物品名稱關鍵字") @RequestParam(required = false) String itemName,
            @Parameter(description = "借用人ID") @RequestParam(required = false) Long borrowerId,
            @Parameter(description = "借用人姓名關鍵字") @RequestParam(required = false) String borrowerName,
            @Parameter(description = "部門") @RequestParam(required = false) String department,
            @Parameter(description = "借還狀態") @RequestParam(required = false) String status,
            @Parameter(description = "是否逾期") @RequestParam(required = false) Boolean overdue,
            @Parameter(description = "借用開始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate borrowStartDate,
            @Parameter(description = "借用結束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate borrowEndDate,
            @Parameter(description = "歸還開始日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate returnStartDate,
            @Parameter(description = "歸還結束日期") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate returnEndDate,
            @Parameter(description = "關鍵字搜尋") @RequestParam(required = false) String keyword,
            @Parameter(description = "頁碼") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每頁數量") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(required = false, defaultValue = "borrowDate") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        try {
            BorrowRecordDTO.QueryRequest request = BorrowRecordDTO.QueryRequest.builder()
                    .recordNumber(recordNumber)
                    .itemId(itemId)
                    .itemCode(itemCode)
                    .itemName(itemName)
                    .borrowerId(borrowerId)
                    .borrowerName(borrowerName)
                    .department(department)
                    .status(status != null ? com.cheng.linegroup.enums.inventory.BorrowStatusEnum.valueOf(status) : null)
                    .overdue(overdue)
                    .borrowStartDate(borrowStartDate)
                    .borrowEndDate(borrowEndDate)
                    .returnStartDate(returnStartDate)
                    .returnEndDate(returnEndDate)
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDir(sortDir)
                    .build();

            PageResponse<BorrowRecordDTO> records = borrowRecordService.getBorrowRecords(request);
            return ApiResponse.success(records);
        } catch (Exception e) {
            log.error("查詢借還記錄列表失敗", e);
            return ApiResponse.error("查詢借還記錄列表失敗: " + e.getMessage());
        }
    }

    /**
     * 取得使用者當前借用記錄
     */
    @GetMapping("/user/{userId}/current")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得使用者當前借用記錄", description = "取得指定使用者當前未歸還的借用記錄")
    public ApiResponse<List<BorrowRecordDTO>> getCurrentBorrowsByUser(@Parameter(description = "使用者ID") @PathVariable Long userId) {
        try {
            List<BorrowRecordDTO> records = borrowRecordService.getCurrentBorrowsByUser(userId);
            return ApiResponse.success(records);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("取得使用者當前借用記錄失敗，使用者ID: {}", userId, e);
            return ApiResponse.error("取得借用記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 取得逾期記錄
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得逾期記錄", description = "取得所有逾期未歸還的借用記錄")
    public ApiResponse<List<BorrowRecordDTO>> getOverdueRecords() {
        try {
            List<BorrowRecordDTO> records = borrowRecordService.getOverdueRecords();
            return ApiResponse.success(records);
        } catch (Exception e) {
            log.error("取得逾期記錄失敗", e);
            return ApiResponse.error("取得逾期記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 取得即將到期記錄
     */
    @GetMapping("/due-soon")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得即將到期記錄", description = "取得即將到期的借用記錄")
    public ApiResponse<List<BorrowRecordDTO>> getRecordsDueSoon(
            @Parameter(description = "提前天數") @RequestParam(required = false, defaultValue = "3") Integer days) {
        try {
            List<BorrowRecordDTO> records = borrowRecordService.getRecordsDueSoon(days);
            return ApiResponse.success(records);
        } catch (Exception e) {
            log.error("取得即將到期記錄失敗", e);
            return ApiResponse.error("取得即將到期記錄失敗: " + e.getMessage());
        }
    }

    /**
     * 取得借還統計資訊
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得借還統計", description = "取得借還相關統計資訊")
    public ApiResponse<BorrowRecordDTO.Statistics> getBorrowStatistics(
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

            BorrowRecordDTO.Statistics statistics = borrowRecordService.getBorrowStatistics(startDate, endDate);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("取得借還統計失敗", e);
            return ApiResponse.error("取得借還統計失敗: " + e.getMessage());
        }
    }
}
