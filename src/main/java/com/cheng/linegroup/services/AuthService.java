package com.cheng.linegroup.services;

import com.cheng.linegroup.common.contants.OAuth2;
import com.cheng.linegroup.dto.auth.Login;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.utils.SecureJwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author cheng
 * @since 2024/3/11 21:17
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecureJwtUtils secureJwtUtils;
    private final HttpServletRequest request;

    public Login login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username.trim(), password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        
        try {
            // 僅產生加密的安全 JWT 令牌
            String secureToken = secureJwtUtils.generateSecureToken(authentication, request);
            log.info("加密JWT令牌產產生功");
            
            Login loginResponse = Login.builder()
                    .tokenType(OAuth2.BEARER.trim())
                    .secureToken(secureToken)  // 僅返回加密令牌
                    .build();
            
            log.info("登入回應產生完成，使用加密JWT");
            return loginResponse;
        } catch (Exception e) {
            log.error("產生加密令牌失敗", e);
            // 加密令牌產生失敗是嚴重錯誤，不提供降級處理
            throw BizException.create(ApiResult.ERROR, "系統無法產生安全令牌，請稍後再試");
        }
    }
    
    public void logout() {
        // 處理登出邏輯，如需要將令牌加入黑名單等操作
        // 該方法可由 AuthController 中的登出端點調用
    }
}
