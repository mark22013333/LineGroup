package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.BaseResponse;
import com.cheng.linegroup.common.R;
import com.cheng.linegroup.dto.common.PageResponse;
import com.cheng.linegroup.dto.user.UserDTO;
import com.cheng.linegroup.dto.user.UserQueryParams;
import com.cheng.linegroup.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 使用者管理控制器
 *
 * @author cheng
 * @since 2025/5/3 12:10
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/users")
@Tag(name = "UserManagementAPI", description = "使用者管理API")
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(
            summary = "分頁查詢使用者",
            description = "可用關鍵字搜尋、角色和狀態篩選，分頁顯示使用者資訊",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查詢成功",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = PageResponse.class)
                            )
                    )
            }
    )
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public BaseResponse findByPage(
            @Parameter(description = "使用者查詢參數") @Validated UserQueryParams queryParams) {
        PageResponse<UserDTO> pageResponse = userManagementService.findByPage(queryParams);
        return R.success(pageResponse);
    }

    @Operation(
            summary = "根據ID查詢使用者",
            description = "查詢單個使用者的詳細資訊",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "查詢成功",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = UserDTO.class)
                            )
                    )
            }
    )
    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public BaseResponse findById(@PathVariable Long id) {
        UserDTO userDTO = userManagementService.findById(id);
        return R.success(userDTO);
    }
}
