package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.InventoryDTO;
import com.cheng.linegroup.service.inventory.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 庫存管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/stock")
@RequiredArgsConstructor
@Validated
@Tag(name = "庫存管理", description = "庫存數量管理相關 API")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 庫存調整
     */
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "庫存調整", description = "調整物品庫存數量")
    public ApiResponse<InventoryDTO> adjustInventory(@Valid @RequestBody InventoryDTO.AdjustRequest request) {
        try {
            InventoryDTO inventory = inventoryService.adjustInventory(request);
            return ApiResponse.success("庫存調整成功", inventory);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("庫存調整失敗", e);
            return ApiResponse.error("庫存調整失敗: " + e.getMessage());
        }
    }

    /**
     * 批次庫存調整
     */
    @PostMapping("/batch-adjust")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "批次庫存調整", description = "批次調整多個物品的庫存數量")
    public ApiResponse<List<InventoryDTO>> batchAdjustInventory(@Valid @RequestBody List<InventoryDTO.AdjustRequest> requests) {
        try {
            List<InventoryDTO> inventories = inventoryService.batchAdjustInventory(requests);
            return ApiResponse.success("批次庫存調整成功", inventories);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("批次庫存調整失敗", e);
            return ApiResponse.error("批次庫存調整失敗: " + e.getMessage());
        }
    }

    /**
     * 設定庫存警告
     */
    @PutMapping("/setting")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "設定庫存警告", description = "設定物品的庫存警告閾值")
    public ApiResponse<InventoryDTO> setStockLevel(@Valid @RequestBody InventoryDTO.SettingRequest request) {
        try {
            InventoryDTO inventory = inventoryService.setStockLevel(request);
            return ApiResponse.success("庫存警告設定成功", inventory);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("設定庫存警告失敗", e);
            return ApiResponse.error("設定庫存警告失敗: " + e.getMessage());
        }
    }

    /**
     * 庫存盤點
     */
    @PostMapping("/stock-taking")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "庫存盤點", description = "執行庫存盤點並調整差異")
    public ApiResponse<List<InventoryDTO>> stockTaking(@Valid @RequestBody List<InventoryDTO.AdjustRequest> adjustments) {
        try {
            List<InventoryDTO> inventories = inventoryService.stockTaking(adjustments);
            return ApiResponse.success("庫存盤點完成", inventories);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("庫存盤點失敗", e);
            return ApiResponse.error("庫存盤點失敗: " + e.getMessage());
        }
    }

    /**
     * 根據ID查詢庫存
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "查詢庫存詳情", description = "根據ID查詢庫存詳細資訊")
    public ApiResponse<InventoryDTO> getInventoryById(@Parameter(description = "庫存ID") @PathVariable Long id) {
        try {
            InventoryDTO inventory = inventoryService.getInventoryById(id);
            return ApiResponse.success(inventory);
        } catch (IllegalArgumentException e) {
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("查詢庫存失敗，ID: {}", id, e);
            return ApiResponse.error("查詢庫存失敗: " + e.getMessage());
        }
    }

    /**
     * 根據物品ID查詢庫存
     */
    @GetMapping("/item/{itemId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "根據物品ID查詢庫存", description = "根據物品ID查詢對應的庫存資訊")
    public ApiResponse<InventoryDTO> getInventoryByItemId(@Parameter(description = "物品ID") @PathVariable Long itemId) {
        try {
            InventoryDTO inventory = inventoryService.getInventoryByItemId(itemId);
            return ApiResponse.success(inventory);
        } catch (IllegalArgumentException e) {
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("根據物品ID查詢庫存失敗，物品ID: {}", itemId, e);
            return ApiResponse.error("查詢庫存失敗: " + e.getMessage());
        }
    }

    /**
     * 分頁查詢庫存
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "分頁查詢庫存", description = "根據條件分頁查詢庫存列表")
    public ApiResponse<PageResponse<InventoryDTO>> getInventories(
            @Parameter(description = "物品代碼關鍵字") @RequestParam(required = false) String itemCode,
            @Parameter(description = "物品名稱關鍵字") @RequestParam(required = false) String itemName,
            @Parameter(description = "分類ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "是否有庫存") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "是否低庫存") @RequestParam(required = false) Boolean lowStock,
            @Parameter(description = "是否缺貨") @RequestParam(required = false) Boolean outOfStock,
            @Parameter(description = "最小總數量") @RequestParam(required = false) Integer minTotalQuantity,
            @Parameter(description = "最大總數量") @RequestParam(required = false) Integer maxTotalQuantity,
            @Parameter(description = "關鍵字搜尋") @RequestParam(required = false) String keyword,
            @Parameter(description = "頁碼") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每頁數量") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(required = false, defaultValue = "totalQuantity") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        try {
            InventoryDTO.QueryRequest request = InventoryDTO.QueryRequest.builder()
                    .itemCode(itemCode)
                    .itemName(itemName)
                    .categoryId(categoryId)
                    .inStock(inStock)
                    .lowStock(lowStock)
                    .outOfStock(outOfStock)
                    .minTotalQuantity(minTotalQuantity)
                    .maxTotalQuantity(maxTotalQuantity)
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDir(sortDir)
                    .build();

            PageResponse<InventoryDTO> inventories = inventoryService.getInventories(request);
            return ApiResponse.success(inventories);
        } catch (Exception e) {
            log.error("查詢庫存列表失敗", e);
            return ApiResponse.error("查詢庫存列表失敗: " + e.getMessage());
        }
    }

    /**
     * 取得低庫存警告列表
     */
    @GetMapping("/alerts/low-stock")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得低庫存警告", description = "取得所有低庫存警告的物品")
    public ApiResponse<List<InventoryDTO.Alert>> getLowStockAlerts() {
        try {
            List<InventoryDTO.Alert> alerts = inventoryService.getLowStockAlerts();
            return ApiResponse.success(alerts);
        } catch (Exception e) {
            log.error("取得低庫存警告失敗", e);
            return ApiResponse.error("取得低庫存警告失敗: " + e.getMessage());
        }
    }

    /**
     * 取得缺貨警告列表
     */
    @GetMapping("/alerts/out-of-stock")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得缺貨警告", description = "取得所有缺貨警告的物品")
    public ApiResponse<List<InventoryDTO.Alert>> getOutOfStockAlerts() {
        try {
            List<InventoryDTO.Alert> alerts = inventoryService.getOutOfStockAlerts();
            return ApiResponse.success(alerts);
        } catch (Exception e) {
            log.error("取得缺貨警告失敗", e);
            return ApiResponse.error("取得缺貨警告失敗: " + e.getMessage());
        }
    }

    /**
     * 取得庫存統計資訊
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得庫存統計", description = "取得庫存相關統計資訊")
    public ApiResponse<InventoryDTO.Statistics> getInventoryStatistics() {
        try {
            InventoryDTO.Statistics statistics = inventoryService.getInventoryStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("取得庫存統計失敗", e);
            return ApiResponse.error("取得庫存統計失敗: " + e.getMessage());
        }
    }
}
