package com.cheng.linegroup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class MessageEventRecord extends BaseEntity {

    @Column(columnDefinition = "VARCHAR(50)")
    private String uid;

    @Column(columnDefinition = "VARCHAR(10)")
    private String messageType;

    @Column(columnDefinition = "VARCHAR(50)")
    private String messageId;

}
