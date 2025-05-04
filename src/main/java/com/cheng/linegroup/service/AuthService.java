package com.cheng.linegroup.service;

import com.cheng.linegroup.common.contants.OAuth2;
import com.cheng.linegroup.dto.auth.Login;
import com.cheng.linegroup.security.token.SecureJwtUtils;
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
        
        // 生成標準 JWT 令牌（向後兼容）
        String accessToken = secureJwtUtils.generateStandardJwt(authentication, request);
        log.info("標準JWT令牌生成: {}", accessToken);
        
        try {
            // 生成加密的安全 JWT 令牌
            String secureToken = secureJwtUtils.generateSecureToken(authentication, request);
            log.info("加密JWT令牌生成: {}", secureToken);
            
            Login loginResponse = Login.builder()
                    .tokenType(OAuth2.BEARER.trim())
                    .accessToken(accessToken)
                    .secureToken(secureToken)  // 新增加密令牌
                    .build();
            
            // 記錄完整的登入回應
            log.info("登入回應生成: accessToken={}, tokenType={}, secureToken={}",
                    loginResponse.getAccessToken(),
                    loginResponse.getTokenType(),
                    loginResponse.getSecureToken());
            
            return loginResponse;
        } catch (Exception e) {
            log.error("生成加密令牌失敗", e);
            // 若加密令牌生成失敗，仍返回標準JWT
            return Login.builder()
                    .tokenType(OAuth2.BEARER.trim())
                    .accessToken(accessToken)
                    .build();
        }
    }
    
    public void logout() {
        // 處理登出邏輯，如需要將令牌加入黑名單等操作
        // 該方法可由 AuthController 中的登出端點調用
    }
}
