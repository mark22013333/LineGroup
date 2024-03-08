package com.cheng.linegroup.security.exception;

import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.utils.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author cheng
 * @since 2024/3/3 15:04
 **/
@Component
public class SystemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        int status = response.getStatus();
        if (status == response.SC_NOT_FOUND) {
            ResponseUtils.writeErrMsg(response, ApiResult.RESOURCE_NOT_FOUND);
        } else {
            if (authException instanceof BadCredentialsException) {
                ResponseUtils.writeErrMsg(response, ApiResult.USERNAME_OR_PASSWORD_ERROR);
            } else {
                ResponseUtils.writeErrMsg(response, ApiResult.TOKEN_INVALID);
            }
        }
    }
}
