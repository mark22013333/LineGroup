package com.cheng.linegroup.entity.inventory;

import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.enums.inventory.BorrowStatusEnum;
import com.cheng.linegroup.enums.inventory.ReturnConditionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 借還記錄實體
 * 記錄物品的借出和歸還資訊
 */
@Entity
@Table(name = "borrow_records", 
    indexes = {
        @Index(name = "idx_record_number", columnList = "record_number", unique = true),
        @Index(name = "idx_item_id", columnList = "item_id"),
        @Index(name = "idx_borrower_id", columnList = "borrower_id"),
        @Index(name = "idx_borrow_date", columnList = "borrow_date"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_expected_return_date", columnList = "expected_return_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_record_number", columnNames = "record_number")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"item", "borrower", "approvedBy", "processedBy", "returnedBy"})
@ToString(exclude = {"item", "borrower", "approvedBy", "processedBy", "returnedBy"})
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 借還單號
     */
    @Column(name = "record_number", nullable = false, unique = true, length = 50)
    private String recordNumber;

    /**
     * 借用物品
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * 借用人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    /**
     * 借用數量
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 借出時間
     */
    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    /**
     * 預期歸還日期
     */
    @Column(name = "expected_return_date")
    private LocalDate expectedReturnDate;

    /**
     * 實際歸還時間
     */
    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    /**
     * 已歸還數量
     */
    @Column(name = "return_quantity")
    private Integer returnQuantity = 0;

    /**
     * 借還狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BorrowStatusEnum status = BorrowStatusEnum.BORROWED;

    /**
     * 借用目的
     */
    @Column(name = "borrow_purpose", columnDefinition = "TEXT")
    private String borrowPurpose;

    /**
     * 借用備註
     */
    @Column(name = "borrow_notes", columnDefinition = "TEXT")
    private String borrowNotes;

    /**
     * 歸還狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "return_condition")
    private ReturnConditionEnum returnCondition;

    /**
     * 損壞描述
     */
    @Column(name = "damage_description", columnDefinition = "TEXT")
    private String damageDescription;

    /**
     * 罰款金額
     */
    @Column(name = "penalty_amount", precision = 10, scale = 2)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    /**
     * 罰款是否已支付
     */
    @Column(name = "penalty_paid")
    private Boolean penaltyPaid = false;

    /**
     * 歸還備註
     */
    @Column(name = "return_notes", columnDefinition = "TEXT")
    private String returnNotes;

    /**
     * 備註
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 審核人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    /**
     * 處理人（借出處理）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    /**
     * 歸還處理人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returned_by")
    private User returnedBy;

    /**
     * 建立時間
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 檢查是否逾期
     */
    public boolean isOverdue() {
        if (expectedReturnDate == null || status == BorrowStatusEnum.RETURNED) {
            return false;
        }
        return LocalDate.now().isAfter(expectedReturnDate);
    }

    /**
     * 取得逾期天數
     */
    public long getOverdueDays() {
        if (!isOverdue()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(expectedReturnDate, LocalDate.now());
    }

    /**
     * 取得借用天數
     */
    public long getBorrowDays() {
        LocalDateTime endDate = actualReturnDate != null ? actualReturnDate : LocalDateTime.now();
        return ChronoUnit.DAYS.between(borrowDate.toLocalDate(), endDate.toLocalDate());
    }

    /**
     * 檢查是否完全歸還
     */
    public boolean isFullyReturned() {
        return returnQuantity != null && returnQuantity.equals(quantity);
    }

    /**
     * 檢查是否部分歸還
     */
    public boolean isPartiallyReturned() {
        return returnQuantity != null && returnQuantity > 0 && returnQuantity < quantity;
    }

    /**
     * 取得未歸還數量
     */
    public int getUnreturnedQuantity() {
        return quantity - (returnQuantity != null ? returnQuantity : 0);
    }

    /**
     * 歸還物品
     */
    public void returnItems(int returnQty, ReturnConditionEnum condition, String damageDesc, User returnHandler) {
        this.returnQuantity = (this.returnQuantity != null ? this.returnQuantity : 0) + returnQty;
        this.returnCondition = condition;
        this.damageDescription = damageDesc;
        this.returnedBy = returnHandler;
        
        if (this.actualReturnDate == null) {
            this.actualReturnDate = LocalDateTime.now();
        }
        
        // 更新狀態
        if (isFullyReturned()) {
            this.status = BorrowStatusEnum.RETURNED;
        } else if (isPartiallyReturned()) {
            this.status = BorrowStatusEnum.PARTIAL_RETURNED;
        }
        
        // 如果逾期，更新狀態
        if (isOverdue() && this.status == BorrowStatusEnum.BORROWED) {
            this.status = BorrowStatusEnum.OVERDUE;
        }
    }

    /**
     * 延期歸還
     */
    public void extendReturnDate(LocalDate newReturnDate, String reason, User processor) {
        this.expectedReturnDate = newReturnDate;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + 
                    "延期至 " + newReturnDate + "，原因：" + reason + 
                    "，處理人：" + processor.getFullName() + 
                    "，處理時間：" + LocalDateTime.now();
    }

    /**
     * 計算罰款
     */
    public void calculatePenalty(BigDecimal dailyPenaltyRate) {
        if (isOverdue() && dailyPenaltyRate != null && dailyPenaltyRate.compareTo(BigDecimal.ZERO) > 0) {
            long overdueDays = getOverdueDays();
            this.penaltyAmount = dailyPenaltyRate.multiply(BigDecimal.valueOf(overdueDays));
        }
    }

    /**
     * 取得借用狀態描述
     */
    public String getStatusDescription() {
        StringBuilder desc = new StringBuilder();
        
        switch (status) {
            case BORROWED:
                desc.append("已借出");
                if (isOverdue()) {
                    desc.append("（逾期 ").append(getOverdueDays()).append(" 天）");
                }
                break;
            case RETURNED:
                desc.append("已歸還");
                break;
            case OVERDUE:
                desc.append("逾期（").append(getOverdueDays()).append(" 天）");
                break;
            case PARTIAL_RETURNED:
                desc.append("部分歸還（").append(returnQuantity).append("/").append(quantity).append("）");
                break;
        }
        
        return desc.toString();
    }

    /**
     * 取得借用人姓名
     */
    public String getBorrowerName() {
        return borrower != null ? borrower.getFullName() : "";
    }

    /**
     * 取得借用人部門
     */
    public String getBorrowerDepartment() {
        return borrower != null ? borrower.getDepartment() : "";
    }

    /**
     * 取得歸還處理人（別名方法）
     */
    public User getReturnProcessedBy() {
        return returnedBy;
    }

    /**
     * 設置歸還處理人（別名方法）
     */
    public void setReturnProcessedBy(User user) {
        this.returnedBy = user;
    }

    /**
     * 取得借用目的（別名方法）
     */
    public String getPurpose() {
        return borrowPurpose;
    }

    /**
     * 產生借用單號
     */
    public static String generateRecordNumber() {
        return "BR-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }
}
