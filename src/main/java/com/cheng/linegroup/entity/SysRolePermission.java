package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系統角色權限關聯實體類
 *
 * @author cheng
 * @since 2025/5/15 00:43
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_roleId_permissionId", columnNames = {"roleId", "permissionId"})
})
public class SysRolePermission extends BaseEntity {

    @Column(columnDefinition = "bigint comment '角色ID'")
    private Long roleId;

    @Column(columnDefinition = "bigint comment '權限ID'")
    private Long permissionId;
}
