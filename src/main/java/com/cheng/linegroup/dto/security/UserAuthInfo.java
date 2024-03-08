package com.cheng.linegroup.dto.security;

import lombok.Data;

import java.util.Set;

/**
 * @author cheng
 * @since 2024/3/7 19:01
 **/
@Data
public class UserAuthInfo {
    private Long userId;
    private String username;
    private String nickname;
    private Long deptId;
    private String password;
    private Integer status;
    private Set<String> roles;
    private Set<String> perms;
    private Integer dataScope;
}
