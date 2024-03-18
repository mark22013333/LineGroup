package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.BaseResponse;
import com.cheng.linegroup.common.R;
import com.cheng.linegroup.dto.request.SignUpOrIn;
import com.cheng.linegroup.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * @author cheng
 * @since 2024/3/8 23:03
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;


    @GetMapping("{pwd}")
    public BaseResponse getPw(@PathVariable String pwd) {
        String encode = passwordEncoder.encode(pwd);
        return R.success(encode);
    }

    @PostMapping("login")
    public BaseResponse login(@RequestBody SignUpOrIn signUp) {
        return R.success(authService.login(signUp.getUsername(), signUp.getPassword()));
    }

}
