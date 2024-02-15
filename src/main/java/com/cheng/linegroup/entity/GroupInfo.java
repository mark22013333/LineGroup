package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cheng
 * @since 2024/2/15 22:15
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = {
        @Index(name = "idx_uid_gid", columnList = "uid, gid", unique = true)
})
public class GroupInfo extends BaseEntity {

    @Column(columnDefinition = "VARCHAR(50)")
    private String uid;

    @Column(columnDefinition = "VARCHAR(100)")
    private String userName;

    @ManyToOne
    @JoinColumn(name = "gid", referencedColumnName = "gid")
    private GroupMain groupMain;
}
