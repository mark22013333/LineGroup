package com.cheng.linegroup.service.impl;

import com.cheng.linegroup.dao.SysUserRepository;
import com.cheng.linegroup.dao.SysRoleRepository;
import com.cheng.linegroup.dao.SysUserRoleRepository;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.user.UserDTO;
import com.cheng.linegroup.dto.user.UserQueryParams;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysUser;
import com.cheng.linegroup.entity.SysUserRole;
import com.cheng.linegroup.service.UserManagementService;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 使用者管理服務實作
 * 
 * @author cheng
 * @since 2025/5/3 12:05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final SysUserRepository sysUserRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> findByPage(UserQueryParams queryParams) {
        // 建立分頁對象
        Pageable pageable = PageRequest.of(
                queryParams.getPage(),
                queryParams.getSize(),
                createSort(queryParams)
        );
        
        // 建立查詢條件
        Specification<SysUser> spec = createSpecification(queryParams);
        
        // 執行分頁查詢
        Page<SysUser> page = sysUserRepository.findAll(spec, pageable);
        
        // 為每個使用者載入角色數據
        List<UserDTO> userDTOs = page.getContent().stream()
                .map(user -> {
                    // 查詢使用者角色
                    List<SysUserRole> userRoles = sysUserRoleRepository.findByUserId(user.getId());
                    
                    if (userRoles != null && !userRoles.isEmpty()) {
                        // 取得角色ID列表
                        List<Long> roleIds = userRoles.stream()
                                .map(SysUserRole::getRoleId)
                                .toList();
                        
                        // 查詢角色詳情
                        List<SysRole> roles = sysRoleRepository.findAllById(roleIds);
                        user.setRoles(roles);
                    }
                    
                    // 轉DTO
                    return convertToDto(user);
                })
                .collect(Collectors.toList());
        
        return PageResponse.of(
                userDTOs,
                page.getTotalElements(),
                page.getNumber(),
                page.getSize()
        );
    }

    @Override
    public UserDTO findById(Long id) {
        return sysUserRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public boolean updateLastLoginTime(String username) {
        // 需要修改實現方式，因為舊的 Repository 沒有 updateLastLoginTime 方法
        Optional<SysUser> userOpt = sysUserRepository.getSysUsersByUsername(username);
        if (userOpt.isPresent()) {
            SysUser user = userOpt.get();
            user.setLastLoginTime(LocalDateTime.now());
            sysUserRepository.save(user);
            return true;
        }
        return false;
    }
    
    /**
     * 建立排序條件
     */
    private Sort createSort(UserQueryParams queryParams) {
        String sortField = queryParams.getSortField();
        String sortDirection = queryParams.getSortDirection();
        
        if (!StringUtils.hasText(sortField)) {
            // 預設排序欄位
            sortField = "createTime";
        }
        
        // 設定排序方向
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        
        return Sort.by(direction, sortField);
    }
    
    /**
     * 建立動態查詢條件
     */
    private Specification<SysUser> createSpecification(UserQueryParams queryParams) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 狀態條件
            if (queryParams.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), queryParams.getStatus()));
            }
            
            // 角色 ID 條件 - 使用子查詢而非錯誤的關聯方式
            if (queryParams.getRoleId() != null) {
                // 創建關聯表的子查詢，查詢指定角色ID相關的使用者
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<SysUserRole> userRoleRoot = subquery.from(SysUserRole.class);
                subquery.select(userRoleRoot.get("userId"))
                        .where(cb.equal(userRoleRoot.get("roleId"), queryParams.getRoleId()));
                
                // 主查詢條件：使用者ID在子查詢結果中
                predicates.add(cb.in(root.get("id")).value(subquery));
            }
            
            // 關鍵字搜尋 (使用者名稱、暱稱、電子郵件、手機號碼)
            if (StringUtils.hasText(queryParams.getKeyword())) {
                String keyword = "%" + queryParams.getKeyword() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), keyword),
                        cb.like(root.get("nickname"), keyword),
                        cb.like(root.get("email"), keyword),
                        cb.like(root.get("phone"), keyword)
                ));
            }
            
            // 日期範圍條件
            if (StringUtils.hasText(queryParams.getStartDate())) {
                LocalDateTime startDateTime = LocalDate
                        .parse(queryParams.getStartDate(), DateTimeFormatter.ISO_DATE)
                        .atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), startDateTime));
            }
            
            if (StringUtils.hasText(queryParams.getEndDate())) {
                LocalDateTime endDateTime = LocalDate
                        .parse(queryParams.getEndDate(), DateTimeFormatter.ISO_DATE)
                        .atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), endDateTime));
            }
            
            // 只顯示未刪除的使用者
            predicates.add(cb.equal(root.get("deleted"), 0));
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * 將實體轉換為 DTO
     */
    private UserDTO convertToDto(SysUser user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        
        // 確保角色數據被載入
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles());
        }
        
        return dto;
    }
}
