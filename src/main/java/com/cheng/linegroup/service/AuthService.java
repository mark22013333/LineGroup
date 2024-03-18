package com.cheng.linegroup.service;

import com.cheng.linegroup.common.contants.OAuth2;
import com.cheng.linegroup.dto.auth.Login;
import com.cheng.linegroup.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

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

    public Login login(String username, String password) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username.trim(), password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        String accessToken = JwtUtils.generateToken(authentication);
        return Login.builder()
                .tokenType(OAuth2.BEARER.trim())
                .accessToken(accessToken)
                .build();
    }

}
