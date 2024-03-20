package com.cheng.linegroup.dto.request;

import com.cheng.linegroup.dto.BaseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author cheng
 * @since 2024/3/12 22:28
 **/
@Data
@Schema(description = "註冊或登入物件")
@EqualsAndHashCode(callSuper = true)
public class SignUpOrIn extends BaseDto {
    @NotNull(groups = {OnSignIn.class, OnSignUp.class})
    @Schema(description = "帳號", example = "admin")
    private String username;
    @Schema(description = "暱稱")
    private String nickname;
    @Schema(description = "手機號碼")
    private String phone;
    @NotNull(groups = {OnSignIn.class})
    @Schema(description = "密碼", example = "123456")
    private String password;
    @Schema(description = "信箱")
    private String email;
}
