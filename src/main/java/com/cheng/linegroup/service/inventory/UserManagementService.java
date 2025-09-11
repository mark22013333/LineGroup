package com.cheng.linegroup.service.inventory;

import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.UserDTO;
import com.cheng.linegroup.entity.User;
import com.cheng.linegroup.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 庫存管理系統使用者管理服務層
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final UserRepository userRepository;

    /**
     * 根據ID查詢使用者
     */
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        return convertToDTO(user);
    }

    /**
     * 根據使用者名稱查詢使用者
     */
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));
        return convertToDTO(user);
    }

    /**
     * 分頁查詢使用者
     */
    public PageResponse<UserDTO> getUsers(UserDTO.QueryRequest request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        Page<User> userPage = userRepository.findUsersByConditions(
                request.getUsername(),
                request.getNickname(),
                request.getEmail(),
                request.getDeptId(),
                request.getStatus(),
                pageable
        );

        List<UserDTO> userDTOs = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return PageResponse.of(userDTOs, userPage.getTotalElements(), 
                              request.getPage() - 1, request.getSize());
    }

    /**
     * 取得所有啟用的使用者
     */
    public List<UserDTO> getEnabledUsers() {
        List<User> users = userRepository.findEnabledUsers();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根據部門ID查詢使用者
     */
    public List<UserDTO> getUsersByDepartment(Long deptId) {
        List<User> users = userRepository.findByDeptId(deptId);
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 搜尋使用者
     */
    public List<UserDTO> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getEnabledUsers();
        }
        
        List<User> users = userRepository.searchUsers(keyword.trim());
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 建立使用者
     */
    @Transactional
    public UserDTO createUser(UserDTO.CreateRequest request) {
        log.info("建立使用者，使用者名稱: {}", request.getUsername());

        // 檢查使用者名稱是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("使用者名稱已存在");
        }

        // 檢查電子郵件是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("電子郵件已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .deptId(request.getDeptId())
                .status(1) // 預設啟用
                .deleted(0) // 預設未刪除
                .build();

        user = userRepository.save(user);
        log.info("使用者建立成功，ID: {}", user.getId());
        return convertToDTO(user);
    }

    /**
     * 更新使用者
     */
    @Transactional
    public UserDTO updateUser(Long id, UserDTO.UpdateRequest request) {
        log.info("更新使用者，ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

        // 檢查使用者名稱是否已被其他使用者使用
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("使用者名稱已存在");
            }
            user.setUsername(request.getUsername());
        }

        // 檢查電子郵件是否已被其他使用者使用
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("電子郵件已存在");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getDeptId() != null) {
            user.setDeptId(request.getDeptId());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user = userRepository.save(user);
        log.info("使用者更新成功，ID: {}", user.getId());
        return convertToDTO(user);
    }

    /**
     * 啟用/停用使用者
     */
    @Transactional
    public UserDTO toggleUserStatus(Long id) {
        log.info("切換使用者狀態，ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

        user.setStatus(user.getStatus() == 1 ? 0 : 1);
        user = userRepository.save(user);

        log.info("使用者狀態切換成功，ID: {}, 新狀態: {}", user.getId(), user.getStatus());
        return convertToDTO(user);
    }

    /**
     * 刪除使用者（軟刪除）
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("刪除使用者，ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("使用者不存在"));

        user.setDeleted(1);
        userRepository.save(user);

        log.info("使用者刪除成功，ID: {}", id);
    }

    /**
     * 取得使用者統計資訊
     */
    public UserDTO.Statistics getUserStatistics() {
        long totalUsers = userRepository.count();
        long enabledUsers = userRepository.countEnabledUsers();
        List<Object[]> departmentStats = userRepository.countUsersByDepartment();

        return UserDTO.Statistics.builder()
                .totalUsers(totalUsers)
                .enabledUsers(enabledUsers)
                .disabledUsers(totalUsers - enabledUsers)
                .departmentUserCounts(departmentStats)
                .build();
    }

    /**
     * 轉換為 DTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .deptId(user.getDeptId())
                .department(user.getDepartment())
                .status(user.getStatus())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
