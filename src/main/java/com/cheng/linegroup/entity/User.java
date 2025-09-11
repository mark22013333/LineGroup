package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 庫存管理系統使用者實體
 * 這是一個簡化的 User Entity，主要用於庫存管理系統
 * 實際上會映射到 SysUser 表
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inventory_user")
public class User extends BaseEntity {

    @Column(name = "username", columnDefinition = "varchar(50) comment '使用者名稱'")
    private String username;

    @Column(name = "nickname", columnDefinition = "varchar(20) comment '暱稱'")
    private String nickname;

    @Column(name = "email", columnDefinition = "varchar(100) comment '電子郵件'")
    private String email;

    @Column(name = "phone", columnDefinition = "varchar(20) comment '手機號碼'")
    private String phone;

    @Column(name = "dept_id", columnDefinition = "bigint(20) comment '部門ID'")
    private Long deptId;

    @Column(name = "status", columnDefinition = "tinyint(1) comment '狀態(0:停用, 1:啟用)'")
    private Integer status;

    @Column(name = "deleted", columnDefinition = "tinyint(1) comment '是否刪除(0:否, 1:是)'")
    private Integer deleted;

    /**
     * 部門名稱 (透過 deptId 關聯查詢)
     */
    @Transient
    private String department;

    /**
     * 檢查使用者是否啟用
     */
    public Boolean getEnabled() {
        return status != null && status == 1 && (deleted == null || deleted == 0);
    }

    /**
     * 取得顯示名稱
     */
    public String getDisplayName() {
        return nickname != null && !nickname.trim().isEmpty() ? nickname : username;
    }

    /**
     * 取得完整姓名（顯示名稱的別名）
     */
    public String getFullName() {
        return getDisplayName();
    }

    /**
     * 取得建立時間（createTime 的別名）
     */
    public java.time.LocalDateTime getCreatedAt() {
        return this.getCreateTime();
    }

    /**
     * 取得更新時間（modifyTime 的別名）
     */
    public java.time.LocalDateTime getUpdatedAt() {
        return this.getModifyTime();
    }
}
