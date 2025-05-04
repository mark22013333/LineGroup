package com.cheng.linegroup.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.cheng.linegroup.common.contants.JwtClaim;
import com.cheng.linegroup.common.contants.OAuth2;
import com.cheng.linegroup.common.contants.Sign;
import com.cheng.linegroup.dto.auth.LoginUser;
import com.cheng.linegroup.dto.security.SysUserDetails;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * @author cheng
 * @since 2024/3/7 18:56
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static byte[] secret;

    /**
     * Token 過期時間 (單位:秒)
     */
    private static int expire;


    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        JwtUtils.secret = secret.getBytes();
    }

    @Value("${jwt.expire}")
    public void setExpire(int expire) {
        JwtUtils.expire = expire;
    }


    /**
     * 建立Token
     *
     * @param authentication 使用者認證訊息
     * @return Token
     */
    public static String generateToken(Authentication authentication) {

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        Map<String, Object> payload = new HashMap<>();
        payload.put(JwtClaim.USER_ID, loginUser.getUser().getId());

        List<String> roles = loginUser.getAuthorities().stream().distinct()
                .map(GrantedAuthority::getAuthority).toList();
        payload.put(JwtClaim.AUTHORITIES, roles);

        Date now = new Date();
        Date validity = new Date(now.getTime() + expire);
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString().replace("-", ""))
                .withSubject(authentication.getName())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withPayload(payload)
                .sign(Algorithm.HMAC256(secret));
    }

    private static Map<String, Claim> getClaims(String token) {
        return JWT.decode(token).getClaims();
    }

    /**
     * 解析Token
     *
     * @param token JWT Token
     * @return 解析後的參數 {@link Map}
     */
    public static Map<String, Object> parseToken(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                return Collections.emptyMap();
            }

            if (token.startsWith(OAuth2.BEARER)) {
                token = token.replace(OAuth2.BEARER, StringUtils.EMPTY);
            }

            Map<String, Claim> claims = getClaims(token);
            return claims.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            log.error("ERR:{}", e.getMessage(), e);
        }

        return Collections.emptyMap();
    }

    public static UsernamePasswordAuthenticationToken getAuthentication(Map<String, Object> payload) {
        SysUserDetails userDetails = new SysUserDetails();
        long userId = Long.parseLong(String.valueOf(payload.get(JwtClaim.USER_ID)));
        userDetails.setUserId(userId);

        userDetails.setUsername(String.valueOf(payload.get(JwtClaim.SUBJECT)));

        // 取得角色權限
        Object authoritiesObj = payload.get(JwtClaim.AUTHORITIES);
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (authoritiesObj instanceof List) {
            // 如果已經是 List 類型，直接使用
            @SuppressWarnings("unchecked")
            List<String> authList = (List<String>) authoritiesObj;
            authorities = authList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        } else {
            // 如果是字符串類型 (如 JSON 數組字符串)
            String authStr = String.valueOf(authoritiesObj);
            if (authStr.startsWith("[") && authStr.endsWith("]")) {
                // 移除方括號並分割
                authStr = authStr.substring(1, authStr.length() - 1);
                authorities = Arrays.stream(authStr.split(","))
                        .map(String::trim)
                        .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
            } else {
                // 原始逗號分隔方式
                authorities = Arrays.stream(authStr.split(Sign.COMMA))
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
            }
        }

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }
}
