package com.cheng.linegroup.repository;

import com.cheng.linegroup.entity.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色權限關聯資料存取層
 *
 * @author cheng
 * @since 2025/5/15 00:50
 */
@Repository
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {

    /**
     * 根據角色ID查詢權限ID列表
     *
     * @param roleId 角色ID
     * @return 權限ID列表
     */
    @Query("SELECT rp.permissionId FROM SysRolePermission rp WHERE rp.roleId = :roleId")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 刪除角色的所有權限
     *
     * @param roleId 角色ID
     * @return 刪除的行數
     */
    @Modifying
    @Query("DELETE FROM SysRolePermission rp WHERE rp.roleId = :roleId")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根據權限ID刪除關聯
     *
     * @param permissionId 權限ID
     * @return 刪除的行數
     */
    @Modifying
    @Query("DELETE FROM SysRolePermission rp WHERE rp.permissionId = :permissionId")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
