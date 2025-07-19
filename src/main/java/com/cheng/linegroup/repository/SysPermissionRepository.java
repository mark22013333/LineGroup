package com.cheng.linegroup.repository;

import com.cheng.linegroup.entity.SysPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 權限資料存取層
 *
 * @author cheng
 * @since 2025/5/15 00:48
 */
@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    /**
     * 根據動作鍵查詢
     *
     * @param actionKey 動作鍵
     * @return 權限
     */
    Optional<SysPermission> findByActionKey(String actionKey);

    /**
     * 根據條件分頁查詢
     *
     * @param module   模組
     * @param feature  功能
     * @param keyword  關鍵字
     * @param pageable 分頁參數
     * @return 分頁結果
     */
    @Query("SELECT p FROM SysPermission p WHERE " +
            "(:module IS NULL OR p.module = :module) AND " +
            "(:feature IS NULL OR p.feature = :feature) AND " +
            "(:keyword IS NULL OR p.actionKey LIKE %:keyword% OR p.description LIKE %:keyword%) " +
            "ORDER BY p.module, p.sort ASC")
    Page<SysPermission> findByConditions(
            @Param("module") String module,
            @Param("feature") String feature,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 根據模組查詢權限列表
     *
     * @param module 模組名稱
     * @return 權限列表
     */
    List<SysPermission> findByModuleOrderBySortAsc(String module);

    /**
     * 查詢所有模組名稱（去重）
     *
     * @return 模組名稱列表
     */
    @Query("SELECT DISTINCT p.module FROM SysPermission p ORDER BY p.module")
    List<String> findAllModules();
}
