package com.cheng.linegroup.services.impl;

import com.cheng.linegroup.dto.PermissionDTO;
import com.cheng.linegroup.dto.PermissionQueryParams;
import com.cheng.linegroup.dto.RoleDTO;
import com.cheng.linegroup.dto.RoleQueryParams;
import com.cheng.linegroup.entity.SysPermission;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysRolePermission;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.dao.SysRoleRepository;
import com.cheng.linegroup.repository.SysPermissionRepository;
import com.cheng.linegroup.repository.SysRolePermissionRepository;
import com.cheng.linegroup.services.RolePermissionService;
import com.cheng.linegroup.utils.PageResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 角色與權限管理服務實作
 *
 * @author cheng
 * @since 2025/5/15 01:12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;

    @Override
    public PageResponse<RoleDTO> findRolesByPage(RoleQueryParams params) {
        // 建立分頁和排序條件
        Sort sort = Sort.by(
                Sort.Direction.fromString(params.getSortDirection()),
                params.getSortField()
        );
        Pageable pageable = PageRequest.of(params.getPage(), params.getSize(), sort);
        
        // 執行查詢
        Integer deleted = params.getIncludeDeleted() ? null : 0;
        Page<SysRole> rolePage = roleRepository.findByConditions(
                params.getKeyword(),
                params.getStatus(),
                deleted,
                pageable
        );
        
        // 轉換為DTO並返回
        List<RoleDTO> roleDTOs = rolePage.getContent().stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(
                roleDTOs,
                rolePage.getNumber(),
                rolePage.getSize(),
                rolePage.getTotalElements()
        );
    }

    @Override
    public List<RoleDTO> findAllEnabledRoles() {
        List<SysRole> roles = roleRepository.findByStatusAndDeleted(1, 0);
        return roles.stream()
                .map(this::convertToRoleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO findRoleById(Long id) {
        SysRole role = getRoleById(id);
        RoleDTO roleDTO = convertToRoleDTO(role);
        
        // 查詢角色的權限列表
        List<Long> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(id);
        roleDTO.setPermissionIds(permissionIds);
        
        return roleDTO;
    }

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        // 檢查角色代碼是否已存在
        Optional<SysRole> existingRole = roleRepository.findByCode(roleDTO.getCode());
        if (existingRole.isPresent()) {
            throw BizException.error("角色代碼已存在");
        }
        
        // 建立新角色
        SysRole role = new SysRole();
        BeanUtils.copyProperties(roleDTO, role);
        role.setDeleted(0);
        role.setStatus(roleDTO.getStatus() != null ? roleDTO.getStatus() : 1);
        role.setSort(roleDTO.getSort() != null ? roleDTO.getSort() : 99);
        
        SysRole savedRole = roleRepository.save(role);
        
        // 如果有指定權限，則設定權限
        if (roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            assignPermissions(savedRole.getId(), roleDTO.getPermissionIds());
        }
        
        return convertToRoleDTO(savedRole);
    }

    @Override
    @Transactional
    public RoleDTO updateRole(Long id, RoleDTO roleDTO) {
        SysRole role = getRoleById(id);
        
        // 更新基本資訊
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role.setDataScope(roleDTO.getDataScope());
        
        if (roleDTO.getSort() != null) {
            role.setSort(roleDTO.getSort());
        }
        
        if (roleDTO.getStatus() != null) {
            role.setStatus(roleDTO.getStatus());
        }
        
        SysRole updatedRole = roleRepository.save(role);
        
        // 如果有指定權限，則重新設定權限
        if (roleDTO.getPermissionIds() != null) {
            assignPermissions(id, roleDTO.getPermissionIds());
        }
        
        return convertToRoleDTO(updatedRole);
    }

    @Override
    @Transactional
    public RoleDTO updateRoleStatus(Long id, Integer status) {
        SysRole role = getRoleById(id);
        role.setStatus(status);
        SysRole updatedRole = roleRepository.save(role);
        return convertToRoleDTO(updatedRole);
    }

    @Override
    @Transactional
    public RoleDTO updateRoleSort(Long id, Integer sort) {
        SysRole role = getRoleById(id);
        role.setSort(sort);
        SysRole updatedRole = roleRepository.save(role);
        return convertToRoleDTO(updatedRole);
    }

    @Override
    @Transactional
    public boolean deleteRole(Long id) {
        SysRole role = getRoleById(id);
        role.setDeleted(1);
        roleRepository.save(role);
        return true;
    }

    @Override
    @Transactional
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        // 先刪除該角色的所有權限
        rolePermissionRepository.deleteByRoleId(roleId);
        
        // 如果權限列表為空，則直接返回成功
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        
        // 批次插入新的角色權限關聯
        List<SysRolePermission> rolePermissions = permissionIds.stream()
                .map(permissionId -> SysRolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .build())
                .collect(Collectors.toList());
        
        rolePermissionRepository.saveAll(rolePermissions);
        return true;
    }

    @Override
    public List<Long> getRolePermissions(Long roleId) {
        return rolePermissionRepository.findPermissionIdsByRoleId(roleId);
    }

    @Override
    public PageResponse<PermissionDTO> findPermissionsByPage(PermissionQueryParams params) {
        // 建立分頁和排序條件
        Sort sort = Sort.by(
                Sort.Direction.fromString(params.getSortDirection()),
                params.getSortField()
        );
        Pageable pageable = PageRequest.of(params.getPage(), params.getSize(), sort);
        
        // 執行查詢
        Page<SysPermission> permissionPage = permissionRepository.findByConditions(
                params.getModule(),
                params.getFeature(),
                params.getKeyword(),
                pageable
        );
        
        // 轉換為DTO並返回
        List<PermissionDTO> permissionDTOs = permissionPage.getContent().stream()
                .map(this::convertToPermissionDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(
                permissionDTOs,
                permissionPage.getNumber(),
                permissionPage.getSize(),
                permissionPage.getTotalElements()
        );
    }

    @Override
    public PermissionDTO findPermissionById(Long id) {
        SysPermission permission = getPermissionById(id);
        return convertToPermissionDTO(permission);
    }

    @Override
    @Transactional
    public PermissionDTO createPermission(PermissionDTO permissionDTO) {
        // 檢查權限代碼是否已存在
        Optional<SysPermission> existingPermission = permissionRepository.findByActionKey(permissionDTO.getActionKey());
        if (existingPermission.isPresent()) {
            throw BizException.error("權限代碼已存在");
        }
        
        // 建立新權限
        SysPermission permission = new SysPermission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permission.setSort(permissionDTO.getSort() != null ? permissionDTO.getSort() : 99);
        
        SysPermission savedPermission = permissionRepository.save(permission);
        return convertToPermissionDTO(savedPermission);
    }

    @Override
    @Transactional
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO) {
        SysPermission permission = getPermissionById(id);
        
        // 如果更改了actionKey，需要檢查是否與其他權限衝突
        if (!permission.getActionKey().equals(permissionDTO.getActionKey())) {
            Optional<SysPermission> existingPermission = permissionRepository.findByActionKey(permissionDTO.getActionKey());
            if (existingPermission.isPresent() && !existingPermission.get().getId().equals(id)) {
                throw BizException.error("權限代碼已存在");
            }
        }
        
        // 更新權限資訊
        permission.setModule(permissionDTO.getModule());
        permission.setFeature(permissionDTO.getFeature());
        permission.setActionKey(permissionDTO.getActionKey());
        permission.setDescription(permissionDTO.getDescription());
        
        if (permissionDTO.getSort() != null) {
            permission.setSort(permissionDTO.getSort());
        }
        
        SysPermission updatedPermission = permissionRepository.save(permission);
        return convertToPermissionDTO(updatedPermission);
    }

    @Override
    @Transactional
    public boolean deletePermission(Long id) {
        // 先檢查該權限是否有被角色使用
        SysPermission permission = getPermissionById(id);
        
        // 刪除該權限與角色的關聯
        rolePermissionRepository.deleteByPermissionId(id);
        
        // 刪除權限
        permissionRepository.delete(permission);
        return true;
    }

    @Override
    public List<String> getAllModules() {
        return permissionRepository.findAllModules();
    }

    @Override
    public List<PermissionDTO> getPermissionTree() {
        List<SysPermission> allPermissions = permissionRepository.findAll(Sort.by("module").and(Sort.by("sort")));
        return allPermissions.stream()
                .map(this::convertToPermissionDTO)
                .collect(Collectors.toList());
    }
    
    // 轉換為RoleDTO
    private RoleDTO convertToRoleDTO(SysRole role) {
        RoleDTO roleDTO = new RoleDTO();
        BeanUtils.copyProperties(role, roleDTO);
        return roleDTO;
    }
    
    // 轉換為PermissionDTO
    private PermissionDTO convertToPermissionDTO(SysPermission permission) {
        PermissionDTO permissionDTO = new PermissionDTO();
        BeanUtils.copyProperties(permission, permissionDTO);
        return permissionDTO;
    }
    
    // 根據ID取得角色，如果不存在則拋出異常
    private SysRole getRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> BizException.error("找不到ID為" + id + "的角色"));
    }
    
    // 根據ID取得權限，如果不存在則拋出異常
    private SysPermission getPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> BizException.error("找不到ID為" + id + "的權限"));
    }
}
