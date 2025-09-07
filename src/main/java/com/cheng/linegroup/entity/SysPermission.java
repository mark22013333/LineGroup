package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系統權限實體類
 *
 * @author cheng
 * @since 2025/5/15 00:41
 */
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_actionKey", columnNames = {"actionKey"})
})
public class SysPermission extends BaseEntity {

    @Column(columnDefinition = "varchar(50) comment '模組名稱'")
    private String module;

    @Column(columnDefinition = "varchar(50) comment '功能名稱'")
    private String feature;

    @Column(columnDefinition = "varchar(100) comment '權限鍵值，對應按鈕的唯一代碼'")
    private String actionKey;

    @Column(columnDefinition = "varchar(200) comment '描述說明'")
    private String description;

    @Column(columnDefinition = "int comment '排序'")
    private Integer sort;
}
