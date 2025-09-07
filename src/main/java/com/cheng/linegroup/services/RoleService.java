package com.cheng.linegroup.services;

import com.cheng.linegroup.entity.SysRole;

import java.util.List;

/**
 * 角色管理服務介面
 *
 * @author cheng
 * @since 2025/5/3 12:54
 */
public interface RoleService {

    /**
     * 取得所有角色
     *
     * @return 角色列表
     */
    List<SysRole> getAllRoles();

    /**
     * 根據ID取得角色
     *
     * @param id 角色ID
     * @return 角色
     */
    SysRole getRoleById(Long id);

    /**
     * 根據角色代碼取得角色
     *
     * @param code 角色代碼
     * @return 角色
     */
    SysRole getRoleByCode(String code);

    /**
     * 根據多個ID取得角色列表
     *
     * @param ids 角色ID列表
     * @return 角色列表
     */
    List<SysRole> getRolesByIds(List<Long> ids);
}
