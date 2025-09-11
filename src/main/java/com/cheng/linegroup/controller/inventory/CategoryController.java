package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.CategoryDTO;
import com.cheng.linegroup.service.inventory.CategoryService;
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
 * 分類管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "分類管理", description = "庫存分類管理相關 API")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 建立分類
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "建立分類", description = "建立新的物品分類")
    public ApiResponse<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO.CreateRequest request) {
        try {
            CategoryDTO category = categoryService.createCategory(request);
            return ApiResponse.success("分類建立成功", category);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("建立分類失敗", e);
            return ApiResponse.error("建立分類失敗: " + e.getMessage());
        }
    }

    /**
     * 更新分類
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "更新分類", description = "更新指定分類的資訊")
    public ApiResponse<CategoryDTO> updateCategory(
            @Parameter(description = "分類ID") @PathVariable Long id,
            @Valid @RequestBody CategoryDTO.UpdateRequest request) {
        try {
            CategoryDTO category = categoryService.updateCategory(id, request);
            return ApiResponse.success("分類更新成功", category);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("更新分類失敗，ID: {}", id, e);
            return ApiResponse.error("更新分類失敗: " + e.getMessage());
        }
    }

    /**
     * 刪除分類
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "刪除分類", description = "刪除指定的分類")
    public ApiResponse<Void> deleteCategory(@Parameter(description = "分類ID") @PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ApiResponse.success("分類刪除成功");
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (Exception e) {
            log.error("刪除分類失敗，ID: {}", id, e);
            return ApiResponse.error("刪除分類失敗: " + e.getMessage());
        }
    }

    /**
     * 根據ID查詢分類
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "查詢分類詳情", description = "根據ID查詢分類詳細資訊")
    public ApiResponse<CategoryDTO> getCategoryById(@Parameter(description = "分類ID") @PathVariable Long id) {
        try {
            CategoryDTO category = categoryService.getCategoryById(id);
            return ApiResponse.success(category);
        } catch (IllegalArgumentException e) {
            return ApiResponse.notFound(e.getMessage());
        } catch (Exception e) {
            log.error("查詢分類失敗，ID: {}", id, e);
            return ApiResponse.error("查詢分類失敗: " + e.getMessage());
        }
    }

    /**
     * 分頁查詢分類
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "分頁查詢分類", description = "根據條件分頁查詢分類列表")
    public ApiResponse<PageResponse<CategoryDTO>> getCategories(
            @Parameter(description = "分類名稱關鍵字") @RequestParam(required = false) String name,
            @Parameter(description = "分類代碼") @RequestParam(required = false) String code,
            @Parameter(description = "父分類ID") @RequestParam(required = false) Long parentId,
            @Parameter(description = "是否啟用") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "是否包含子分類") @RequestParam(required = false, defaultValue = "false") Boolean includeChildren,
            @Parameter(description = "頁碼") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每頁數量") @RequestParam(required = false, defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(required = false, defaultValue = "sortOrder") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(required = false, defaultValue = "ASC") String sortDir) {
        try {
            CategoryDTO.QueryRequest request = CategoryDTO.QueryRequest.builder()
                    .name(name)
                    .code(code)
                    .parentId(parentId)
                    .enabled(enabled)
                    .includeChildren(includeChildren)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDir(sortDir)
                    .build();

            PageResponse<CategoryDTO> categories = categoryService.getCategories(request);
            return ApiResponse.success(categories);
        } catch (Exception e) {
            log.error("查詢分類列表失敗", e);
            return ApiResponse.error("查詢分類列表失敗: " + e.getMessage());
        }
    }

    /**
     * 取得分類樹狀結構
     */
    @GetMapping("/tree")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得分類樹狀結構", description = "取得完整的分類階層樹狀結構")
    public ApiResponse<List<CategoryDTO.TreeNode>> getCategoryTree(
            @Parameter(description = "是否只顯示啟用的分類") @RequestParam(required = false, defaultValue = "true") Boolean enabledOnly) {
        try {
            List<CategoryDTO.TreeNode> tree = categoryService.getCategoryTree(enabledOnly);
            return ApiResponse.success(tree);
        } catch (Exception e) {
            log.error("取得分類樹狀結構失敗", e);
            return ApiResponse.error("取得分類樹狀結構失敗: " + e.getMessage());
        }
    }

    /**
     * 取得所有啟用的分類
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得啟用分類列表", description = "取得所有啟用的分類（平面列表）")
    public ApiResponse<List<CategoryDTO>> getEnabledCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getEnabledCategories();
            return ApiResponse.success(categories);
        } catch (Exception e) {
            log.error("取得啟用分類列表失敗", e);
            return ApiResponse.error("取得啟用分類列表失敗: " + e.getMessage());
        }
    }

    /**
     * 根據代碼查詢分類
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "根據代碼查詢分類", description = "根據分類代碼查詢分類資訊")
    public ApiResponse<CategoryDTO> getCategoryByCode(@Parameter(description = "分類代碼") @PathVariable String code) {
        try {
            return categoryService.getCategoryByCode(code)
                    .map(category -> ApiResponse.success(category))
                    .orElse(ApiResponse.notFound("分類不存在"));
        } catch (Exception e) {
            log.error("根據代碼查詢分類失敗，代碼: {}", code, e);
            return ApiResponse.error("查詢分類失敗: " + e.getMessage());
        }
    }

    /**
     * 取得分類統計資訊
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "取得分類統計", description = "取得分類相關統計資訊")
    public ApiResponse<CategoryDTO.Statistics> getCategoryStatistics() {
        try {
            CategoryDTO.Statistics statistics = categoryService.getCategoryStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("取得分類統計失敗", e);
            return ApiResponse.error("取得分類統計失敗: " + e.getMessage());
        }
    }
}
