package com.cheng.linegroup.utils;

import com.cheng.linegroup.common.contants.JwtClaim;
import com.cheng.linegroup.common.contants.OAuth2;
import com.cheng.linegroup.dto.security.SysUserDetails;

import java.util.*;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
     * <p>
     * 認證成功後的使用者資訊會被封裝到 Authentication 物件中，然後透過 JwtTokenProvider#createToken(Authentication) 方法建立 Token
     *
     * @param authentication 使用者認證信息
     * @return Token
     */
    public String generateToken(Authentication authentication) {

        SysUserDetails userDetails = (SysUserDetails) authentication.getPrincipal();

        Map<String, Object> payload = new HashMap<>();
        payload.put(JwtClaim.USER_ID, userDetails.getUserId());

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        payload.put(JwtClaim.AUTHORITIES, roles);

        Date now = new Date();
        Date validity = new Date(now.getTime() + expire);
        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setId(UUID.randomUUID().toString().replace("-", ""))
                .setSubject(authentication.getName())
                .setIssuedAt(now)
                .setExpiration(validity)
                .setClaims(payload)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private static Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
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

            Claims claims = getClaims(token);
            return claims.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            log.error("ERR:{}", e.getMessage(), e);
        }

        return Collections.emptyMap();
    }

    public static UsernamePasswordAuthenticationToken getAuthentication(Map<String, Object> payload) {
        SysUserDetails userDetails = new SysUserDetails();
        userDetails.setUserId((Long) payload.get(JwtClaim.USER_ID));
        userDetails.setUsername(String.valueOf(payload.get(JwtClaim.SUBJECT)));

        Set<String> rawAuthorities = ((Set<String>) payload.get(JwtClaim.AUTHORITIES));

        Set<SimpleGrantedAuthority> authorities = rawAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }
}
