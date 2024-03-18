package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.SysUser;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author cheng
 * @since 2024/3/11 22:59
 **/
@Repository
public interface SysUserRepository extends BaseRepository<SysUser, Long> {
    Optional<SysUser> getSysUsersByUsername(String username);
}
