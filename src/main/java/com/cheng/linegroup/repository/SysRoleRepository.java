package com.cheng.linegroup.repository;

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
 * 角色資料存取層
 *
 * @author cheng
 * @since 2025/5/15 00:45
 */
@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    /**
     * 根據角色代碼查詢
     *
     * @param code 角色代碼
     * @return 角色
     */
    Optional<SysRole> findByCode(String code);

    /**
     * 根據條件分頁查詢
     *
     * @param keyword 關鍵字（角色名稱或代碼）
     * @param status  狀態
     * @param deleted 刪除標記
     * @param pageable 分頁參數
     * @return 分頁結果
     */
    @Query("SELECT r FROM SysRole r WHERE " +
            "(:keyword IS NULL OR r.name LIKE %:keyword% OR r.code LIKE %:keyword%) AND " +
            "(:status IS NULL OR r.status = :status) AND " +
            "(:deleted IS NULL OR r.deleted = :deleted) " +
            "ORDER BY r.sort ASC")
    Page<SysRole> findByConditions(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("deleted") Integer deleted,
            Pageable pageable);

    /**
     * 查詢所有可用角色
     *
     * @return 角色列表
     */
    List<SysRole> findByStatusAndDeleted(Integer status, Integer deleted);
}
