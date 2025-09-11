package com.cheng.linegroup.entity.inventory;

import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.enums.inventory.ReferenceTypeEnum;
import com.cheng.linegroup.enums.inventory.TransactionTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 庫存異動記錄實體
 * 記錄所有庫存變動的詳細資訊
 */
@Entity
@Table(name = "inventory_transactions", 
    indexes = {
        @Index(name = "idx_transaction_number", columnList = "transaction_number", unique = true),
        @Index(name = "idx_item_id", columnList = "item_id"),
        @Index(name = "idx_transaction_type", columnList = "transaction_type"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date"),
        @Index(name = "idx_reference_type_id", columnList = "reference_type, reference_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_transaction_number", columnNames = "transaction_number")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"item", "processedBy", "approvedBy"})
@ToString(exclude = {"item", "processedBy", "approvedBy"})
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 異動單號
     */
    @Column(name = "transaction_number", nullable = false, unique = true, length = 50)
    private String transactionNumber;

    /**
     * 異動物品
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * 異動類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionTypeEnum transactionType;

    /**
     * 異動數量（正數表示增加，負數表示減少）
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * 異動前數量
     */
    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;

    /**
     * 異動後數量
     */
    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    /**
     * 參考類型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceTypeEnum referenceType;

    /**
     * 參考ID（如借還記錄ID）
     */
    @Column(name = "reference_id")
    private Long referenceId;

    /**
     * 參考單號
     */
    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    /**
     * 單價
     */
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * 總價
     */
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * 異動原因
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /**
     * 異動時間
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate = LocalDateTime.now();

    /**
     * 處理人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by", nullable = false)
    private User processedBy;

    /**
     * 審核人
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    /**
     * 審核時間
     */
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    /**
     * 備註
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

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
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 建立庫存入庫異動記錄
     */
    public static InventoryTransaction createInboundTransaction(Item item, int quantity, 
            int beforeQty, ReferenceTypeEnum refType, Long refId, String reason, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNumber(generateTransactionNumber("IN"));
        transaction.setItem(item);
        transaction.setTransactionType(TransactionTypeEnum.IN);
        transaction.setQuantity(quantity);
        transaction.setBeforeQuantity(beforeQty);
        transaction.setAfterQuantity(beforeQty + quantity);
        transaction.setReferenceType(refType);
        transaction.setReferenceId(refId);
        transaction.setReason(reason);
        transaction.setProcessedBy(processor);
        return transaction;
    }

    /**
     * 建立庫存出庫異動記錄
     */
    public static InventoryTransaction createOutboundTransaction(Item item, int quantity, 
            int beforeQty, ReferenceTypeEnum refType, Long refId, String reason, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNumber(generateTransactionNumber("OUT"));
        transaction.setItem(item);
        transaction.setTransactionType(TransactionTypeEnum.OUT);
        transaction.setQuantity(-quantity); // 負數表示減少
        transaction.setBeforeQuantity(beforeQty);
        transaction.setAfterQuantity(beforeQty - quantity);
        transaction.setReferenceType(refType);
        transaction.setReferenceId(refId);
        transaction.setReason(reason);
        transaction.setProcessedBy(processor);
        return transaction;
    }

    /**
     * 建立庫存調整異動記錄
     */
    public static InventoryTransaction createAdjustmentTransaction(Item item, int quantityChange, 
            int beforeQty, String reason, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNumber(generateTransactionNumber("ADJ"));
        transaction.setItem(item);
        transaction.setTransactionType(TransactionTypeEnum.ADJUST);
        transaction.setQuantity(quantityChange);
        transaction.setBeforeQuantity(beforeQty);
        transaction.setAfterQuantity(beforeQty + quantityChange);
        transaction.setReferenceType(ReferenceTypeEnum.ADJUSTMENT);
        transaction.setReason(reason);
        transaction.setProcessedBy(processor);
        return transaction;
    }

    /**
     * 建立損壞異動記錄
     */
    public static InventoryTransaction createDamageTransaction(Item item, int quantity, 
            int beforeQty, Long refId, String reason, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNumber(generateTransactionNumber("DMG"));
        transaction.setItem(item);
        transaction.setTransactionType(TransactionTypeEnum.DAMAGED);
        transaction.setQuantity(-quantity); // 負數表示減少可用庫存
        transaction.setBeforeQuantity(beforeQty);
        transaction.setAfterQuantity(beforeQty - quantity);
        transaction.setReferenceType(ReferenceTypeEnum.DAMAGE);
        transaction.setReferenceId(refId);
        transaction.setReason(reason);
        transaction.setProcessedBy(processor);
        return transaction;
    }

    /**
     * 建立遺失異動記錄
     */
    public static InventoryTransaction createLossTransaction(Item item, int quantity, 
            int beforeQty, Long refId, String reason, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNumber(generateTransactionNumber("LOSS"));
        transaction.setItem(item);
        transaction.setTransactionType(TransactionTypeEnum.LOST);
        transaction.setQuantity(-quantity); // 負數表示減少
        transaction.setBeforeQuantity(beforeQty);
        transaction.setAfterQuantity(beforeQty - quantity);
        transaction.setReferenceType(ReferenceTypeEnum.LOSS);
        transaction.setReferenceId(refId);
        transaction.setReason(reason);
        transaction.setProcessedBy(processor);
        return transaction;
    }

    /**
     * 產生異動單號
     */
    public static String generateTransactionNumber(String prefix) {
        return prefix + "-" + LocalDateTime.now().getYear() + "-" + 
               String.format("%08d", System.currentTimeMillis() % 100000000);
    }

    /**
     * 取得異動描述
     */
    public String getTransactionDescription() {
        StringBuilder desc = new StringBuilder();
        
        desc.append(transactionType.getDisplayName());
        desc.append(" ").append(Math.abs(quantity)).append(" ").append(item.getUnit());
        
        if (referenceType != null) {
            desc.append("（").append(referenceType.getDisplayName()).append("）");
        }
        
        if (reason != null && !reason.trim().isEmpty()) {
            desc.append(" - ").append(reason);
        }
        
        return desc.toString();
    }

    /**
     * 檢查是否為增加庫存的異動
     */
    public boolean isIncrease() {
        return quantity > 0;
    }

    /**
     * 檢查是否為減少庫存的異動
     */
    public boolean isDecrease() {
        return quantity < 0;
    }

    /**
     * 取得異動數量的絕對值
     */
    public int getAbsoluteQuantity() {
        return Math.abs(quantity);
    }

    /**
     * 建立庫存調整異動記錄
     */
    public static InventoryTransaction createAdjustment(Item item, Integer beforeQty, int adjustmentQty, 
            Integer afterQty, String reason, String referenceNumber, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNumber(referenceNumber != null ? referenceNumber : generateTransactionNumber("ADJ"));
        transaction.setItem(item);
        transaction.setTransactionType(adjustmentQty > 0 ? TransactionTypeEnum.IN : TransactionTypeEnum.OUT);
        transaction.setQuantity(adjustmentQty);
        transaction.setBeforeQuantity(beforeQty != null ? beforeQty : 0);
        transaction.setAfterQuantity(afterQty != null ? afterQty : (beforeQty != null ? beforeQty : 0) + adjustmentQty);
        transaction.setReferenceType(ReferenceTypeEnum.ADJUSTMENT);
        transaction.setReason(reason);
        transaction.setProcessedBy(processor);
        return transaction;
    }

    /**
     * 建立借出交易記錄
     */
    public static InventoryTransaction createBorrow(Item item, Integer quantity, int unitPrice, 
                                                   Integer totalPrice, ReferenceTypeEnum referenceType, 
                                                   Long referenceId, String notes, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(item);
        transaction.setTransactionType(TransactionTypeEnum.OUT);
        transaction.setQuantity(-Math.abs(quantity)); // 借出為負數
        transaction.setUnitPrice(BigDecimal.valueOf(unitPrice));
        transaction.setTotalPrice(BigDecimal.valueOf(totalPrice));
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);
        transaction.setProcessedBy(processor);
        return transaction;
    }

    /**
     * 建立歸還交易記錄
     */
    public static InventoryTransaction createReturn(Item item, Integer quantity, int unitPrice, 
                                                   Integer totalPrice, ReferenceTypeEnum referenceType, 
                                                   Long referenceId, String notes, User processor) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(item);
        transaction.setTransactionType(TransactionTypeEnum.IN);
        transaction.setQuantity(Math.abs(quantity)); // 歸還為正數
        transaction.setUnitPrice(BigDecimal.valueOf(unitPrice));
        transaction.setTotalPrice(BigDecimal.valueOf(totalPrice));
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);
        transaction.setProcessedBy(processor);
        return transaction;
    }
}
