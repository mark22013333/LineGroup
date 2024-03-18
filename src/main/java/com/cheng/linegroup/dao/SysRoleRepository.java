package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.SysRole;

import java.util.List;

/**
 * @author cheng
 * @since 2024/3/11 23:28
 **/
public interface SysRoleRepository extends BaseRepository<SysRole, Long> {
    List<SysRole> findByIdIn(List<Long> roleIds);
}
