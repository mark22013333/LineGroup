package com.cheng.linegroup.entity.inventory;

import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品實體
 * 包含物品的基本資訊、規格、價格等
 */
@Entity
@Table(name = "items", 
    indexes = {
        @Index(name = "idx_item_code", columnList = "item_code", unique = true),
        @Index(name = "idx_barcode", columnList = "barcode", unique = true),
        @Index(name = "idx_item_name", columnList = "name"),
        @Index(name = "idx_category_id", columnList = "category_id"),
        @Index(name = "idx_item_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_code", columnNames = "item_code"),
        @UniqueConstraint(name = "uk_barcode", columnNames = "barcode")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"category", "inventory", "borrowRecords", "inventoryTransactions"})
@ToString(exclude = {"category", "inventory", "borrowRecords", "inventoryTransactions"})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 物品編號（系統內部編號）
     */
    @Column(name = "item_code", nullable = false, unique = true, length = 100)
    private String itemCode;

    /**
     * 條碼/QR碼
     */
    @Column(name = "barcode", unique = true, length = 200)
    private String barcode;

    /**
     * 物品名稱
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * 物品描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 所屬分類
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 品牌
     */
    @Column(name = "brand", length = 100)
    private String brand;

    /**
     * 型號
     */
    @Column(name = "model", length = 100)
    private String model;

    /**
     * 規格說明
     */
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    /**
     * 單位
     */
    @Column(name = "unit", length = 20)
    private String unit = "個";

    /**
     * 採購價格
     */
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    /**
     * 現價
     */
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    /**
     * 供應商
     */
    @Column(name = "supplier", length = 200)
    private String supplier;

    /**
     * 存放位置
     */
    @Column(name = "location", length = 200)
    private String location;

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
     * 購買日期
     */
    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    /**
     * 保固到期日
     */
    @Column(name = "warranty_expiry")
    private LocalDateTime warrantyExpiry;

    /**
     * 是否啟用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 是否可借用
     */
    @Column(name = "borrowable")
    private Boolean borrowable = true;

    /**
     * 最大借用天數
     */
    @Column(name = "max_borrow_days")
    private Integer maxBorrowDays;

    /**
     * 是否為消耗品
     */
    @Column(name = "is_consumable")
    private Boolean isConsumable = false;

    /**
     * 狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status = StatusEnum.ACTIVE;

    /**
     * 物品圖片URL
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 備註
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 庫存資訊（一對一關係）
     */
    @OneToOne(mappedBy = "item", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Inventory inventory;

    /**
     * 借還記錄列表
     */
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    /**
     * 庫存異動記錄列表
     */
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    private List<InventoryTransaction> inventoryTransactions = new ArrayList<>();

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
     * 建立者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * 更新者
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * 取得完整的物品顯示名稱
     * 格式：品牌 型號 - 物品名稱
     */
    public String getDisplayName() {
        StringBuilder displayName = new StringBuilder();
        
        if (brand != null && !brand.trim().isEmpty()) {
            displayName.append(brand);
        }
        
        if (model != null && !model.trim().isEmpty()) {
            if (displayName.length() > 0) {
                displayName.append(" ");
            }
            displayName.append(model);
        }
        
        if (displayName.length() > 0) {
            displayName.append(" - ");
        }
        
        displayName.append(name);
        
        return displayName.toString();
    }

    /**
     * 檢查是否為低庫存
     */
    public boolean isLowStock() {
        if (inventory == null || minStockLevel == null || minStockLevel <= 0) {
            return false;
        }
        return inventory.getAvailableQuantity() <= minStockLevel;
    }

    /**
     * 檢查是否缺貨
     */
    public boolean isOutOfStock() {
        return inventory == null || inventory.getAvailableQuantity() <= 0;
    }

    /**
     * 檢查是否可以借用指定數量
     */
    public boolean canBorrow(int quantity) {
        if (inventory == null || status != StatusEnum.ACTIVE) {
            return false;
        }
        return inventory.getAvailableQuantity() >= quantity;
    }

    /**
     * 取得物品總價值
     */
    public BigDecimal getTotalValue() {
        if (currentPrice == null || inventory == null) {
            return BigDecimal.ZERO;
        }
        return currentPrice.multiply(BigDecimal.valueOf(inventory.getTotalQuantity()));
    }

    /**
     * 取得物品代碼（物品編號的別名）
     */
    public String getCode() {
        return this.itemCode;
    }

    /**
     * 檢查是否有活躍的借用記錄
     */
    public boolean hasActiveBorrowRecords() {
        if (borrowRecords == null || borrowRecords.isEmpty()) {
            return false;
        }
        return borrowRecords.stream()
                .anyMatch(record -> record.getActualReturnDate() == null);
    }

    /**
     * 取得分類路徑
     */
    public String getCategoryPath() {
        return category != null ? category.getFullPath() : "";
    }
}
