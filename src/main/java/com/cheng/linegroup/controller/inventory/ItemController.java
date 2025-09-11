package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.ItemDTO;
import com.cheng.linegroup.service.inventory.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 物品管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/items")
@RequiredArgsConstructor
@Validated
@Tag(name = "物品管理", description = "庫存物品管理相關 API")
public class ItemController {

    private final ItemService itemService;

    /**
     * 建立物品
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "建立物品", description = "建立新的庫存物品")
    public ApiResponse<ItemDTO> createItem(@Valid @RequestBody ItemDTO.CreateRequest request) {
        try {
            ItemDTO item = itemService.createItem(request);
            return ApiResponse.success("物品建立成功", item);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("建立物品失敗", e);
            return ApiResponse.error("建立物品失敗: " + e.getMessage());
        }
    }

    /**
     * 更新物品
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "更新物品", description = "更新指定物品的資訊")
    public ApiResponse<ItemDTO> updateItem(
            @Parameter(description = "物品ID") @PathVariable Long id,
            @Valid @RequestBody ItemDTO.UpdateRequest request) {
        try {
            ItemDTO item = itemService.updateItem(id, request);
            return ApiResponse.success("物品更新成功", item);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("更新物品失敗，ID: {}", id, e);
            return ApiResponse.error("更新物品失敗: " + e.getMessage());
        }
    }

    /**
     * 刪除物品
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "刪除物品", description = "刪除指定的物品")
    public ApiResponse<Void> deleteItem(@Parameter(description = "物品ID") @PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ApiResponse.success("物品刪除成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("刪除物品失敗，ID: {}", id, e);
            return ApiResponse.error("刪除物品失敗: " + e.getMessage());
        }
    }

    /**
     * 根據ID查詢物品
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "查詢物品詳情", description = "根據ID查詢物品詳細資訊")
    public ApiResponse<ItemDTO> getItemById(@Parameter(description = "物品ID") @PathVariable Long id) {
        try {
            ItemDTO item = itemService.getItemById(id);
            return ApiResponse.success(item);
        } catch (IllegalArgumentException e) {
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("查詢物品失敗，ID: {}", id, e);
            return ApiResponse.error("查詢物品失敗: " + e.getMessage());
        }
    }

    /**
     * 根據條碼查詢物品
     */
    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "根據條碼查詢物品", description = "根據條碼查詢物品資訊")
    public ApiResponse<ItemDTO> getItemByBarcode(@Parameter(description = "條碼") @PathVariable String barcode) {
        try {
            return itemService.getItemByBarcode(barcode)
                    .map(item -> ApiResponse.success(item))
                    .orElse(ApiResponse.notFound("條碼對應的物品不存在"));
        } catch (Exception e) {
            log.error("根據條碼查詢物品失敗，條碼: {}", barcode, e);
            return ApiResponse.error("查詢物品失敗: " + e.getMessage());
        }
    }

    /**
     * 根據物品代碼查詢物品
     */
    @GetMapping("/code/{itemCode}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "根據物品代碼查詢物品", description = "根據物品代碼查詢物品資訊")
    public ApiResponse<ItemDTO> getItemByCode(@Parameter(description = "物品代碼") @PathVariable String itemCode) {
        try {
            return itemService.getItemByCode(itemCode)
                    .map(item -> ApiResponse.success(item))
                    .orElse(ApiResponse.notFound("物品代碼對應的物品不存在"));
        } catch (Exception e) {
            log.error("根據物品代碼查詢物品失敗，代碼: {}", itemCode, e);
            return ApiResponse.error("查詢物品失敗: " + e.getMessage());
        }
    }

    /**
     * 分頁查詢物品
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "分頁查詢物品", description = "根據條件分頁查詢物品列表")
    public ApiResponse<PageResponse<ItemDTO>> getItems(
            @Parameter(description = "物品代碼關鍵字") @RequestParam(required = false) String itemCode,
            @Parameter(description = "物品名稱關鍵字") @RequestParam(required = false) String name,
            @Parameter(description = "條碼") @RequestParam(required = false) String barcode,
            @Parameter(description = "分類ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "品牌") @RequestParam(required = false) String brand,
            @Parameter(description = "型號") @RequestParam(required = false) String model,
            @Parameter(description = "是否啟用") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "是否可借用") @RequestParam(required = false) Boolean borrowable,
            @Parameter(description = "是否有庫存") @RequestParam(required = false) Boolean inStock,
            @Parameter(description = "是否低庫存") @RequestParam(required = false) Boolean lowStock,
            @Parameter(description = "關鍵字搜尋") @RequestParam(required = false) String keyword,
            @Parameter(description = "頁碼") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每頁數量") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        try {
            ItemDTO.QueryRequest request = ItemDTO.QueryRequest.builder()
                    .itemCode(itemCode)
                    .name(name)
                    .barcode(barcode)
                    .categoryId(categoryId)
                    .brand(brand)
                    .model(model)
                    .enabled(enabled)
                    .borrowable(borrowable)
                    .inStock(inStock)
                    .lowStock(lowStock)
                    .keyword(keyword)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDir(sortDir)
                    .build();

            PageResponse<ItemDTO> items = itemService.getItems(request);
            return ApiResponse.success(items);
        } catch (Exception e) {
            log.error("查詢物品列表失敗", e);
            return ApiResponse.error("查詢物品列表失敗: " + e.getMessage());
        }
    }

    /**
     * 取得可借用的物品列表
     */
    @GetMapping("/borrowable")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得可借用物品列表", description = "取得所有可借用且有庫存的物品")
    public ApiResponse<List<ItemDTO.SimpleItem>> getBorrowableItems() {
        try {
            List<ItemDTO.SimpleItem> items = itemService.getBorrowableItems();
            return ApiResponse.success(items);
        } catch (Exception e) {
            log.error("取得可借用物品列表失敗", e);
            return ApiResponse.error("取得可借用物品列表失敗: " + e.getMessage());
        }
    }

    /**
     * 根據分類查詢物品
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "根據分類查詢物品", description = "根據分類ID查詢物品列表")
    public ApiResponse<List<ItemDTO>> getItemsByCategory(
            @Parameter(description = "分類ID") @PathVariable Long categoryId,
            @Parameter(description = "是否包含子分類") @RequestParam(required = false, defaultValue = "false") Boolean includeSubCategories) {
        try {
            List<ItemDTO> items = itemService.getItemsByCategory(categoryId, includeSubCategories);
            return ApiResponse.success(items);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("根據分類查詢物品失敗，分類ID: {}", categoryId, e);
            return ApiResponse.error("查詢物品失敗: " + e.getMessage());
        }
    }

    /**
     * 搜尋物品
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "搜尋物品", description = "全文搜尋物品")
    public ApiResponse<PageResponse<ItemDTO>> searchItems(
            @Parameter(description = "搜尋關鍵字") @RequestParam String keyword,
            @Parameter(description = "頁碼") @RequestParam(required = false, defaultValue = "0") Integer page,
            @Parameter(description = "每頁數量") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "DESC") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            PageResponse<ItemDTO> items = itemService.searchItems(keyword, pageable);
            return ApiResponse.success(items);
        } catch (Exception e) {
            log.error("搜尋物品失敗，關鍵字: {}", keyword, e);
            return ApiResponse.error("搜尋物品失敗: " + e.getMessage());
        }
    }

    /**
     * 取得低庫存物品
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得低庫存物品", description = "取得所有低庫存警告的物品")
    public ApiResponse<List<ItemDTO>> getLowStockItems() {
        try {
            List<ItemDTO> items = itemService.getLowStockItems();
            return ApiResponse.success(items);
        } catch (Exception e) {
            log.error("取得低庫存物品失敗", e);
            return ApiResponse.error("取得低庫存物品失敗: " + e.getMessage());
        }
    }

    /**
     * 取得缺貨物品
     */
    @GetMapping("/out-of-stock")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得缺貨物品", description = "取得所有缺貨的物品")
    public ApiResponse<List<ItemDTO>> getOutOfStockItems() {
        try {
            List<ItemDTO> items = itemService.getOutOfStockItems();
            return ApiResponse.success(items);
        } catch (Exception e) {
            log.error("取得缺貨物品失敗", e);
            return ApiResponse.error("取得缺貨物品失敗: " + e.getMessage());
        }
    }

    /**
     * 取得物品統計資訊
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得物品統計", description = "取得物品相關統計資訊")
    public ApiResponse<ItemDTO.Statistics> getItemStatistics() {
        try {
            ItemDTO.Statistics statistics = itemService.getItemStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("取得物品統計失敗", e);
            return ApiResponse.error("取得物品統計失敗: " + e.getMessage());
        }
    }
}
