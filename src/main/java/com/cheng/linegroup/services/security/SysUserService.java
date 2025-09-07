package com.cheng.linegroup.services.security;

import com.cheng.linegroup.dao.SysRoleRepository;
import com.cheng.linegroup.dao.SysUserRepository;
import com.cheng.linegroup.dao.SysUserRoleRepository;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysUser;
import com.cheng.linegroup.entity.SysUserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * @author cheng
 * @since 2024/3/11 22:57
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserRepository sysUserRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    private final SysRoleRepository sysRoleRepository;

    public Optional<SysUser> selectUserByUserName(String userName) {
        Optional<SysUser> userOptional = sysUserRepository.getSysUsersByUsername(userName);
        return userOptional.map(this::handleUser);
    }

    private SysUser handleUser(SysUser sysUser) {
        List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(sysUser.getId());
        if (userRoles != null && !userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
            List<SysRole> sysRoles = sysRoleRepository.findByIdIn(roleIds);
            sysUser.setRoleIds(roleIds.toArray(new Long[0]));
            sysUser.setRoles(sysRoles);
        }
        return sysUser;
    }
}
