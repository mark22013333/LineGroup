package com.cheng.linegroup.filter.token;

import com.cheng.linegroup.common.contants.RedisPrefix;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.utils.ResponseUtils;
import com.cheng.linegroup.utils.SecureJwtUtils;
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
        String tokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            if (StringUtils.isNoneEmpty(tokenHeader)) {
                // 取出令牌，移除 Bearer 前綴（如果存在）
                String token = tokenHeader;
                if (tokenHeader.startsWith("Bearer ")) {
                    token = tokenHeader.substring(7);
                    log.debug("移除了Bearer前綴");
                }

                // 檢查令牌長度，避免處理短令牌或空令牌
                if (token.length() < 50) {
                    log.warn("令牌長度不足，可能不是有效的加密JWT: {}", token.length());
                    SecurityContextHolder.clearContext();
                    ResponseUtils.writeErrMsg(response, ApiResult.TOKEN_INVALID);
                    return;
                }

                log.debug("開始驗證加密JWT: {}", token.substring(0, 20) + "...");

                // 解析和驗證安全令牌（包括設備綁定驗證）
                Map<String, Object> payload = secureJwtUtils.parseAndVerifySecureToken(token, request);

                if (payload.isEmpty()) {
                    log.warn("加密JWT驗證失敗");
                    SecurityContextHolder.clearContext();
                    ResponseUtils.writeErrMsg(response, ApiResult.TOKEN_INVALID);
                    return;
                }

                // 檢查令牌是否在黑名單中
                String jti = String.valueOf(payload.get("jti"));
                Boolean isTokenBlacklisted = redisTemplate.hasKey(RedisPrefix.BLACKLIST_TOKEN + jti);

                if (isTokenBlacklisted) {
                    log.warn("令牌已被列入黑名單: {}", jti);
                    SecurityContextHolder.clearContext();
                    ResponseUtils.writeErrMsg(response, ApiResult.TOKEN_BLOCK);
                    return;
                }

                // 輸出載荷資訊，用於除錯
                log.debug("JWT 載荷資訊: 使用者ID={}, 使用者名稱={}, 角色={}", 
                    payload.get("uid"), 
                    payload.get("sub"),
                    payload.get("authorities"));

                // 設置認證
                Authentication authentication = SecureJwtUtils.createAuthenticationFromPayload(payload);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 更詳細的權限診斷日誌
                if (authentication != null) {
                    log.debug("加密JWT驗證成功: 使用者={}, 角色={}, Principal類別={}",
                            authentication.getName(),
                            authentication.getAuthorities(),
                            authentication.getPrincipal().getClass().getName());
                    
                    // 檢查是否具有 ROLE_ADMIN 權限
                    boolean hasAdminRole = authentication.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                    log.debug("使用者 {} {} ROLE_ADMIN 權限", 
                              authentication.getName(), 
                              hasAdminRole ? "具有" : "不具有");
                } else {
                    log.warn("無法從JWT創建認證對象");
                }
            }
        } catch (Exception ex) {
            log.error("處理加密JWT時發生錯誤: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
