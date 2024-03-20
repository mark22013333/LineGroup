package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.BaseResponse;
import com.cheng.linegroup.common.R;
import com.cheng.linegroup.dto.request.OnSignIn;
import com.cheng.linegroup.dto.request.SignUpOrIn;
import com.cheng.linegroup.service.AuthService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author cheng
 * @since 2024/3/8 23:03
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
@Tag(name = "AuthControllerAPI", description = "認證控制器")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "產生雜湊值")
    @GetMapping("{pwd}")
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    public BaseResponse getPw(@PathVariable String pwd) {
        String encode = passwordEncoder.encode(pwd);
        return R.success(encode);
    }

    @Operation(
            summary = "登入",
            description = "輸入帳號密碼通過認證後取得Token",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "認證成功",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            title = "R 和 SignUpOrIn 合併物件",
                                            description = "回傳物件，R 的屬性 data 為 SignUpOrIn 物件",
                                            anyOf = {R.class, SignUpOrIn.class})
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "認證失敗",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            title = "R 物件",
                                            description = "回傳物件，R 的屬性 data 為空值",
                                            implementation = R.class)
                            )
                    )
            }
    )
    @PostMapping("login")
    public BaseResponse login(@Validated(OnSignIn.class) @RequestBody SignUpOrIn signUp) {
        return R.success(authService.login(signUp.getUsername(), signUp.getPassword()));
    }

}
