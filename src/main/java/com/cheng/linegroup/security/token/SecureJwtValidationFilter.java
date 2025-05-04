package com.cheng.linegroup.security.token;

import com.cheng.linegroup.common.R;
import com.cheng.linegroup.common.contants.RedisPrefix;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.utils.ResponseUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * 安全JWT驗證過濾器
 * 用於驗證加密JWT令牌並檢查設備指紋
 * 
 * @author cheng
 */
@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class SecureJwtValidationFilter extends OncePerRequestFilter {

    private final SecureJwtUtils secureJwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        try {
            if (StringUtils.isNoneEmpty(token)) {
                // 解析和驗證安全令牌（包括設備綁定驗證）
                Map<String, Object> payload = secureJwtUtils.parseAndVerifySecureToken(token, request);
                
                if (payload.isEmpty()) {
                    log.warn("安全令牌驗證失敗");
                    SecurityContextHolder.clearContext();
                    ResponseUtils.writeErrMsg(response, ApiResult.TOKEN_INVALID);
                    return;
                }
                
                // 檢查令牌是否在黑名單中
                String jti = String.valueOf(payload.get("jti"));
                Boolean isTokenBlacklisted = redisTemplate.hasKey(RedisPrefix.BLACKLIST_TOKEN + jti);
                
                if (isTokenBlacklisted) {
                    log.warn("令牌已被列入黑名單");
                    SecurityContextHolder.clearContext();
                    ResponseUtils.writeErrMsg(response, ApiResult.TOKEN_BLOCK);
                    return;
                }
                
                // 設置認證
                Authentication authentication = SecureJwtUtils.getAuthentication(payload);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("安全令牌驗證成功: {}", authentication.getName());
            }
        } catch (Exception ex) {
            log.error("處理令牌時發生錯誤: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}
