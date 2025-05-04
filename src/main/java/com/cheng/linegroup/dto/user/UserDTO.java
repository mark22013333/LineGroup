package com.cheng.linegroup.dto.user;

import com.cheng.linegroup.dto.BaseDto;
import com.cheng.linegroup.entity.SysRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用者資訊 DTO
 * 
 * @author cheng
 * @since 2025/5/3 11:50
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "使用者資訊")
public class UserDTO extends BaseDto {
    
    @Schema(description = "使用者 ID")
    private Long id;
    
    @Schema(description = "使用者名稱")
    private String username;
    
    @Schema(description = "暱稱")
    private String nickname;
    
    @Schema(description = "性別(1:男, 2:女)")
    private Integer gender;
    
    @Schema(description = "電子郵件")
    private String email;
    
    @Schema(description = "手機號碼")
    private String phone;
    
    @Schema(description = "頭像")
    private String avatar;
    
    @Schema(description = "部門ID")
    private Long deptId;
    
    @Schema(description = "狀態(0:停用, 1:啟用)")
    private Integer status;
    
    @Schema(description = "角色清單")
    private List<SysRole> roles;
    
    @Schema(description = "建立時間")
    private LocalDateTime createTime;
    
    @Schema(description = "最後修改時間")
    private LocalDateTime modifyTime;
    
    @Schema(description = "最後登入時間")
    private LocalDateTime lastLoginTime;
}
