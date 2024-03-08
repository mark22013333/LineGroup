package com.cheng.linegroup.security.exception;

import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.utils.ResponseUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author cheng
 * @since 2024/3/5 22:48
 **/
@Component
public class SystemAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseUtils.writeErrMsg(response, ApiResult.ACCESS_UNAUTHORIZED);
    }
}
