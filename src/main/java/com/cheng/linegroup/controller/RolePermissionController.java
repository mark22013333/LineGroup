package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.BaseResponse;
import com.cheng.linegroup.common.R;
import com.cheng.linegroup.dto.PermissionDTO;
import com.cheng.linegroup.dto.PermissionQueryParams;
import com.cheng.linegroup.dto.RoleDTO;
import com.cheng.linegroup.dto.RoleQueryParams;
import com.cheng.linegroup.services.RolePermissionService;
import com.cheng.linegroup.utils.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色與權限管理控制器
 *
 * @author cheng
 * @since 2025/5/15 01:17
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/system")
@Tag(name = "角色與權限管理", description = "角色與權限管理相關接口")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    //----------------------------- 角色管理 -----------------------------//

    @GetMapping("/roles")
    @Operation(summary = "分頁查詢角色")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse findRolesByPage(@Parameter(description = "角色查詢參數") @Validated RoleQueryParams params) {
        PageResponse<RoleDTO> pageResponse = rolePermissionService.findRolesByPage(params);
        return R.success(pageResponse);
    }

    @GetMapping("/roles/all")
    @Operation(summary = "取得所有啟用的角色")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse findAllEnabledRoles() {
        List<RoleDTO> roles = rolePermissionService.findAllEnabledRoles();
        return R.success(roles);
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "根據ID查詢角色")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse findRoleById(@PathVariable Long id) {
        RoleDTO role = rolePermissionService.findRoleById(id);
        return R.success(role);
    }

    @PostMapping("/roles")
    @Operation(summary = "新增角色")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse createRole(@RequestBody @Validated RoleDTO roleDTO) {
        RoleDTO createdRole = rolePermissionService.createRole(roleDTO);
        return R.success(createdRole);
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "更新角色")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse updateRole(@PathVariable Long id, @RequestBody @Validated RoleDTO roleDTO) {
        RoleDTO updatedRole = rolePermissionService.updateRole(id, roleDTO);
        return R.success(updatedRole);
    }

    @PatchMapping("/roles/{id}/status")
    @Operation(summary = "更新角色狀態")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse updateRoleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer status = params.get("status");
        if (status == null) {
            return R.error("狀態值不能為空");
        }
        RoleDTO updatedRole = rolePermissionService.updateRoleStatus(id, status);
        return R.success(updatedRole);
    }

    @PatchMapping("/roles/{id}/sort")
    @Operation(summary = "更新角色排序")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse updateRoleSort(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer sort = params.get("sort");
        if (sort == null) {
            return R.error("排序值不能為空");
        }
        RoleDTO updatedRole = rolePermissionService.updateRoleSort(id, sort);
        return R.success(updatedRole);
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "刪除角色")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse deleteRole(@PathVariable Long id) {
        boolean success = rolePermissionService.deleteRole(id);
        return success ? R.success() : R.error("刪除角色失敗");
    }

    @PostMapping("/roles/{roleId}/permissions")
    @Operation(summary = "分配角色權限")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        boolean success = rolePermissionService.assignPermissions(roleId, permissionIds);
        return success ? R.success() : R.error("分配角色權限失敗");
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "取得角色權限")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse getRolePermissions(@PathVariable Long roleId) {
        List<Long> permissionIds = rolePermissionService.getRolePermissions(roleId);
        return R.success(permissionIds);
    }

    //----------------------------- 權限管理 -----------------------------//

    @GetMapping("/permissions")
    @Operation(summary = "分頁查詢權限")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse findPermissionsByPage(@Parameter(description = "權限查詢參數") @Validated PermissionQueryParams params) {
        PageResponse<PermissionDTO> pageResponse = rolePermissionService.findPermissionsByPage(params);
        return R.success(pageResponse);
    }

    @GetMapping("/permissions/{id}")
    @Operation(summary = "根據ID查詢權限")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse findPermissionById(@PathVariable Long id) {
        PermissionDTO permission = rolePermissionService.findPermissionById(id);
        return R.success(permission);
    }

    @PostMapping("/permissions")
    @Operation(summary = "新增權限")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse createPermission(@RequestBody @Validated PermissionDTO permissionDTO) {
        PermissionDTO createdPermission = rolePermissionService.createPermission(permissionDTO);
        return R.success(createdPermission);
    }

    @PutMapping("/permissions/{id}")
    @Operation(summary = "更新權限")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse updatePermission(@PathVariable Long id, @RequestBody @Validated PermissionDTO permissionDTO) {
        PermissionDTO updatedPermission = rolePermissionService.updatePermission(id, permissionDTO);
        return R.success(updatedPermission);
    }

    @DeleteMapping("/permissions/{id}")
    @Operation(summary = "刪除權限")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse deletePermission(@PathVariable Long id) {
        boolean success = rolePermissionService.deletePermission(id);
        return success ? R.success() : R.error("刪除權限失敗");
    }

    @GetMapping("/permissions/modules")
    @Operation(summary = "取得所有模組名稱")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse getAllModules() {
        List<String> modules = rolePermissionService.getAllModules();
        return R.success(modules);
    }

    @GetMapping("/permissions/tree")
    @Operation(summary = "取得權限樹")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse getPermissionTree() {
        List<PermissionDTO> tree = rolePermissionService.getPermissionTree();
        return R.success(tree);
    }
}
