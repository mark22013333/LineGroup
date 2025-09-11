package com.cheng.linegroup.controller.inventory;

import com.cheng.linegroup.dto.common.ApiResponse;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.inventory.UserDTO;
import com.cheng.linegroup.service.inventory.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 庫存管理系統使用者管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory/users")
@RequiredArgsConstructor
@Tag(name = "庫存管理 - 使用者管理", description = "庫存管理系統使用者管理相關API")
public class UserController {

    private final UserManagementService userManagementService;

    @Operation(summary = "分頁查詢使用者")
    @GetMapping
    @PreAuthorize("hasAuthority('inventory:user:view')")
    public ResponseEntity<ApiResponse<PageResponse<UserDTO>>> getUsers(
            @Parameter(description = "使用者名稱") @RequestParam(required = false) String username,
            @Parameter(description = "暱稱") @RequestParam(required = false) String nickname,
            @Parameter(description = "電子郵件") @RequestParam(required = false) String email,
            @Parameter(description = "部門ID") @RequestParam(required = false) Long deptId,
            @Parameter(description = "狀態") @RequestParam(required = false) Integer status,
            @Parameter(description = "關鍵字搜尋") @RequestParam(required = false) String keyword,
            @Parameter(description = "頁碼") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每頁大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "排序欄位") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {

        UserDTO.QueryRequest request = UserDTO.QueryRequest.builder()
                .username(username)
                .nickname(nickname)
                .email(email)
                .deptId(deptId)
                .status(status)
                .keyword(keyword)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<UserDTO> result = userManagementService.getUsers(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "根據ID查詢使用者")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:user:view')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @Parameter(description = "使用者ID") @PathVariable Long id) {
        UserDTO result = userManagementService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "根據使用者名稱查詢使用者")
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('inventory:user:view')")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(
            @Parameter(description = "使用者名稱") @PathVariable String username) {
        UserDTO result = userManagementService.getUserByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "取得所有啟用的使用者")
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('inventory:user:view')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getEnabledUsers() {
        List<UserDTO> result = userManagementService.getEnabledUsers();
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "根據部門ID查詢使用者")
    @GetMapping("/department/{deptId}")
    @PreAuthorize("hasAuthority('inventory:user:view')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsersByDepartment(
            @Parameter(description = "部門ID") @PathVariable Long deptId) {
        List<UserDTO> result = userManagementService.getUsersByDepartment(deptId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "搜尋使用者")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('inventory:user:view')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(
            @Parameter(description = "搜尋關鍵字") @RequestParam String keyword) {
        List<UserDTO> result = userManagementService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "建立使用者")
    @PostMapping
    @PreAuthorize("hasAuthority('inventory:user:create')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @RequestBody UserDTO.CreateRequest request) {
        log.info("建立使用者請求: {}", request);
        UserDTO result = userManagementService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("使用者建立成功", result));
    }

    @Operation(summary = "更新使用者")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:user:update')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @Parameter(description = "使用者ID") @PathVariable Long id,
            @RequestBody UserDTO.UpdateRequest request) {
        log.info("更新使用者請求，ID: {}, 資料: {}", id, request);
        UserDTO result = userManagementService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("使用者更新成功", result));
    }

    @Operation(summary = "切換使用者狀態")
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('inventory:user:update')")
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserStatus(
            @Parameter(description = "使用者ID") @PathVariable Long id) {
        log.info("切換使用者狀態，ID: {}", id);
        UserDTO result = userManagementService.toggleUserStatus(id);
        return ResponseEntity.ok(ApiResponse.success("使用者狀態切換成功", result));
    }

    @Operation(summary = "刪除使用者")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:user:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "使用者ID") @PathVariable Long id) {
        log.info("刪除使用者，ID: {}", id);
        userManagementService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("使用者刪除成功"));
    }

    @Operation(summary = "取得使用者統計資訊")
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('inventory:user:view')")
    public ResponseEntity<ApiResponse<UserDTO.Statistics>> getUserStatistics() {
        UserDTO.Statistics result = userManagementService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
