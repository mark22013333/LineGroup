package com.cheng.linegroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Cheng
 * @since 2024/7/29 00:28
 **/
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageSentRecords extends BaseEntity {

    @Column(columnDefinition = "varchar(500) comment '圖片連結'")
    private String imageLink;

    @Column(columnDefinition = "varchar(50) comment 'Line User Id'")
    private String uid;

    @Column(columnDefinition = "varchar(50) comment '群組'")
    private String groupId;
}
