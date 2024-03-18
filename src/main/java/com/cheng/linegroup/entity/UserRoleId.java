package com.cheng.linegroup.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author cheng
 * @since 2024/3/10 11:18
 **/
@Data
public class UserRoleId implements Serializable {
    private Long userId;
    private Long roleId;
}
