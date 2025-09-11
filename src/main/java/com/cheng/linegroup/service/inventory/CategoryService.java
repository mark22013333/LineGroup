package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.CategoryDTO;
import com.cheng.linegroup.entity.inventory.Category;
import com.cheng.linegroup.enums.StatusEnum;
import com.cheng.linegroup.repository.inventory.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 分類服務層
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * 建立分類
     */
    @Transactional
    public CategoryDTO createCategory(CategoryDTO.CreateRequest request) {
        log.info("建立分類: {}", request.getName());

        // 檢查分類代碼是否重複
        if (request.getCode() != null && categoryRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("分類代碼已存在: " + request.getCode());
        }

        // 檢查父分類是否存在
        Category parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("父分類不存在"));
        }

        // 建立分類實體
        Category category = Category.builder()
                .name(request.getName())
                .categoryCode(request.getCode())
                .description(request.getDescription())
                .parent(parent)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .status(request.getEnabled() != null && request.getEnabled() ? StatusEnum.ACTIVE : StatusEnum.INACTIVE)
                .build();

        category = categoryRepository.save(category);
        log.info("分類建立成功，ID: {}", category.getId());

        return convertToDTO(category);
    }

    /**
     * 更新分類
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO.UpdateRequest request) {
        log.info("更新分類，ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));

        // 檢查分類代碼是否重複（排除自己）
        if (request.getCode() != null && !request.getCode().equals(category.getCode()) 
            && categoryRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("分類代碼已存在: " + request.getCode());
        }

        // 檢查父分類
        Category parent = null;
        if (request.getParentId() != null && request.getParentId() > 0) {
            if (request.getParentId().equals(id)) {
                throw new IllegalArgumentException("不能將自己設為父分類");
            }
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("父分類不存在"));
            
            // 檢查是否會造成循環引用
            if (isDescendantOf(parent, category)) {
                throw new IllegalArgumentException("不能將子分類設為父分類");
            }
        }

        // 更新分類資訊
        category.setName(request.getName());
        category.setCategoryCode(request.getCode());
        category.setDescription(request.getDescription());
        category.setParent(parent);
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : category.getSortOrder());
        category.setStatus(request.getEnabled() != null && request.getEnabled() ? StatusEnum.ACTIVE : StatusEnum.INACTIVE);

        category = categoryRepository.save(category);
        log.info("分類更新成功，ID: {}", category.getId());

        return convertToDTO(category);
    }

    /**
     * 刪除分類
     */
    @Transactional
    public void deleteCategory(Long id) {
        log.info("刪除分類，ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));

        // 檢查是否有子分類
        if (categoryRepository.countByParent(category) > 0) {
            throw new IllegalArgumentException("該分類下還有子分類，無法刪除");
        }

        // 檢查是否有物品
        if (categoryRepository.countItemsByCategory(category) > 0) {
            throw new IllegalArgumentException("該分類下還有物品，無法刪除");
        }

        categoryRepository.delete(category);
        log.info("分類刪除成功，ID: {}", id);
    }

    /**
     * 根據ID查詢分類
     */
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分類不存在"));
        return convertToDTO(category);
    }

    /**
     * 分頁查詢分類
     */
    public PageResponse<CategoryDTO> getCategories(CategoryDTO.QueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        Page<Category> categoryPage = categoryRepository.findCategoriesByConditions(
                request.getName(),
                request.getCode(),
                request.getParentId(),
                request.getEnabled(),
                pageable
        );

        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(categoryDTOs, categoryPage.getTotalElements(), 
                              request.getPage() - 1, request.getSize());
    }

    /**
     * 取得分類樹狀結構
     */
    public List<CategoryDTO.TreeNode> getCategoryTree(Boolean enabledOnly) {
        List<Category> rootCategories = categoryRepository.findRootCategories(enabledOnly);
        return rootCategories.stream()
                .map(this::convertToTreeNode)
                .collect(Collectors.toList());
    }

    /**
     * 取得所有啟用的分類（平面列表）
     */
    public List<CategoryDTO> getEnabledCategories() {
        List<Category> categories = categoryRepository.findByEnabledTrueOrderBySortOrder();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根據代碼查詢分類
     */
    public Optional<CategoryDTO> getCategoryByCode(String code) {
        return categoryRepository.findByCode(code)
                .map(this::convertToDTO);
    }

    /**
     * 取得分類統計資訊
     */
    public CategoryDTO.Statistics getCategoryStatistics() {
        long totalCategories = categoryRepository.count();
        long enabledCategories = categoryRepository.countByEnabledTrue();
        long rootCategories = categoryRepository.countByParentIsNull();
        
        return CategoryDTO.Statistics.builder()
                .totalCategories(totalCategories)
                .enabledCategories(enabledCategories)
                .rootCategories(rootCategories)
                .build();
    }

    /**
     * 檢查是否為子分類
     */
    private boolean isDescendantOf(Category ancestor, Category descendant) {
        Category current = ancestor.getParent();
        while (current != null) {
            if (current.getId().equals(descendant.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * 轉換為 DTO
     */
    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .level(category.getLevel())
                .sortOrder(category.getSortOrder())
                .enabled(category.getEnabled())
                .childrenCount(categoryRepository.countByParent(category))
                .itemCount(categoryRepository.countItemsByCategory(category))
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .createdBy(category.getCreatedBy() != null ? category.getCreatedBy().getUsername() : null)
                .updatedBy(category.getUpdatedBy() != null ? category.getUpdatedBy().getUsername() : null)
                .build();
    }

    /**
     * 轉換為樹狀節點
     */
    private CategoryDTO.TreeNode convertToTreeNode(Category category) {
        List<CategoryDTO.TreeNode> children = categoryRepository.findByParentOrderBySortOrder(category)
                .stream()
                .map(this::convertToTreeNode)
                .collect(Collectors.toList());

        return CategoryDTO.TreeNode.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .level(category.getLevel())
                .sortOrder(category.getSortOrder())
                .enabled(category.getEnabled())
                .itemCount(categoryRepository.countItemsByCategory(category))
                .children(children)
                .build();
    }
}
