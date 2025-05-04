package com.cheng.linegroup.security.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cheng.linegroup.common.contants.JwtClaim;
import com.cheng.linegroup.common.contants.OAuth2;
import com.cheng.linegroup.common.contants.RedisPrefix;
import com.cheng.linegroup.dto.auth.LoginUser;
import com.cheng.linegroup.dto.security.SysUserDetails;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysUser;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 安全增強型JWT工具類
 * 可用JWT加密和設備綁定，防止令牌被盜用
 *
 * @author cheng
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SecureJwtUtils {

    private static byte[] jwtSecret;
    private static byte[] encryptionKey;
    private static int tokenExpire;
    private static int refreshTokenExpire;

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    public void setJwtSecret(String secret) {
        SecureJwtUtils.jwtSecret = secret.getBytes();

        // 從JWT密鑰派生加密密鑰，確保長度為32字節（256位）
        SecureJwtUtils.encryptionKey = DigestUtils.sha256(secret.getBytes());
    }

    @Value("${jwt.expire}")
    public void setTokenExpire(int expire) {
        SecureJwtUtils.tokenExpire = expire;
        // 重整令牌有效期為訪問令牌的10倍
        SecureJwtUtils.refreshTokenExpire = expire * 10;
    }

    /**
     * 產生安全的JWT令牌（加密並綁定設備）
     *
     * @param authentication 使用者認證訊息
     * @param request        HTTP請求，用於取得客戶端訊息
     * @return 加密的JWT令牌
     */
    public String generateSecureToken(Authentication authentication, HttpServletRequest request) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 產生標準JWT
        String jwtToken = generateStandardJwt(authentication, request);

        // 將產生的JWT令牌記錄到Redis，用於服務端驗證
        String tokenId = UUID.randomUUID().toString();
        String deviceFingerprint = createDeviceFingerprint(request);

        TokenInfo tokenInfo = new TokenInfo(
                loginUser.getUser().getId(),
                deviceFingerprint,
                new Date()
        );

        // 存儲令牌訊息到Redis
        redisTemplate.opsForValue().set(
                RedisPrefix.ACTIVE_TOKEN + tokenId,
                tokenInfo,
                tokenExpire + 300, // 比JWT有效期多5分鐘
                TimeUnit.SECONDS
        );

        // 加密JWT令牌
        try {
            return encryptToken(jwtToken + ":" + tokenId);
        } catch (Exception e) {
            log.error("加密令牌失敗: {}", e.getMessage(), e);
            throw BizException.create(ApiResult.ERROR, "產生安全令牌失敗");
        }
    }

    /**
     * 產生重整令牌
     *
     * @param userId   使用者ID
     * @param username 使用者名
     * @return 加密的重整令牌
     */
    public String generateRefreshToken(Long userId, String username) {
        String refreshTokenId = UUID.randomUUID().toString();

        // 存儲重整令牌訊息到Redis
        redisTemplate.opsForValue().set(
                RedisPrefix.REFRESH_TOKEN + refreshTokenId,
                userId + ":" + username,
                refreshTokenExpire,
                TimeUnit.SECONDS
        );

        // 加密重整令牌ID
        try {
            return encryptToken(refreshTokenId);
        } catch (Exception e) {
            log.error("加密重整令牌失敗: {}", e.getMessage(), e);
            throw BizException.create(ApiResult.ERROR, "產生重整令牌失敗");
        }
    }

    /**
     * 使用重整令牌取得新的訪問令牌
     *
     * @param encryptedRefreshToken 加密的重整令牌
     * @param request               HTTP請求
     * @return 包含新訪問令牌和重整令牌的映射
     */
    public Map<String, String> refreshToken(String encryptedRefreshToken, HttpServletRequest request) {
        try {
            // 解密重整令牌
            String refreshTokenId = decryptToken(encryptedRefreshToken);

            // 從Redis取得使用者訊息
            String userInfo = (String) redisTemplate.opsForValue().get(RedisPrefix.REFRESH_TOKEN + refreshTokenId);
            if (StringUtils.isEmpty(userInfo)) {
                throw BizException.create(ApiResult.TOKEN_INVALID, "重整令牌無效或已過期");
            }

            // 解析使用者訊息
            String[] parts = userInfo.split(":");
            Long userId = Long.valueOf(parts[0]);
            String username = parts[1];

            // 刪除舊的重整令牌
            redisTemplate.delete(RedisPrefix.REFRESH_TOKEN + refreshTokenId);

            // 建立臨時認證對象
            LoginUser tempUser = createTempLoginUser(username);
            Authentication tempAuth = new UsernamePasswordAuthenticationToken(tempUser, null, tempUser.getAuthorities());

            // 產生新令牌
            String newAccessToken = generateSecureToken(tempAuth, request);
            String newRefreshToken = generateRefreshToken(userId, username);

            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", newAccessToken);
            tokenMap.put("refreshToken", newRefreshToken);

            return tokenMap;
        } catch (Exception e) {
            log.error("重整令牌失敗: {}", e.getMessage(), e);
            throw BizException.create(ApiResult.ERROR, "重整令牌失敗");
        }
    }

    /**
     * 解析並驗證安全令牌
     *
     * @param encryptedToken 加密的JWT令牌或標準JWT令牌
     * @param request        HTTP請求，用於驗證設備指紋
     * @return JWT載荷
     */
    public Map<String, Object> parseAndVerifySecureToken(String encryptedToken, HttpServletRequest request) {
        try {
            if (StringUtils.isBlank(encryptedToken)) {
                return Collections.emptyMap();
            }

            if (encryptedToken.startsWith(OAuth2.BEARER)) {
                encryptedToken = encryptedToken.replace(OAuth2.BEARER, StringUtils.EMPTY);
            }

            // 嘗試將其視為標準 JWT 令牌
            if (encryptedToken.contains(".")) {
                log.debug("檢測到標準JWT格式，使用標準JWT處理邏輯");
                try {
                    // 使用標準 JWT 解析邏輯
                    Map<String, Object> claims = parseStandardJwt(encryptedToken);
                    if (!claims.isEmpty()) {
                        return claims;
                    }
                } catch (Exception e) {
                    log.debug("標準JWT解析失敗，嘗試以加密JWT處理: {}", e.getMessage());
                    // 解析失敗時繼續下一步處理
                }
            }

            // 嘗試作為加密令牌處理
            try {
                // 解密令牌
                String combinedToken = decryptToken(encryptedToken);

                // 分離JWT和令牌ID
                String[] parts = combinedToken.split(":");
                if (parts.length != 2) {
                    log.warn("令牌格式無效");
                    return Collections.emptyMap();
                }

                String jwt = parts[0];
                String tokenId = parts[1];

                // 從Redis取得令牌訊息
                TokenInfo tokenInfo = (TokenInfo) redisTemplate.opsForValue().get(RedisPrefix.ACTIVE_TOKEN + tokenId);
                if (tokenInfo == null) {
                    log.warn("令牌未在活躍列表中找到: {}", tokenId);
                    return Collections.emptyMap();
                }

                // 驗證設備指紋
                String currentFingerprint = createDeviceFingerprint(request);
                if (!tokenInfo.getDeviceFingerprint().equals(currentFingerprint)) {
                    log.warn("設備指紋不匹配，可能的令牌盜用: {}", tokenId);

                    // 將令牌加入黑名單
                    DecodedJWT decodedJWT = JWT.decode(jwt);
                    String jti = decodedJWT.getId();
                    redisTemplate.opsForValue().set(
                            RedisPrefix.BLACKLIST_TOKEN + jti,
                            "STOLEN",
                            30,
                            TimeUnit.DAYS
                    );

                    // 刪除活躍令牌記錄
                    redisTemplate.delete(RedisPrefix.ACTIVE_TOKEN + tokenId);

                    return Collections.emptyMap();
                }

                // 標準JWT驗證
                Map<String, Object> claims = parseStandardJwt(jwt);
                if (claims.isEmpty()) {
                    return Collections.emptyMap();
                }

                // 更新最後活動時間
                tokenInfo.setLastActivityTime(new Date());
                redisTemplate.opsForValue().set(
                        RedisPrefix.ACTIVE_TOKEN + tokenId,
                        tokenInfo,
                        tokenExpire + 300,
                        TimeUnit.SECONDS
                );

                // 新增令牌ID到載荷
                claims.put("tokenId", tokenId);

                return claims;
            } catch (Exception e) {
                log.error("解析加密令牌失敗: {}", e.getMessage());
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            log.error("解析安全令牌失敗: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 註銷令牌
     *
     * @param encryptedToken 加密的JWT令牌
     */
    public void revokeToken(String encryptedToken) {
        try {
            if (StringUtils.isBlank(encryptedToken)) {
                return;
            }

            if (encryptedToken.startsWith(OAuth2.BEARER)) {
                encryptedToken = encryptedToken.replace(OAuth2.BEARER, StringUtils.EMPTY);
            }

            // 解密令牌
            String combinedToken = decryptToken(encryptedToken);

            // 分離JWT和令牌ID
            String[] parts = combinedToken.split(":");
            if (parts.length != 2) {
                return;
            }

            String jwt = parts[0];
            String tokenId = parts[1];

            // 將JWT加入黑名單
            DecodedJWT decodedJWT = JWT.decode(jwt);
            String jti = decodedJWT.getId();
            Date expiresAt = decodedJWT.getExpiresAt();
            long ttl = Math.max(1, (expiresAt.getTime() - System.currentTimeMillis()) / 1000);

            redisTemplate.opsForValue().set(
                    RedisPrefix.BLACKLIST_TOKEN + jti,
                    "REVOKED",
                    ttl,
                    TimeUnit.SECONDS
            );

            // 刪除活躍令牌記錄
            redisTemplate.delete(RedisPrefix.ACTIVE_TOKEN + tokenId);
        } catch (Exception e) {
            log.error("註銷令牌失敗: {}", e.getMessage(), e);
            throw BizException.create(ApiResult.ERROR, "註銷令牌失敗");
        }
    }

    /**
     * 從令牌載荷建立認證對象
     *
     * @param payload JWT載荷
     * @return 認證對象
     */
    public static UsernamePasswordAuthenticationToken getAuthentication(Map<String, Object> payload) {
        SysUserDetails userDetails = new SysUserDetails();
        long userId = Long.parseLong(String.valueOf(payload.get(JwtClaim.USER_ID)));
        userDetails.setUserId(userId);
        userDetails.setUsername(String.valueOf(payload.get(JwtClaim.SUBJECT)));

        // 取得角色權限
        Object authoritiesObj = payload.get(JwtClaim.AUTHORITIES);
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (authoritiesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> authList = (List<String>) authoritiesObj;
            authorities = authList.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        } else {
            String authStr = String.valueOf(authoritiesObj);
            if (authStr.startsWith("[") && authStr.endsWith("]")) {
                authStr = authStr.substring(1, authStr.length() - 1);
                authorities = Arrays.stream(authStr.split(","))
                        .map(String::trim)
                        .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
            } else {
                authorities = Arrays.stream(authStr.split(","))
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
            }
        }

        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    // ----- 私有工具方法 -----

    /**
     * 產生標準JWT令牌
     */
    public String generateStandardJwt(Authentication authentication, HttpServletRequest request) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        Map<String, Object> payload = new HashMap<>();
        payload.put(JwtClaim.USER_ID, loginUser.getUser().getId());

        // 存儲使用者角色
        List<String> roles = loginUser.getAuthorities().stream().distinct()
                .map(GrantedAuthority::getAuthority).toList();
        payload.put(JwtClaim.AUTHORITIES, roles);

        // 新增設備指紋
        payload.put("fingerprint", createDeviceFingerprint(request));

        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenExpire * 1000L);

        return JWT.create()
                .withJWTId(UUID.randomUUID().toString().replace("-", ""))
                .withSubject(authentication.getName())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withPayload(payload)
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    /**
     * 解析標準JWT令牌
     */
    private Map<String, Object> parseStandardJwt(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            DecodedJWT jwt = verifier.verify(token);

            Map<String, Claim> claims = jwt.getClaims();
            return claims.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().as(Object.class)));
        } catch (JWTVerificationException e) {
            log.error("JWT驗證失敗: {}", e.getMessage());
            throw BizException.create(ApiResult.TOKEN_INVALID, "JWT驗證失敗");
        }
    }

    /**
     * 建立設備指紋
     */
    private String createDeviceFingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String remoteAddr = getClientIp(request);
        String sessionId = request.getSession().getId();

        return DigestUtils.md5Hex(userAgent + remoteAddr + sessionId);
    }

    /**
     * 取得客戶端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 取第一個IP（多級代理的情況下）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 加密令牌
     */
    private String encryptToken(String token) throws Exception {
        SecretKey key = new SecretKeySpec(encryptionKey, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        byte[] encryptedData = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));

        // 組合IV和加密數據
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 解密令牌
     */
    private String decryptToken(String encryptedToken) throws Exception {
        if (StringUtils.isBlank(encryptedToken)) {
            throw new IllegalArgumentException("加密令牌不能為空");
        }
        
        // 確保輸入是有效的 Base64 字符串
        // 替換可能破壞 Base64 解碼的字符
        String sanitizedToken = encryptedToken.trim()
                .replace(" ", "+") // 處理URL編碼空格問題
                .replace("\n", "") // 移除換行符
                .replace("\r", ""); // 移除回車符
        
        try {
            byte[] combined = Base64.getDecoder().decode(sanitizedToken);
            if (combined.length < 12) { // 至少需要IV長度
                throw new IllegalArgumentException("加密令牌格式無效");
            }
            
            // 分離IV和加密數據
            byte[] iv = new byte[12];
            byte[] encryptedData = new byte[combined.length - iv.length];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encryptedData, 0, encryptedData.length);

            SecretKey key = new SecretKeySpec(encryptionKey, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);

            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
            byte[] decryptedData = cipher.doFinal(encryptedData);

            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            log.error("Base64解碼失敗: {}, 令牌: {}", e.getMessage(), sanitizedToken);
            throw new IllegalArgumentException("無效的令牌格式");
        } catch (Exception e) {
            log.error("令牌解密失敗: {}", e.getMessage(), e);
            throw new IllegalArgumentException("令牌解密失敗");
        }
    }

    /**
     * 建立臨時登錄使用者（用於重整令牌）
     */
    private LoginUser createTempLoginUser(String username) {
        // 建立 SysUser 對象
        SysUser user = SysUser.builder()
                .username(username)
                .status(1) // 啟用狀態
                .deleted(0) // 未刪除
                .build();

        // 設置基本角色
        SysRole defaultRole = new SysRole();
        defaultRole.setId(1L);
        defaultRole.setCode("user");
        defaultRole.setName("一般使用者");

        // 設置使用者角色
        List<SysRole> roles = new ArrayList<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        // 使用 SysUser 建立 LoginUser
        return new LoginUser(user);
    }
}
