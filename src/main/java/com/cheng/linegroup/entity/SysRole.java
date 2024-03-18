package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cheng
 * @since 2024/3/10 11:08
 **/
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_nameAndCode", columnNames = {"name", "code"})
})
public class SysRole extends BaseEntity {

    @Column(columnDefinition = "varchar(50) comment '角色名稱'")
    private String name;

    @Column(columnDefinition = "varchar(30) comment '角色代號'")
    private String code;

    @Column(columnDefinition = "int comment '排序'")
    private Integer sort;

    @Column(columnDefinition = "int comment '狀態(0:停用, 1:啟用)'")
    private Integer status;

    @Column(columnDefinition = "int comment '是否刪除(0:未刪除, 1:已刪除)'")
    private Integer deleted;

    @Column(columnDefinition = "int comment '資料權限(0:所有資料, 1:部門及子部門資料, 2:本部門資料, 3:本人資料)'")
    private Integer dataScope;

    @Column(columnDefinition = "varchar(255) comment '描述'")
    private String description;
}
