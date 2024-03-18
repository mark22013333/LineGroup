package com.cheng.linegroup.dto.auth;

import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cheng
 * @since 2024/3/11 23:42
 **/
@Slf4j
@Data
public class LoginUser implements UserDetails {

    private String token;
    private Long loginTime;
    private Long expireTime;
    private String ipAddress;
    private String loginLocation;
    private String browser;
    private String os;
    private SysUser user;

    public LoginUser(SysUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SysRole> roleIds = user.getRoles();
        Set<SimpleGrantedAuthority> authorities;
        if (roleIds != null && !roleIds.isEmpty()) {
            authorities = roleIds.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                    .collect(Collectors.toSet());
        } else {
            authorities = Collections.emptySet();
        }
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 帳號是否未過期，過期無法驗證
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 指定使用者是否解鎖，鎖定的使用者無法進行身份驗證
     */
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 是否已過期的使用者的憑證(密碼)，過期的憑證防止認證
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否可用，停用的使用者不能身份驗證
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
