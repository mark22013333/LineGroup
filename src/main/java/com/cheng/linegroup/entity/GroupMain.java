package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.*;


/**
 * @author cheng
 * @since 2023/12/3 12:22 AM
 **/
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_gid", columnList = "gid", unique = true)
})
public class GroupMain extends BaseEntity {

    @Column(columnDefinition = "VARCHAR(50)")
    private String gid;

    @Column(columnDefinition = "VARCHAR(200)")
    private String name;

    @Column(columnDefinition = "BIT")
    private boolean isExist;

}
