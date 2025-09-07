package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.SysRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author cheng
 * @since 2024/3/11 23:28
 **/
@Repository("daoSysRoleRepository")
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {
    List<SysRole> findByIdIn(List<Long> roleIds);
    
    /**
     * 根據角色代碼查詢角色
     * 
     * @param code 角色代碼
     * @return 角色（如果存在）
     */
    Optional<SysRole> findByCode(String code);
    
    /**
     * 根據條件查詢角色列表
     * 
     * @param keyword 關鍵字 (用於搜尋名稱和描述)
     * @param status 狀態
     * @param deleted 刪除標記
     * @param pageable 分頁參數
     * @return 分頁角色列表
     */
    @Query("SELECT r FROM SysRole r WHERE " +
           "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.description LIKE %:keyword%) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "r.deleted = :deleted " +
           "ORDER BY r.sort ASC")
    Page<SysRole> findByConditions(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("deleted") Integer deleted,
            Pageable pageable);
    
    /**
     * 查詢啟用且未刪除的角色
     * 
     * @param status 狀態（1表示啟用）
     * @param deleted 刪除標記（0表示未刪除）
     * @return 角色列表
     */
    List<SysRole> findByStatusAndDeleted(Integer status, Integer deleted);
}
