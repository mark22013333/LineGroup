package com.cheng.linegroup.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 庫存管理系統使用者 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String nickname;
    private String displayName;
    private String email;
    private String phone;
    private Long deptId;
    private String department;
    private Integer status;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 查詢請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryRequest {
        private String username;
        private String nickname;
        private String email;
        private Long deptId;
        private Integer status;
        private String keyword;
        
        // 分頁參數
        @Builder.Default
        private Integer page = 1;
        @Builder.Default
        private Integer size = 10;
        @Builder.Default
        private String sortBy = "createdAt";
        @Builder.Default
        private String sortDir = "desc";
    }

    /**
     * 建立請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private String username;
        private String nickname;
        private String email;
        private String phone;
        private Long deptId;
    }

    /**
     * 更新請求 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String username;
        private String nickname;
        private String email;
        private String phone;
        private Long deptId;
        private Integer status;
    }

    /**
     * 統計資訊 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalUsers;
        private Long enabledUsers;
        private Long disabledUsers;
        private List<Object[]> departmentUserCounts;
        private LocalDateTime statisticsTime;
    }

    /**
     * 簡化的使用者資訊 DTO（用於下拉選單等）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleUser {
        private Long id;
        private String username;
        private String displayName;
        private String department;
    }
}
