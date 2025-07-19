package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.BaseResponse;
import com.cheng.linegroup.common.R;
import com.cheng.linegroup.dto.system.SystemSettingDTO;
import com.cheng.linegroup.services.SystemSettingService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系統設置控制器
 *
 * @author cheng
 * @since 2025/6/8 22:30
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/system/settings")
@Tag(name = "SystemSettingAPI", description = "系統設置管理API")
public class SystemSettingController {

    private final SystemSettingService systemSettingService;

    @Operation(
            summary = "取得所有系統設置",
            description = "取得系統中所有可用的設置列表",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功取得系統設置列表",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemSettingDTO.class)
                            )
                    )
            }
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse getAllSettings() {
        log.info("API - 取得所有系統設置");
        List<SystemSettingDTO> settings = systemSettingService.getAllSettings();
        return R.success(settings);
    }

    @Operation(
            summary = "根據類別取得系統設置",
            description = "取得指定類別的系統設置列表",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功取得系統設置列表",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemSettingDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse getSettingsByCategory(
            @Parameter(description = "設置類別") @PathVariable String category) {
        log.info("API - 根據類別取得系統設置: {}", category);
        List<SystemSettingDTO> settings = systemSettingService.getSettingsByCategory(category);
        return R.success(settings);
    }

    @Operation(
            summary = "根據ID取得系統設置",
            description = "取得指定ID的系統設置",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功取得系統設置",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemSettingDTO.class)
                            )
                    )
            }
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse getSettingById(
            @Parameter(description = "設置ID") @PathVariable Long id) {
        log.info("API - 根據ID取得系統設置: {}", id);
        SystemSettingDTO setting = systemSettingService.getSettingById(id);
        return R.success(setting);
    }

    @Operation(
            summary = "建立新系統設置",
            description = "建立一個新的系統設置",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功建立系統設置",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemSettingDTO.class)
                            )
                    )
            }
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse createSetting(
            @Parameter(description = "系統設置資料") @RequestBody @Validated SystemSettingDTO settingDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("API - 建立新系統設置: {}", settingDTO.getKey());
        settingDTO.setCreatedBy(userDetails.getUsername());
        settingDTO.setUpdatedBy(userDetails.getUsername());
        SystemSettingDTO createdSetting = systemSettingService.createSetting(settingDTO);
        return R.success(createdSetting);
    }

    @Operation(
            summary = "更新系統設置",
            description = "更新指定ID的系統設置",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功更新系統設置",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemSettingDTO.class)
                            )
                    )
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse updateSetting(
            @Parameter(description = "設置ID") @PathVariable Long id,
            @Parameter(description = "系統設置資料") @RequestBody @Validated SystemSettingDTO settingDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("API - 更新系統設置: {}", id);
        settingDTO.setUpdatedBy(userDetails.getUsername());
        SystemSettingDTO updatedSetting = systemSettingService.updateSetting(id, settingDTO);
        return R.success(updatedSetting);
    }

    @Operation(
            summary = "批量更新系統設置",
            description = "一次更新多個系統設置",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功更新系統設置",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = SystemSettingDTO.class)
                            )
                    )
            }
    )
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse batchUpdateSettings(
            @Parameter(description = "系統設置資料列表") @RequestBody @Validated List<SystemSettingDTO> settingDTOs,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("API - 批量更新系統設置: {} 筆", settingDTOs.size());
        String username = userDetails.getUsername();
        settingDTOs.forEach(dto -> dto.setUpdatedBy(username));
        List<SystemSettingDTO> updatedSettings = systemSettingService.batchUpdateSettings(settingDTOs);
        return R.success(updatedSettings);
    }

    @Operation(
            summary = "刪除系統設置",
            description = "刪除指定ID的系統設置",
            security = @SecurityRequirement(name = HttpHeaders.AUTHORIZATION),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "成功刪除系統設置"
                    )
            }
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BaseResponse deleteSetting(
            @Parameter(description = "設置ID") @PathVariable Long id) {
        log.info("API - 刪除系統設置: {}", id);
        systemSettingService.deleteSetting(id);
        return R.success();
    }
}
