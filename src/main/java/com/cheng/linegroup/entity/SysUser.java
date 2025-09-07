package com.cheng.linegroup.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author cheng
 * @since 2024/3/9 17:58
 **/
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "UniqueUsernameAndEmail", columnNames = {"username", "email"})
})
public class SysUser extends BaseEntity {

    @Column(columnDefinition = "varchar(50) comment '使用者名稱'")
    private String username;

    @Column(columnDefinition = "varchar(20) comment '暱稱'")
    private String nickname;

    @Column(columnDefinition = "varchar(200) comment '使用者密碼'")
    private String password;

    @Column(columnDefinition = "tinyint(1) comment '性別(1:男, 2:女)'")
    private Integer gender;

    @Column(columnDefinition = "varchar(100) comment '電子郵件'")
    private String email;

    @Column(columnDefinition = "varchar(20) comment '手機號碼'")
    private String phone;

    @Column(columnDefinition = "varchar(200) comment '頭像'")
    private String avatar;

    @Column(columnDefinition = "bigint(20) comment '部門ID'")
    private Long deptId;

    @Column(columnDefinition = "tinyint(1) comment '狀態(0:停用, 1:啟用)'")
    private Integer status;

    @Column(columnDefinition = "tinyint(1) comment '是否刪除(0:否, 1:是)'")
    private Integer deleted;

    @Column(columnDefinition = "TIMESTAMP comment '最後登入時間'")
    private LocalDateTime lastLoginTime;

    @Transient
    private Long[] roleIds;

    @Transient
    private List<SysRole> roles;

    @JsonIgnore
    @JsonProperty
    public String getPassword() {
        return password;
    }
}
