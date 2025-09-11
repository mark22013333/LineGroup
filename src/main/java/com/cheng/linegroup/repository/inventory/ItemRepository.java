package com.cheng.linegroup.repository.inventory;

import com.cheng.linegroup.entity.inventory.Category;
import com.cheng.linegroup.entity.inventory.Item;
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
 * 物品資料存取介面
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * 根據物品編號查詢
     */
    Optional<Item> findByItemCodeAndStatus(String itemCode, StatusEnum status);

    /**
     * 根據條碼查詢
     */
    Optional<Item> findByBarcodeAndStatus(String barcode, StatusEnum status);

    /**
     * 根據條碼查詢（包含庫存資訊）
     */
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.inventory WHERE i.barcode = :barcode AND i.status = :status")
    Optional<Item> findByBarcodeWithInventory(@Param("barcode") String barcode, @Param("status") StatusEnum status);

    /**
     * 根據分類查詢物品
     */
    Page<Item> findByCategoryAndStatus(Category category, StatusEnum status, Pageable pageable);

    /**
     * 根據分類ID查詢物品
     */
    Page<Item> findByCategoryIdAndStatus(Long categoryId, StatusEnum status, Pageable pageable);

    /**
     * 全文搜尋物品
     */
    @Query("SELECT i FROM Item i WHERE i.status = :status AND " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> searchItems(@Param("keyword") String keyword, @Param("status") StatusEnum status, Pageable pageable);

    /**
     * 根據名稱模糊查詢
     */
    Page<Item> findByNameContainingIgnoreCaseAndStatus(String name, StatusEnum status, Pageable pageable);

    /**
     * 查詢低庫存物品
     */
    @Query("SELECT i FROM Item i INNER JOIN i.inventory inv WHERE i.status = :status AND " +
           "i.minStockLevel > 0 AND inv.availableQuantity <= i.minStockLevel")
    List<Item> findLowStockItems(@Param("status") StatusEnum status);

    /**
     * 查詢缺貨物品
     */
    @Query("SELECT i FROM Item i INNER JOIN i.inventory inv WHERE i.status = :status AND inv.availableQuantity <= 0")
    List<Item> findOutOfStockItems(@Param("status") StatusEnum status);

    /**
     * 查詢庫存過多物品
     */
    @Query("SELECT i FROM Item i INNER JOIN i.inventory inv WHERE i.status = :status AND " +
           "i.maxStockLevel > 0 AND inv.totalQuantity > i.maxStockLevel")
    List<Item> findOverStockItems(@Param("status") StatusEnum status);

    /**
     * 查詢可借用的物品
     */
    @Query("SELECT i FROM Item i INNER JOIN i.inventory inv WHERE i.status = :status AND inv.availableQuantity > 0")
    Page<Item> findAvailableItems(@Param("status") StatusEnum status, Pageable pageable);

    /**
     * 根據品牌查詢物品
     */
    Page<Item> findByBrandContainingIgnoreCaseAndStatus(String brand, StatusEnum status, Pageable pageable);

    /**
     * 根據型號查詢物品
     */
    Page<Item> findByModelContainingIgnoreCaseAndStatus(String model, StatusEnum status, Pageable pageable);

    /**
     * 根據存放位置查詢物品
     */
    Page<Item> findByLocationContainingIgnoreCaseAndStatus(String location, StatusEnum status, Pageable pageable);

    /**
     * 檢查物品編號是否存在（排除指定ID）
     */
    boolean existsByItemCodeAndStatusAndIdNot(String itemCode, StatusEnum status, Long id);

    /**
     * 檢查條碼是否存在（排除指定ID）
     */
    boolean existsByBarcodeAndStatusAndIdNot(String barcode, StatusEnum status, Long id);

    /**
     * 統計物品總數
     */
    long countByStatus(StatusEnum status);

    /**
     * 統計分類下的物品數量
     */
    long countByCategoryAndStatus(Category category, StatusEnum status);

    /**
     * 查詢最近建立的物品
     */
    List<Item> findTop10ByStatusOrderByCreatedAtDesc(StatusEnum status);

    /**
     * 查詢最近更新的物品
     */
    List<Item> findTop10ByStatusOrderByUpdatedAtDesc(StatusEnum status);

    /**
     * 查詢消耗品
     */
    Page<Item> findByIsConsumableAndStatus(Boolean isConsumable, StatusEnum status, Pageable pageable);

    /**
     * 複合條件查詢物品
     */
    @Query("SELECT i FROM Item i WHERE i.status = :status " +
           "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
           "AND (:keyword IS NULL OR :keyword = '' OR " +
           "     LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(i.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(i.model) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "     LOWER(i.itemCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:brand IS NULL OR :brand = '' OR LOWER(i.brand) LIKE LOWER(CONCAT('%', :brand, '%'))) " +
           "AND (:location IS NULL OR :location = '' OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:isConsumable IS NULL OR i.isConsumable = :isConsumable)")
    Page<Item> findItemsByConditions(@Param("status") StatusEnum status,
                                   @Param("categoryId") Long categoryId,
                                   @Param("keyword") String keyword,
                                   @Param("brand") String brand,
                                   @Param("location") String location,
                                   @Param("isConsumable") Boolean isConsumable,
                                   Pageable pageable);

    /**
     * 查詢物品及其庫存資訊
     */
    @Query("SELECT i FROM Item i LEFT JOIN FETCH i.inventory LEFT JOIN FETCH i.category WHERE i.status = :status")
    Page<Item> findItemsWithInventoryAndCategory(@Param("status") StatusEnum status, Pageable pageable);

    /**
     * 查詢需要盤點的物品（超過指定天數未盤點）
     */
    @Query("SELECT i FROM Item i INNER JOIN i.inventory inv WHERE i.status = :status AND " +
           "(inv.lastStockCheckDate IS NULL OR inv.lastStockCheckDate < :checkDate)")
    List<Item> findItemsNeedStockCheck(@Param("status") StatusEnum status, @Param("checkDate") java.time.LocalDate checkDate);

    /**
     * 檢查條碼是否存在
     */
    boolean existsByBarcode(String barcode);

    /**
     * 根據條碼查詢物品
     */
    Optional<Item> findByBarcode(String barcode);

    /**
     * 根據物品編號查詢
     */
    Optional<Item> findByItemCode(String itemCode);

    /**
     * 根據編號查詢物品（別名方法）
     */
    default Optional<Item> findByCode(String code) {
        return findByItemCode(code);
    }

    /**
     * 根據分類查詢物品（包含子分類）
     */
    @Query("SELECT i FROM Item i WHERE i.category = :category OR i.category.parent = :category")
    List<Item> findByCategoryAndSubCategories(@Param("category") Category category);

    /**
     * 根據分類ID查詢啟用的物品
     */
    List<Item> findByCategoryIdAndEnabledTrue(Long categoryId);

    /**
     * 統計啟用的物品數量
     */
    long countByEnabledTrue();

    /**
     * 統計可借用的物品數量
     */
    long countByBorrowableTrue();

    /**
     * 統計低庫存物品數量
     */
    @Query("SELECT COUNT(i) FROM Item i INNER JOIN i.inventory inv WHERE " +
           "i.minStockLevel > 0 AND inv.availableQuantity <= i.minStockLevel")
    long countLowStockItems();

    /**
     * 統計缺貨物品數量
     */
    @Query("SELECT COUNT(i) FROM Item i INNER JOIN i.inventory inv WHERE inv.availableQuantity <= 0")
    long countOutOfStockItems();

    /**
     * 檢查物品編號是否存在
     */
    boolean existsByItemCode(String itemCode);

    /**
     * 查詢可借用且有庫存的物品
     */
    @Query("SELECT i FROM Item i INNER JOIN i.inventory inv WHERE i.borrowable = true AND inv.availableQuantity > 0")
    List<Item> findBorrowableItemsWithStock();
}
