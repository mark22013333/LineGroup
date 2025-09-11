package com.cheng.linegroup.entity.inventory;

import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 物品分類實體
 * 支援階層式分類結構
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_name", columnList = "name"),
    @Index(name = "idx_category_parent_id", columnList = "parent_id"),
    @Index(name = "idx_category_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"parent", "children", "items"})
@ToString(exclude = {"parent", "children", "items"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分類編號
     */
    @Column(name = "category_code", unique = true, length = 50)
    private String categoryCode;

    /**
     * 分類名稱
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 分類描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 排序順序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 父分類
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * 子分類列表
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> children = new ArrayList<>();

    /**
     * 該分類下的物品列表
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Item> items = new ArrayList<>();

    /**
     * 狀態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusEnum status = StatusEnum.ACTIVE;

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
     * 取得完整的分類路徑
     * 例如：電子設備 > 電腦設備 > 筆記型電腦
     */
    public String getFullPath() {
        StringBuilder path = new StringBuilder();
        buildPath(path);
        return path.toString();
    }

    private void buildPath(StringBuilder path) {
        if (parent != null) {
            parent.buildPath(path);
            path.append(" > ");
        }
        path.append(name);
    }

    /**
     * 檢查是否為根分類
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 檢查是否為葉子分類（沒有子分類）
     */
    public boolean isLeaf() {
        return children.isEmpty();
    }

    /**
     * 取得分類層級（根分類為0）
     */
    public int getLevel() {
        int level = 0;
        Category current = this.parent;
        while (current != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    /**
     * 檢查分類是否啟用
     */
    public Boolean getEnabled() {
        return status == StatusEnum.ACTIVE;
    }

    /**
     * 取得分類代碼（分類編號的別名）
     */
    public String getCode() {
        return this.categoryCode;
    }

    /**
     * 取得排序順序
     */
    public Integer getSortOrder() {
        return this.sortOrder;
    }
}
