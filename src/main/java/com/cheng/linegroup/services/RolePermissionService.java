package com.cheng.linegroup.services;

import com.cheng.linegroup.dto.PermissionDTO;
import com.cheng.linegroup.dto.PermissionQueryParams;
import com.cheng.linegroup.dto.RoleDTO;
import com.cheng.linegroup.dto.RoleQueryParams;
import com.cheng.linegroup.utils.PageResponse;

import java.util.List;

/**
 * 角色與權限管理服務介面
 *
 * @author cheng
 * @since 2025/5/15 01:08
 */
public interface RolePermissionService {

    /**
     * 分頁查詢角色列表
     *
     * @param params 查詢參數
     * @return 分頁結果
     */
    PageResponse<RoleDTO> findRolesByPage(RoleQueryParams params);

    /**
     * 查詢所有啟用的角色
     *
     * @return 角色列表
     */
    List<RoleDTO> findAllEnabledRoles();

    /**
     * 根據ID查詢角色
     *
     * @param id 角色ID
     * @return 角色詳情
     */
    RoleDTO findRoleById(Long id);

    /**
     * 新增角色
     *
     * @param roleDTO 角色資料
     * @return 新增後的角色
     */
    RoleDTO createRole(RoleDTO roleDTO);

    /**
     * 更新角色
     *
     * @param id 角色ID
     * @param roleDTO 角色資料
     * @return 更新後的角色
     */
    RoleDTO updateRole(Long id, RoleDTO roleDTO);

    /**
     * 更新角色狀態
     *
     * @param id 角色ID
     * @param status 狀態值
     * @return 更新後的角色
     */
    RoleDTO updateRoleStatus(Long id, Integer status);

    /**
     * 更新角色排序
     *
     * @param id 角色ID
     * @param sort 排序值
     * @return 更新後的角色
     */
    RoleDTO updateRoleSort(Long id, Integer sort);

    /**
     * 刪除角色（邏輯刪除）
     *
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);

    /**
     * 為角色設定權限
     *
     * @param roleId 角色ID
     * @param permissionIds 權限ID列表
     * @return 是否成功
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 取得角色的權限ID列表
     *
     * @param roleId 角色ID
     * @return 權限ID列表
     */
    List<Long> getRolePermissions(Long roleId);
    
    /**
     * 分頁查詢權限列表
     *
     * @param params 查詢參數
     * @return 分頁結果
     */
    PageResponse<PermissionDTO> findPermissionsByPage(PermissionQueryParams params);
    
    /**
     * 根據ID查詢權限
     *
     * @param id 權限ID
     * @return 權限詳情
     */
    PermissionDTO findPermissionById(Long id);
    
    /**
     * 新增權限
     *
     * @param permissionDTO 權限資料
     * @return 新增後的權限
     */
    PermissionDTO createPermission(PermissionDTO permissionDTO);
    
    /**
     * 更新權限
     *
     * @param id 權限ID
     * @param permissionDTO 權限資料
     * @return 更新後的權限
     */
    PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO);
    
    /**
     * 刪除權限
     *
     * @param id 權限ID
     * @return 是否成功
     */
    boolean deletePermission(Long id);
    
    /**
     * 取得所有模組名稱
     *
     * @return 模組名稱列表
     */
    List<String> getAllModules();
    
    /**
     * 根據模組名稱取得權限樹
     *
     * @return 模組-功能-權限的樹狀結構
     */
    List<PermissionDTO> getPermissionTree();
}
