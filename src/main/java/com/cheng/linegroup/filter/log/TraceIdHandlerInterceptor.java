package com.cheng.linegroup.filter.log;

import com.cheng.linegroup.utils.TraceUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author Cheng
 * @since 2024/7/23 23:48
 **/
public class TraceIdHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        String traceId = request.getHeader(TraceUtils.HEADER_TRACE_ID);
        if (StringUtils.isBlank(traceId)) {
            traceId = TraceUtils.initTrace();
        }
        response.setHeader(TraceUtils.HEADER_TRACE_ID, traceId);
        TraceUtils.initSpan();
        return true;
    }

    @Override
    public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                @NotNull Object handler, Exception ex) {
        TraceUtils.clearTrace();
        TraceUtils.clearSpan();
    }
}

