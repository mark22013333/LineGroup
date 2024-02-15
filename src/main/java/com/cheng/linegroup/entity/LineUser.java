package com.cheng.linegroup.entity;

import com.cheng.linegroup.enums.Gender;
import com.cheng.linegroup.enums.converter.GenderConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author cheng
 * @since 2024/2/13 16:49
 **/
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_uid", columnList = "uid", unique = true)
})
public class LineUser extends BaseEntity {

    @Column(columnDefinition = "VARCHAR(50)")
    private String uid;

    @Column(columnDefinition = "VARCHAR(100)")
    private String nickname;

    @Column(columnDefinition = "VARCHAR(300)")
    private String avatar;

    @Convert(converter = GenderConverter.class)
    @Column(columnDefinition = "TINYINT(1)")
    private Gender gender;

    @Column(columnDefinition = "VARCHAR(100)")
    private String country;

    @Column(columnDefinition = "VARCHAR(20)")
    private String phone;

    @Column(columnDefinition = "VARCHAR(100)")
    private String email;

    @Column(columnDefinition = "BIT")
    private boolean isFriend;
}
