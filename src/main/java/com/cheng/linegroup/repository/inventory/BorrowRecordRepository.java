package com.cheng.linegroup.repository.inventory;

import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.entity.inventory.BorrowRecord;
import com.cheng.linegroup.entity.inventory.Item;
import com.cheng.linegroup.enums.inventory.BorrowStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 借還記錄資料存取介面
 */
@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    /**
     * 根據借還單號查詢
     */
    Optional<BorrowRecord> findByRecordNumber(String recordNumber);

    /**
     * 根據借用人查詢借還記錄
     */
    Page<BorrowRecord> findByBorrowerOrderByBorrowDateDesc(User borrower, Pageable pageable);

    /**
     * 根據物品查詢借還記錄
     */
    Page<BorrowRecord> findByItemOrderByBorrowDateDesc(Item item, Pageable pageable);

    /**
     * 根據狀態查詢借還記錄
     */
    Page<BorrowRecord> findByStatusOrderByBorrowDateDesc(BorrowStatusEnum status, Pageable pageable);

    /**
     * 查詢使用者當前借用的物品
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.borrower = :borrower AND br.status IN ('BORROWED', 'OVERDUE', 'PARTIAL_RETURNED')")
    List<BorrowRecord> findCurrentBorrowsByUser(@Param("borrower") User borrower);

    /**
     * 查詢物品當前的借用記錄
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.item = :item AND br.status IN ('BORROWED', 'OVERDUE', 'PARTIAL_RETURNED')")
    List<BorrowRecord> findCurrentBorrowsByItem(@Param("item") Item item);

    /**
     * 查詢逾期記錄
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status IN ('BORROWED', 'OVERDUE', 'PARTIAL_RETURNED') " +
           "AND br.expectedReturnDate < :currentDate")
    List<BorrowRecord> findOverdueRecords(@Param("currentDate") LocalDate currentDate);

    /**
     * 查詢即將到期的記錄
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status IN ('BORROWED', 'PARTIAL_RETURNED') " +
           "AND br.expectedReturnDate BETWEEN :startDate AND :endDate")
    List<BorrowRecord> findRecordsDueSoon(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根據日期範圍查詢借還記錄
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.borrowDate BETWEEN :startDate AND :endDate")
    Page<BorrowRecord> findByBorrowDateBetween(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate, 
                                             Pageable pageable);

    /**
     * 統計使用者借用次數
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.borrower = :borrower")
    long countByBorrower(@Param("borrower") User borrower);

    /**
     * 統計物品被借用次數
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.item = :item")
    long countByItem(@Param("item") Item item);

    /**
     * 查詢最活躍的借用者
     */
    @Query("SELECT br.borrower, COUNT(br) as borrowCount FROM BorrowRecord br " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY br.borrower ORDER BY borrowCount DESC")
    List<Object[]> findMostActiveBorrowers(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate, 
                                         Pageable pageable);

    /**
     * 查詢最熱門的借用物品
     */
    @Query("SELECT br.item, COUNT(br) as borrowCount FROM BorrowRecord br " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY br.item ORDER BY borrowCount DESC")
    List<Object[]> findMostBorrowedItems(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate, 
                                       Pageable pageable);

    /**
     * 統計借還狀況
     */
    @Query("SELECT br.status, COUNT(br) FROM BorrowRecord br " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY br.status")
    List<Object[]> getBorrowStatistics(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * 查詢部門借用統計
     */
    @Query("SELECT u.deptId, COUNT(br) as borrowCount FROM BorrowRecord br " +
           "INNER JOIN br.borrower u " +
           "WHERE br.borrowDate BETWEEN :startDate AND :endDate " +
           "GROUP BY u.deptId ORDER BY borrowCount DESC")
    List<Object[]> getDepartmentBorrowStatistics(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 複合條件查詢借還記錄
     */
    @Query("SELECT br FROM BorrowRecord br WHERE " +
           "(:borrowerId IS NULL OR br.borrower.id = :borrowerId) " +
           "AND (:itemId IS NULL OR br.item.id = :itemId) " +
           "AND (:status IS NULL OR br.status = :status) " +
           "AND (:startDate IS NULL OR br.borrowDate >= :startDate) " +
           "AND (:endDate IS NULL OR br.borrowDate <= :endDate) " +
           "ORDER BY br.borrowDate DESC")
    Page<BorrowRecord> findRecordsByConditions(@Param("borrowerId") Long borrowerId,
                                             @Param("itemId") Long itemId,
                                             @Param("status") BorrowStatusEnum status,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);

    /**
     * 查詢使用者是否有逾期記錄
     */
    @Query("SELECT COUNT(br) > 0 FROM BorrowRecord br WHERE br.borrower = :borrower " +
           "AND br.status IN ('OVERDUE') AND br.actualReturnDate IS NULL")
    boolean hasOverdueRecords(@Param("borrower") User borrower);

    /**
     * 查詢使用者當前借用數量
     */
    @Query("SELECT COALESCE(SUM(br.quantity - COALESCE(br.returnQuantity, 0)), 0) FROM BorrowRecord br " +
           "WHERE br.borrower = :borrower AND br.status IN ('BORROWED', 'OVERDUE', 'PARTIAL_RETURNED')")
    int getCurrentBorrowQuantityByUser(@Param("borrower") User borrower);

    /**
     * 查詢物品當前借出數量
     */
    @Query("SELECT COALESCE(SUM(br.quantity - COALESCE(br.returnQuantity, 0)), 0) FROM BorrowRecord br " +
           "WHERE br.item = :item AND br.status IN ('BORROWED', 'OVERDUE', 'PARTIAL_RETURNED')")
    int getCurrentBorrowQuantityByItem(@Param("item") Item item);

    /**
     * 查詢月度借還統計
     */
    @Query(value = "SELECT DATE_FORMAT(borrow_date, '%Y-%m') as month, " +
                   "COUNT(*) as borrowCount, " +
                   "SUM(CASE WHEN status = 'RETURNED' THEN 1 ELSE 0 END) as returnCount " +
                   "FROM borrow_records " +
                   "WHERE borrow_date BETWEEN :startDate AND :endDate " +
                   "GROUP BY DATE_FORMAT(borrow_date, '%Y-%m') " +
                   "ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlyBorrowStatistics(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 查詢需要提醒歸還的記錄
     */
    @Query("SELECT br FROM BorrowRecord br WHERE br.status IN ('BORROWED', 'PARTIAL_RETURNED') " +
           "AND br.expectedReturnDate = :reminderDate")
    List<BorrowRecord> findRecordsNeedReminder(@Param("reminderDate") LocalDate reminderDate);

    /**
     * 統計當前借用記錄數量
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status IN ('BORROWED', 'OVERDUE', 'PARTIAL_RETURNED')")
    long countCurrentBorrows();

    /**
     * 統計今日借用記錄數量
     */
    @Query(value = "SELECT COUNT(*) FROM borrow_records WHERE DATE(borrow_date) = CURDATE()", nativeQuery = true)
    long countTodayBorrows();

    /**
     * 統計今日歸還記錄數量
     */
    @Query(value = "SELECT COUNT(*) FROM borrow_records WHERE DATE(actual_return_date) = CURDATE()", nativeQuery = true)
    long countTodayReturns();

    /**
     * 統計逾期記錄數量
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 'OVERDUE'")
    long countOverdueRecords();

    /**
     * 統計即將到期的記錄數量
     */
    @Query(value = "SELECT COUNT(*) FROM borrow_records WHERE status IN ('BORROWED', 'PARTIAL_RETURNED') " +
           "AND expected_return_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ?1 DAY)", 
           nativeQuery = true)
    long countRecordsDueSoon(int days);

    /**
     * 統計本月借用記錄數量
     */
    @Query(value = "SELECT COUNT(*) FROM borrow_records WHERE YEAR(borrow_date) = YEAR(CURDATE()) " +
           "AND MONTH(borrow_date) = MONTH(CURDATE())", nativeQuery = true)
    long countMonthlyBorrows();

    /**
     * 統計上月借用記錄數量
     */
    @Query(value = "SELECT COUNT(*) FROM borrow_records WHERE " +
           "YEAR(borrow_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) " +
           "AND MONTH(borrow_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))", nativeQuery = true)
    long countLastMonthBorrows();

    /**
     * 根據借用日期範圍統計借用記錄數
     */
    long countByBorrowDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 根據日期範圍統計歸還記錄數
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.actualReturnDate BETWEEN :startDate AND :endDate")
    long countReturnedByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * 統計當前借用中的記錄數
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 'BORROWED'")
    long countCurrentBorrowed();

    /**
     * 統計逾期記錄數
     */
    @Query("SELECT COUNT(br) FROM BorrowRecord br WHERE br.status = 'OVERDUE' OR " +
           "(br.status = 'BORROWED' AND br.expectedReturnDate < CURRENT_DATE)")
    long countOverdue();

    /**
     * 根據記錄編號前綴統計記錄數
     */
    long countByRecordNumberStartingWith(String prefix);
}
