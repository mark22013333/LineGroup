package com.cheng.linegroup.services.security;

import com.cheng.linegroup.dto.auth.LoginUser;
import com.cheng.linegroup.entity.SysUser;
import com.cheng.linegroup.enums.common.Status;
import com.cheng.linegroup.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author cheng
 * @since 2024/3/11 22:57
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserDetailsService implements UserDetailsService {

    private final SysUserService sysUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SysUser> user = sysUserService.selectUserByUserName(username);
        if (user.isEmpty()) {
            log.info("使用者:{} 不存在", username);
            throw new UsernameNotFoundException("使用者：" + username + " 不存在");
        }

        SysUser sysUser = user.get();
        if (Status.ENABLE.getValue().equals(sysUser.getDeleted())) {
            log.info("使用者:{} 已被刪除", username);
            throw BizException.error("帳號：" + username + " 已被刪除");
        }

        if (Status.DISABLE.getValue().equals(sysUser.getStatus())) {
            log.info("使用者:{} 已被停用.", username);
            throw BizException.error("使用者：" + username + " 已被停用");
        }

        return createLoginUser(sysUser);
    }

    public UserDetails createLoginUser(SysUser user) {
        return new LoginUser(user);
    }
}
