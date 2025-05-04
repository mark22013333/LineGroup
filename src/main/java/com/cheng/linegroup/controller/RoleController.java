package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.BaseResponse;
import com.cheng.linegroup.common.R;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author cheng
 * @since 2025/5/3 12:25
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/roles")
@Tag(name = "RoleAPI", description = "角色管理API")
public class RoleController {

    private final RoleService roleService;

    @Operation(
            summary = "取得所有角色",
            description = "取得系統中所有可用的角色列表",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功取得角色列表",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SysRole.class)
                            )
                    )
            }
    )
    @GetMapping
    public BaseResponse getAllRoles() {
        List<SysRole> roles = roleService.getAllRoles();
        return R.success(roles);
    }
}
