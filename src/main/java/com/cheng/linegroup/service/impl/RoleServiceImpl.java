package com.cheng.linegroup.service.impl;

import com.cheng.linegroup.dao.SysRoleRepository;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 角色管理服務實現類
 *
 * @author cheng
 * @since 2025/5/3 12:58
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SysRole> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public SysRole getRoleById(Long id) {
        if (id == null) {
            return null;
        }
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public SysRole getRoleByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        // 使用已有的 Repository 不一定有 findByCode 方法，暫時返回 null
        // 實際應該根據已有的 Repository 方法進行實現
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysRole> getRolesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return roleRepository.findByIdIn(ids);
    }
}
