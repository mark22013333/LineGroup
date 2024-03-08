package com.cheng.linegroup.utils;

import com.cheng.linegroup.common.R;
import com.cheng.linegroup.enums.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author cheng
 * @since 2024/3/3 16:09
 **/
@UtilityClass
public class ResponseUtils {

    /**
     * 過濾器中處理異常的回應
     *
     * @param response  HttpServletResponse
     * @param apiResult {@link ApiResult}
     */
    public static void writeErrMsg(HttpServletResponse response, ApiResult apiResult, Object... ars) throws IOException {
        switch (apiResult) {
            case ACCESS_UNAUTHORIZED, TOKEN_INVALID -> response.setStatus(HttpStatus.UNAUTHORIZED.value());
            case TOKEN_ACCESS_FORBIDDEN -> response.setStatus(HttpStatus.FORBIDDEN.value());
            default -> response.setStatus(HttpStatus.BAD_REQUEST.value());
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().print(JacksonUtils.encodeToJson(R.failed(apiResult, ars)));
    }

}
