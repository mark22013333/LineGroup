package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author cheng
 * @since 2024/2/15 23:18
 **/
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_key_uid_gid", columnList = "keyword, uid, gid", unique = true)
})
public class ReplyKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "varchar(50) comment '設定者'")
    private String uid;

    @Column(columnDefinition = "varchar(50) comment '群組'")
    private String gid;

    @Column(columnDefinition = "varchar(200) comment '關鍵字'")
    private String keyword;

    @Column(columnDefinition = "varchar(200) comment '回覆內容'")
    private String reply;
}
