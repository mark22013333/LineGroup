package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.SysUserRole;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author cheng
 * @since 2024/3/11 23:07
 **/
@Repository
public interface SysUserRoleRepository extends BaseRepository<SysUserRole, Long> {

    List<SysUserRole> findByUserId(Long userId);
}
