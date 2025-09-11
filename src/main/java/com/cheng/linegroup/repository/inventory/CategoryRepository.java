package com.cheng.linegroup.repository.inventory;

import com.cheng.linegroup.entity.inventory.Category;
import com.cheng.linegroup.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分類資料存取介面
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根據名稱查詢分類
     */
    Optional<Category> findByNameAndStatus(String name, StatusEnum status);

    /**
     * 查詢所有根分類（沒有父分類的分類）
     */
    List<Category> findByParentIsNullAndStatusOrderByName(StatusEnum status);

    /**
     * 查詢指定父分類下的子分類
     */
    List<Category> findByParentAndStatusOrderByName(Category parent, StatusEnum status);

    /**
     * 根據父分類ID查詢子分類
     */
    List<Category> findByParentIdAndStatusOrderByName(Long parentId, StatusEnum status);

    /**
     * 查詢分類樹狀結構（包含子分類數量）
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL AND c.status = :status ORDER BY c.name")
    List<Category> findRootCategoriesWithChildren(@Param("status") StatusEnum status);

    /**
     * 根據名稱模糊查詢分類
     */
    Page<Category> findByNameContainingIgnoreCaseAndStatus(String name, StatusEnum status, Pageable pageable);

    /**
     * 查詢所有啟用的分類
     */
    List<Category> findByStatusOrderByName(StatusEnum status);

    /**
     * 統計分類下的物品數量
     */
    @Query("SELECT COUNT(i) FROM Item i WHERE i.category = :category AND i.status = 'ACTIVE'")
    long countItemsByCategory(@Param("category") Category category);

    /**
     * 查詢有物品的分類
     */
    @Query("SELECT DISTINCT c FROM Category c INNER JOIN c.items i WHERE i.status = 'ACTIVE' AND c.status = :status")
    List<Category> findCategoriesWithItems(@Param("status") StatusEnum status);

    /**
     * 檢查分類名稱是否存在（排除指定ID）
     */
    boolean existsByNameAndStatusAndIdNot(String name, StatusEnum status, Long id);

    /**
     * 檢查分類是否有子分類
     */
    boolean existsByParentAndStatus(Category parent, StatusEnum status);

    /**
     * 查詢分類路徑
     */
    @Query(value = """
        WITH RECURSIVE category_path AS (
            SELECT id, name, parent_id, name as path, 0 as level
            FROM categories 
            WHERE id = :categoryId
            UNION ALL
            SELECT c.id, c.name, c.parent_id, 
                   CONCAT(c.name, ' > ', cp.path) as path, 
                   cp.level + 1 as level
            FROM categories c
            INNER JOIN category_path cp ON c.id = cp.parent_id
        )
        SELECT path FROM category_path WHERE parent_id IS NULL
        """, nativeQuery = true)
    Optional<String> findCategoryPath(@Param("categoryId") Long categoryId);

    /**
     * 統計啟用的分類數量
     */
    long countByStatus(StatusEnum status);

    /**
     * 統計啟用的分類數量（別名）
     */
    default long countByEnabledTrue() {
        return countByStatus(StatusEnum.ACTIVE);
    }

    /**
     * 統計根分類數量
     */
    long countByParentIsNullAndStatus(StatusEnum status);

    /**
     * 統計根分類數量（別名）
     */
    default long countByParentIsNull() {
        return countByParentIsNullAndStatus(StatusEnum.ACTIVE);
    }

    /**
     * 統計指定父分類下的子分類數量
     */
    long countByParentAndStatus(Category parent, StatusEnum status);

    /**
     * 統計指定父分類下的子分類數量（別名）
     */
    default long countByParent(Category parent) {
        return countByParentAndStatus(parent, StatusEnum.ACTIVE);
    }

    /**
     * 根據父分類查詢子分類（按排序順序）
     */
    List<Category> findByParentAndStatusOrderBySortOrder(Category parent, StatusEnum status);

    /**
     * 根據父分類查詢子分類（按排序順序，別名）
     */
    default List<Category> findByParentOrderBySortOrder(Category parent) {
        return findByParentAndStatusOrderBySortOrder(parent, StatusEnum.ACTIVE);
    }

    /**
     * 檢查分類編號是否存在
     */
    boolean existsByCategoryCodeAndStatus(String categoryCode, StatusEnum status);

    /**
     * 檢查分類編號是否存在（別名）
     */
    default boolean existsByCode(String code) {
        return existsByCategoryCodeAndStatus(code, StatusEnum.ACTIVE);
    }

    /**
     * 根據分類編號查詢
     */
    Optional<Category> findByCategoryCodeAndStatus(String categoryCode, StatusEnum status);

    /**
     * 根據分類編號查詢（別名）
     */
    default Optional<Category> findByCode(String code) {
        return findByCategoryCodeAndStatus(code, StatusEnum.ACTIVE);
    }

    /**
     * 查詢根分類
     */
    default List<Category> findRootCategories(Boolean enabled) {
        StatusEnum status = enabled ? StatusEnum.ACTIVE : StatusEnum.INACTIVE;
        return findByParentIsNullAndStatusOrderByName(status);
    }

    /**
     * 查詢啟用的分類（按排序順序）
     */
    List<Category> findByStatusOrderBySortOrder(StatusEnum status);

    /**
     * 查詢啟用的分類（按排序順序，別名）
     */
    default List<Category> findByEnabledTrueOrderBySortOrder() {
        return findByStatusOrderBySortOrder(StatusEnum.ACTIVE);
    }

    /**
     * 複合條件查詢分類
     */
    @Query("SELECT c FROM Category c WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:code IS NULL OR :code = '' OR LOWER(c.categoryCode) LIKE LOWER(CONCAT('%', :code, '%'))) " +
           "AND (:parentId IS NULL OR c.parent.id = :parentId) " +
           "AND (:enabled IS NULL OR ((:enabled = true AND c.status = 'ACTIVE') OR (:enabled = false AND c.status != 'ACTIVE')))")
    Page<Category> findCategoriesByConditions(@Param("name") String name,
                                            @Param("code") String code,
                                            @Param("parentId") Long parentId,
                                            @Param("enabled") Boolean enabled,
                                            Pageable pageable);
}
