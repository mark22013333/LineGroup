package com.cheng.linegroup.entity.inventory;

import com.cheng.linegroup.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 庫存實體
 * 記錄每個物品的庫存狀況
 */
@Entity
@Table(name = "inventory", 
    indexes = {
        @Index(name = "idx_inventory_item_id", columnList = "item_id", unique = true),
        @Index(name = "idx_available_quantity", columnList = "available_quantity"),
        @Index(name = "idx_last_updated", columnList = "last_updated")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_id", columnNames = "item_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"item", "updatedBy"})
@ToString(exclude = {"item", "updatedBy"})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 對應的物品（一對一關係）
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    /**
     * 總庫存數量
     */
    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity = 0;

    /**
     * 可借用數量
     */
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity = 0;

    /**
     * 已借出數量
     */
    @Column(name = "borrowed_quantity", nullable = false)
    private Integer borrowedQuantity = 0;

    /**
     * 預留數量
     */
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;

    /**
     * 損壞數量
     */
    @Column(name = "damaged_quantity", nullable = false)
    private Integer damagedQuantity = 0;

    /**
     * 最後盤點日期
     */
    @Column(name = "last_stock_check_date")
    private LocalDate lastStockCheckDate;

    /**
     * 最後更新時間
     */
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    /**
     * 最後異動時間
     */
    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    /**
     * 最低庫存警戒線
     */
    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;

    /**
     * 最高庫存上限
     */
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    /**
     * 更新者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

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
     * 更新庫存數量
     * 確保數量一致性
     */
    public void updateQuantities(Integer total, Integer borrowed, Integer reserved, Integer damaged) {
        this.totalQuantity = total != null ? total : 0;
        this.borrowedQuantity = borrowed != null ? borrowed : 0;
        this.reservedQuantity = reserved != null ? reserved : 0;
        this.damagedQuantity = damaged != null ? damaged : 0;
        
        // 計算可用數量 = 總數量 - 已借出 - 預留 - 損壞
        this.availableQuantity = this.totalQuantity - this.borrowedQuantity - this.reservedQuantity - this.damagedQuantity;
        
        // 確保可用數量不為負數
        if (this.availableQuantity < 0) {
            this.availableQuantity = 0;
        }
        
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 增加總庫存
     */
    public void addStock(int quantity) {
        this.totalQuantity += quantity;
        this.availableQuantity += quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 減少總庫存
     */
    public void reduceStock(int quantity) {
        this.totalQuantity = Math.max(0, this.totalQuantity - quantity);
        this.availableQuantity = Math.max(0, this.availableQuantity - quantity);
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 借出物品
     */
    public boolean borrowItem(int quantity) {
        if (this.availableQuantity >= quantity) {
            this.availableQuantity -= quantity;
            this.borrowedQuantity += quantity;
            this.lastUpdated = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 歸還物品
     */
    public void returnItem(int quantity) {
        this.borrowedQuantity = Math.max(0, this.borrowedQuantity - quantity);
        this.availableQuantity += quantity;
        
        // 確保總數量一致性
        if (this.availableQuantity + this.borrowedQuantity + this.reservedQuantity + this.damagedQuantity > this.totalQuantity) {
            this.availableQuantity = this.totalQuantity - this.borrowedQuantity - this.reservedQuantity - this.damagedQuantity;
        }
        
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 標記物品損壞
     */
    public void markAsDamaged(int quantity) {
        // 從可用庫存或借出庫存中扣除
        if (this.availableQuantity >= quantity) {
            this.availableQuantity -= quantity;
        } else {
            int remaining = quantity - this.availableQuantity;
            this.availableQuantity = 0;
            this.borrowedQuantity = Math.max(0, this.borrowedQuantity - remaining);
        }
        
        this.damagedQuantity += quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 預留物品
     */
    public boolean reserveItem(int quantity) {
        if (this.availableQuantity >= quantity) {
            this.availableQuantity -= quantity;
            this.reservedQuantity += quantity;
            this.lastUpdated = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * 取消預留
     */
    public void unreserveItem(int quantity) {
        this.reservedQuantity = Math.max(0, this.reservedQuantity - quantity);
        this.availableQuantity += quantity;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 檢查是否為低庫存
     */
    public boolean isLowStock() {
        if (item == null || item.getMinStockLevel() == null || item.getMinStockLevel() <= 0) {
            return false;
        }
        return this.availableQuantity <= item.getMinStockLevel();
    }

    /**
     * 檢查是否缺貨
     */
    public boolean isOutOfStock() {
        return this.availableQuantity <= 0;
    }

    /**
     * 檢查是否超過最大庫存
     */
    public boolean isOverStock() {
        if (item == null || item.getMaxStockLevel() == null || item.getMaxStockLevel() <= 0) {
            return false;
        }
        return this.totalQuantity > item.getMaxStockLevel();
    }

    /**
     * 取得庫存使用率（已使用/總數量）
     */
    public double getUsageRate() {
        if (totalQuantity == 0) {
            return 0.0;
        }
        return (double) (borrowedQuantity + reservedQuantity + damagedQuantity) / totalQuantity;
    }

    /**
     * 取得庫存健康狀態
     */
    public String getHealthStatus() {
        if (isOutOfStock()) {
            return "缺貨";
        } else if (isLowStock()) {
            return "低庫存";
        } else if (isOverStock()) {
            return "庫存過多";
        } else {
            return "正常";
        }
    }

    /**
     * 移除庫存（用於庫存異動）
     */
    public void removeStock(int quantity) {
        this.totalQuantity = Math.max(0, this.totalQuantity - quantity);
        this.availableQuantity = Math.max(0, this.availableQuantity - quantity);
        this.lastUpdated = LocalDateTime.now();
        this.lastTransactionDate = LocalDateTime.now();
    }

    /**
     * 移除庫存（支援 Integer 參數）
     */
    public void removeStock(Integer quantity) {
        if (quantity != null) {
            removeStock(quantity.intValue());
        }
    }

    /**
     * 調整庫存數量
     */
    public void adjustStock(int quantityChange) {
        if (quantityChange > 0) {
            addStock(quantityChange);
        } else if (quantityChange < 0) {
            removeStock(Math.abs(quantityChange));
        }
    }

    /**
     * 調整庫存數量（支援 Integer 參數）
     */
    public void adjustStock(Integer quantityChange) {
        if (quantityChange != null) {
            adjustStock(quantityChange.intValue());
        }
    }

    /**
     * 標記物品遺失
     */
    public void markAsLost(int quantity) {
        // 從可用庫存或借出庫存中扣除
        if (this.availableQuantity >= quantity) {
            this.availableQuantity -= quantity;
        } else {
            int remaining = quantity - this.availableQuantity;
            this.availableQuantity = 0;
            this.borrowedQuantity = Math.max(0, this.borrowedQuantity - remaining);
        }
        
        // 從總庫存中扣除（遺失的物品不再計入庫存）
        this.totalQuantity = Math.max(0, this.totalQuantity - quantity);
        this.lastUpdated = LocalDateTime.now();
        this.lastTransactionDate = LocalDateTime.now();
    }

    /**
     * 取得當前數量（可用數量的別名）
     */
    public Integer getCurrentQuantity() {
        return this.availableQuantity;
    }

    /**
     * 檢查是否有庫存
     */
    public boolean isInStock() {
        return this.availableQuantity > 0;
    }

    /**
     * 驗證庫存數量一致性
     */
    public boolean validateQuantities() {
        int calculatedAvailable = totalQuantity - borrowedQuantity - reservedQuantity - damagedQuantity;
        return calculatedAvailable == availableQuantity && calculatedAvailable >= 0;
    }

    /**
     * 借出物品
     */
    public void borrowItems(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("借出數量必須大於 0");
        }
        if (this.availableQuantity < quantity) {
            throw new IllegalArgumentException("可用庫存不足");
        }
        this.availableQuantity -= quantity;
        this.borrowedQuantity += quantity;
    }

    /**
     * 歸還物品
     */
    public void returnItems(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("歸還數量必須大於 0");
        }
        if (this.borrowedQuantity < quantity) {
            throw new IllegalArgumentException("借出數量不足");
        }
        this.borrowedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    /**
     * 歸還損壞物品
     */
    public void returnDamagedItems(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("損壞數量必須大於 0");
        }
        if (this.borrowedQuantity < quantity) {
            throw new IllegalArgumentException("借出數量不足");
        }
        this.borrowedQuantity -= quantity;
        this.damagedQuantity += quantity;
    }
}
