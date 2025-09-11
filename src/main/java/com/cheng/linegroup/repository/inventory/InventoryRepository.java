package com.cheng.linegroup.repository.inventory;

import com.cheng.linegroup.entity.inventory.Inventory;
import com.cheng.linegroup.entity.inventory.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 庫存資料存取介面
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * 根據物品查詢庫存
     */
    Optional<Inventory> findByItem(Item item);

    /**
     * 根據物品ID查詢庫存
     */
    Optional<Inventory> findByItemId(Long itemId);

    /**
     * 查詢低庫存項目
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND i.minStockLevel > 0 AND inv.availableQuantity <= i.minStockLevel")
    List<Inventory> findLowStockInventories();

    /**
     * 查詢缺貨項目
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND inv.availableQuantity <= 0")
    List<Inventory> findOutOfStockInventories();

    /**
     * 查詢庫存過多項目
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND i.maxStockLevel > 0 AND inv.totalQuantity > i.maxStockLevel")
    List<Inventory> findOverStockInventories();

    /**
     * 查詢有可借庫存的項目
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND inv.availableQuantity > 0")
    Page<Inventory> findAvailableInventories(Pageable pageable);

    /**
     * 根據分類查詢庫存
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND i.category.id = :categoryId")
    Page<Inventory> findInventoriesByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 查詢需要盤點的庫存
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND (inv.lastStockCheckDate IS NULL OR inv.lastStockCheckDate < :checkDate)")
    List<Inventory> findInventoriesNeedStockCheck(@Param("checkDate") LocalDate checkDate);

    /**
     * 統計總庫存價值
     */
    @Query("SELECT COALESCE(SUM(inv.totalQuantity * i.currentPrice), 0) FROM Inventory inv " +
           "INNER JOIN inv.item i WHERE i.status = 'ACTIVE' AND i.currentPrice IS NOT NULL")
    Double calculateTotalInventoryValue();

    /**
     * 統計可用庫存價值
     */
    @Query("SELECT COALESCE(SUM(inv.availableQuantity * i.currentPrice), 0) FROM Inventory inv " +
           "INNER JOIN inv.item i WHERE i.status = 'ACTIVE' AND i.currentPrice IS NOT NULL")
    Double calculateAvailableInventoryValue();

    /**
     * 統計分類庫存價值
     */
    @Query("SELECT COALESCE(SUM(inv.totalQuantity * i.currentPrice), 0) FROM Inventory inv " +
           "INNER JOIN inv.item i WHERE i.status = 'ACTIVE' AND i.category.id = :categoryId AND i.currentPrice IS NOT NULL")
    Double calculateCategoryInventoryValue(@Param("categoryId") Long categoryId);

    /**
     * 查詢庫存統計資訊
     */
    @Query("SELECT " +
           "COUNT(inv) as totalItems, " +
           "SUM(inv.totalQuantity) as totalQuantity, " +
           "SUM(inv.availableQuantity) as availableQuantity, " +
           "SUM(inv.borrowedQuantity) as borrowedQuantity, " +
           "SUM(inv.damagedQuantity) as damagedQuantity " +
           "FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE'")
    Object[] getInventoryStatistics();

    /**
     * 查詢分類庫存統計
     */
    @Query("SELECT " +
           "COUNT(inv) as totalItems, " +
           "SUM(inv.totalQuantity) as totalQuantity, " +
           "SUM(inv.availableQuantity) as availableQuantity, " +
           "SUM(inv.borrowedQuantity) as borrowedQuantity, " +
           "SUM(inv.damagedQuantity) as damagedQuantity " +
           "FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE' AND i.category.id = :categoryId")
    Object[] getCategoryInventoryStatistics(@Param("categoryId") Long categoryId);

    /**
     * 查詢庫存異常項目（數量不一致）
     */
    @Query("SELECT inv FROM Inventory inv WHERE " +
           "inv.totalQuantity != (inv.availableQuantity + inv.borrowedQuantity + inv.reservedQuantity + inv.damagedQuantity)")
    List<Inventory> findInconsistentInventories();

    /**
     * 查詢最近更新的庫存
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE' " +
           "ORDER BY inv.lastUpdated DESC")
    List<Inventory> findRecentlyUpdatedInventories(Pageable pageable);

    /**
     * 查詢庫存周轉率低的項目
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND inv.borrowedQuantity = 0 AND inv.lastUpdated < :date")
    List<Inventory> findSlowMovingInventories(@Param("date") java.time.LocalDateTime date);

    /**
     * 複合條件查詢庫存
     */
    @Query("SELECT inv FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE' " +
           "AND (:categoryId IS NULL OR i.category.id = :categoryId) " +
           "AND (:lowStockOnly = false OR (i.minStockLevel > 0 AND inv.availableQuantity <= i.minStockLevel)) " +
           "AND (:outOfStockOnly = false OR inv.availableQuantity <= 0) " +
           "AND (:availableOnly = false OR inv.availableQuantity > 0)")
    Page<Inventory> findInventoriesByConditions(@Param("categoryId") Long categoryId,
                                              @Param("lowStockOnly") boolean lowStockOnly,
                                              @Param("outOfStockOnly") boolean outOfStockOnly,
                                              @Param("availableOnly") boolean availableOnly,
                                              Pageable pageable);

    /**
     * 統計低庫存項目數量
     */
    @Query("SELECT COUNT(inv) FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND i.minStockLevel > 0 AND inv.availableQuantity <= i.minStockLevel")
    long countLowStockItems();

    /**
     * 統計缺貨項目數量
     */
    @Query("SELECT COUNT(inv) FROM Inventory inv INNER JOIN inv.item i WHERE " +
           "i.status = 'ACTIVE' AND inv.availableQuantity <= 0")
    long countOutOfStockItems();

    /**
     * 統計總可用數量
     */
    @Query("SELECT COALESCE(SUM(inv.availableQuantity), 0) FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE'")
    long getTotalAvailableQuantity();

    /**
     * 統計總借出數量
     */
    @Query("SELECT COALESCE(SUM(inv.borrowedQuantity), 0) FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE'")
    long getTotalBorrowedQuantity();

    /**
     * 統計總損壞數量
     */
    @Query("SELECT COALESCE(SUM(inv.damagedQuantity), 0) FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE'")
    long getTotalDamagedQuantity();

    /**
     * 統計總庫存數量
     */
    @Query("SELECT COALESCE(SUM(inv.totalQuantity), 0) FROM Inventory inv INNER JOIN inv.item i WHERE i.status = 'ACTIVE'")
    long getTotalQuantity();
}
