package com.cheng.linegroup.filter;

import com.cheng.linegroup.common.contants.JwtClaim;
import com.cheng.linegroup.common.contants.RedisPrefix;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.utils.JwtUtils;
import com.cheng.linegroup.utils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * @author cheng
 * @since 2024/3/7 18:53
 **/
@Slf4j
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        try {
            if (StringUtils.isNoneEmpty(token)) {
                Map<String, Object> payload = JwtUtils.parseToken(token);
                log.info("payload:{}", payload);
                String jti = String.valueOf(payload.get(JwtClaim.JWT_ID));
                Boolean isTokenBlacklisted = redisTemplate.hasKey(RedisPrefix.BLACKLIST_TOKEN + jti);
                if (isTokenBlacklisted) {
                    ResponseUtils.writeErrMsg(response, ApiResult.TOKEN_BLOCK);
                    return;
                }

                Authentication authentication = JwtUtils.getAuthentication(payload);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BizException ex) {
            SecurityContextHolder.clearContext();
            ResponseUtils.writeErrMsg(response, ApiResult.ERROR, ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
