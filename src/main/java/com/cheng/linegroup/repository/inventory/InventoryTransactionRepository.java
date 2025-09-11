package com.cheng.linegroup.repository.inventory;

import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.entity.inventory.InventoryTransaction;
import com.cheng.linegroup.entity.inventory.Item;
import com.cheng.linegroup.enums.inventory.ReferenceTypeEnum;
import com.cheng.linegroup.enums.inventory.TransactionTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 庫存異動記錄資料存取介面
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    /**
     * 根據異動單號查詢
     */
    Optional<InventoryTransaction> findByTransactionNumber(String transactionNumber);

    /**
     * 根據物品查詢異動記錄
     */
    Page<InventoryTransaction> findByItemOrderByTransactionDateDesc(Item item, Pageable pageable);

    /**
     * 根據異動類型查詢
     */
    Page<InventoryTransaction> findByTransactionTypeOrderByTransactionDateDesc(TransactionTypeEnum transactionType, Pageable pageable);

    /**
     * 根據參考類型查詢
     */
    Page<InventoryTransaction> findByReferenceTypeOrderByTransactionDateDesc(ReferenceTypeEnum referenceType, Pageable pageable);

    /**
     * 根據參考類型和參考ID查詢
     */
    List<InventoryTransaction> findByReferenceTypeAndReferenceIdOrderByTransactionDateDesc(ReferenceTypeEnum referenceType, Long referenceId);

    /**
     * 根據處理人查詢異動記錄
     */
    Page<InventoryTransaction> findByProcessedByOrderByTransactionDateDesc(User processedBy, Pageable pageable);

    /**
     * 根據日期範圍查詢異動記錄
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.transactionDate BETWEEN :startDate AND :endDate ORDER BY it.transactionDate DESC")
    Page<InventoryTransaction> findByTransactionDateBetween(@Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate, 
                                                          Pageable pageable);

    /**
     * 查詢物品的最近異動記錄
     */
    List<InventoryTransaction> findTop10ByItemOrderByTransactionDateDesc(Item item);

    /**
     * 統計異動類型數量
     */
    @Query("SELECT it.transactionType, COUNT(it) FROM InventoryTransaction it " +
           "WHERE it.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY it.transactionType")
    List<Object[]> getTransactionTypeStatistics(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * 統計異動數量趨勢
     */
    @Query(value = "SELECT DATE_FORMAT(transaction_date, '%Y-%m-%d') as date, " +
                   "transaction_type, " +
                   "COUNT(*) as count, " +
                   "SUM(ABS(quantity)) as totalQuantity " +
                   "FROM inventory_transactions " +
                   "WHERE transaction_date BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE_FORMAT(transaction_date, '%Y-%m-%d'), transaction_type " +
                   "ORDER BY date DESC", nativeQuery = true)
    List<Object[]> getTransactionTrends(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * 查詢最活躍的處理人
     */
    @Query("SELECT it.processedBy, COUNT(it) as transactionCount FROM InventoryTransaction it " +
           "WHERE it.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY it.processedBy ORDER BY transactionCount DESC")
    List<Object[]> getMostActiveProcessors(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate, 
                                         Pageable pageable);

    /**
     * 查詢異動頻率最高的物品
     */
    @Query("SELECT it.item, COUNT(it) as transactionCount FROM InventoryTransaction it " +
           "WHERE it.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY it.item ORDER BY transactionCount DESC")
    List<Object[]> getMostActiveItems(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate, 
                                    Pageable pageable);

    /**
     * 複合條件查詢異動記錄
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE " +
           "(:itemId IS NULL OR it.item.id = :itemId) " +
           "AND (:transactionType IS NULL OR it.transactionType = :transactionType) " +
           "AND (:referenceType IS NULL OR it.referenceType = :referenceType) " +
           "AND (:processedById IS NULL OR it.processedBy.id = :processedById) " +
           "AND (:startDate IS NULL OR it.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR it.transactionDate <= :endDate) " +
           "ORDER BY it.transactionDate DESC")
    Page<InventoryTransaction> findTransactionsByConditions(@Param("itemId") Long itemId,
                                                          @Param("transactionType") TransactionTypeEnum transactionType,
                                                          @Param("referenceType") ReferenceTypeEnum referenceType,
                                                          @Param("processedById") Long processedById,
                                                          @Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate,
                                                          Pageable pageable);

    /**
     * 統計物品的入庫總量
     */
    @Query("SELECT COALESCE(SUM(it.quantity), 0) FROM InventoryTransaction it " +
           "WHERE it.item = :item AND it.transactionType = 'IN'")
    int getTotalInboundQuantity(@Param("item") Item item);

    /**
     * 統計物品的出庫總量
     */
    @Query("SELECT COALESCE(SUM(ABS(it.quantity)), 0) FROM InventoryTransaction it " +
           "WHERE it.item = :item AND it.transactionType = 'OUT'")
    int getTotalOutboundQuantity(@Param("item") Item item);

    /**
     * 查詢需要審核的異動記錄
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE it.approvedBy IS NULL " +
           "AND it.transactionType IN ('ADJUST', 'DAMAGED', 'LOST') " +
           "ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findPendingApprovalTransactions();

    /**
     * 查詢大額異動記錄
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE ABS(it.quantity) >= :threshold " +
           "ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findLargeQuantityTransactions(@Param("threshold") int threshold, Pageable pageable);

    /**
     * 統計月度異動摘要
     */
    @Query(value = "SELECT DATE_FORMAT(transaction_date, '%Y-%m') as month, " +
                   "SUM(CASE WHEN transaction_type = 'IN' THEN quantity ELSE 0 END) as inbound, " +
                   "SUM(CASE WHEN transaction_type = 'OUT' THEN ABS(quantity) ELSE 0 END) as outbound, " +
                   "SUM(CASE WHEN transaction_type = 'ADJUST' THEN quantity ELSE 0 END) as adjustment, " +
                   "COUNT(*) as totalTransactions " +
                   "FROM inventory_transactions " +
                   "WHERE transaction_date BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE_FORMAT(transaction_date, '%Y-%m') " +
                   "ORDER BY month DESC", nativeQuery = true)
    List<Object[]> getMonthlyTransactionSummary(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * 查詢異常異動記錄（數量異常大或其他異常情況）
     */
    @Query("SELECT it FROM InventoryTransaction it WHERE " +
           "(ABS(it.quantity) > :quantityThreshold) OR " +
           "(it.beforeQuantity < 0) OR " +
           "(it.afterQuantity < 0) " +
           "ORDER BY it.transactionDate DESC")
    List<InventoryTransaction> findAbnormalTransactions(@Param("quantityThreshold") int quantityThreshold);

    /**
     * 查詢物品庫存變化歷史
     */
    @Query("SELECT it.transactionDate, it.beforeQuantity, it.afterQuantity, it.quantity, it.transactionType " +
           "FROM InventoryTransaction it WHERE it.item = :item " +
           "ORDER BY it.transactionDate ASC")
    List<Object[]> getItemQuantityHistory(@Param("item") Item item);
}
