package com.cheng.linegroup.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.cheng.linegroup.common.contants.JwtClaim;
import com.cheng.linegroup.common.contants.OAuth2;
import com.cheng.linegroup.common.contants.RedisPrefix;
import com.cheng.linegroup.dto.auth.LoginResponse;
import com.cheng.linegroup.dto.auth.LoginUser;
import com.cheng.linegroup.dto.security.SysUserDetails;
import com.cheng.linegroup.entity.SysRole;
import com.cheng.linegroup.entity.SysUser;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.enums.common.Status;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.filter.token.dto.TokenInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
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

    // 使用jasypt密碼作為加密密鑰
    public static final String ENCRYPTION_KEY = System.getProperty("jasypt.encryptor.password");

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret}")
    public void setJwtSecret(String secret) {
        SecureJwtUtils.jwtSecret = secret.getBytes();

        // 使用jasypt密碼作為加密密鑰，若未設置則從JWT密鑰派生
        if (ENCRYPTION_KEY != null && !ENCRYPTION_KEY.isEmpty()) {
            SecureJwtUtils.encryptionKey = DigestUtils.sha256(ENCRYPTION_KEY.getBytes());
            log.info("使用jasypt加密器密碼作為JWE加密密鑰");
        } else {
            // 向後兼容：從JWT密鑰派生加密密鑰
            SecureJwtUtils.encryptionKey = DigestUtils.sha256(secret.getBytes());
            log.warn("未找到jasypt加密器密碼，從JWT密鑰派生JWE加密密鑰");
        }
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

        // 取出使用者訊息
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenExpire * 1000L);

        // 建立令牌ID
        String tokenId = UUID.randomUUID().toString();
        String deviceFingerprint = createDeviceFingerprint(request);

        // 產生標準JWT格式的令牌
        String standardJwt = JWT.create()
                .withSubject(authentication.getName())
                .withClaim(JwtClaim.USER_ID, loginUser.getUser().getId())
                .withClaim(JwtClaim.FINGERPRINT, deviceFingerprint)
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withJWTId(UUID.randomUUID().toString().replace("-", ""))
                .withClaim(JwtClaim.AUTHORITIES, authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .sign(Algorithm.HMAC256(jwtSecret));

        log.debug("產生標準JWT: {}", standardJwt.length() > 20 ? standardJwt.substring(0, 20) + "..." : standardJwt);

        // 儲存令牌訊息到Redis
        TokenInfo tokenInfo = new TokenInfo(
                loginUser.getUser().getId(),
                deviceFingerprint,
                new Date()
        );

        try {
            log.info("正在儲存令牌至Redis，令牌ID: {}, 使用者ID: {}", tokenId, loginUser.getUser().getId());
            redisTemplate.opsForValue().set(
                    RedisPrefix.ACTIVE_TOKEN + tokenId,
                    tokenInfo,
                    tokenExpire + 300, // 比JWT有效期多5分鐘
                    TimeUnit.SECONDS
            );
            log.info("令牌成功儲存至Redis: {}", tokenId);

            // 驗證令牌是否成功儲存
            TokenInfo savedTokenInfo = (TokenInfo) redisTemplate.opsForValue().get(RedisPrefix.ACTIVE_TOKEN + tokenId);
            if (savedTokenInfo == null) {
                log.error("令牌儲存失敗，無法從Redis中檢索到令牌: {}", tokenId);
            } else {
                log.info("驗證令牌儲存成功，使用者ID: {}", savedTokenInfo.getUserId());
            }
        } catch (Exception e) {
            log.error("儲存令牌到Redis失敗: {}", e.getMessage(), e);
            throw BizException.create(ApiResult.ERROR, "儲存令牌失敗");
        }

        // 加密JWT令牌
        try {
            return encryptToken(standardJwt + "###" + tokenId);
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

        // 儲存重整令牌訊息到Redis
        redisTemplate.opsForValue().set(
                RedisPrefix.REFRESH_TOKEN + refreshTokenId,
                userId + "###" + username,
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
    public LoginResponse refreshToken(String encryptedRefreshToken, HttpServletRequest request) {
        if (StringUtils.isBlank(encryptedRefreshToken)) {
            throw BizException.create(ApiResult.TOKEN_INVALID, "重整令牌為空");
        }

        try {
            // 解密重整令牌
            String refreshTokenId = decryptToken(encryptedRefreshToken);

            // 從Redis取得對應使用者訊息
            String userInfo = (String) redisTemplate.opsForValue().get(RedisPrefix.REFRESH_TOKEN + refreshTokenId);
            if (userInfo == null) {
                throw BizException.create(ApiResult.TOKEN_INVALID, "重整令牌已過期");
            }

            // 分離使用者ID和使用者名
            String[] parts = userInfo.split("###");
            // 兼容舊格式
            if (parts.length != 2) {
                parts = userInfo.split(":");
            }

            if (parts.length != 2) {
                throw BizException.create(ApiResult.TOKEN_INVALID, "重整令牌格式無效");
            }

            Long userId = Long.valueOf(parts[0]);
            String username = parts[1];

            // 建立臨時認證物件
            Authentication authentication = getAuthenticationForUser(username);

            // 產生新的訪問令牌
            String secureToken = generateSecureToken(authentication, request);

            // 刪除舊重整令牌
            redisTemplate.delete(RedisPrefix.REFRESH_TOKEN + refreshTokenId);

            // 產生新的重整令牌
            String newRefreshToken = generateRefreshToken(userId, username);

            // 封裝回應資料
            return new LoginResponse(secureToken, newRefreshToken);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("處理重整令牌時發生錯誤: {}", e.getMessage(), e);
            throw BizException.create(ApiResult.TOKEN_INVALID, "處理重整令牌時發生錯誤");
        }
    }

    /**
     * 解析並驗證安全令牌
     *
     * @param encryptedToken 加密的JWT令牌或標準JWT令牌
     * @param request        HTTP請求，用於驗證設備指紋
     * @return JWT Payload
     */
    public Map<String, Object> parseAndVerifySecureToken(String encryptedToken, HttpServletRequest request) {
        if (StringUtils.isBlank(encryptedToken)) {
            log.warn("令牌為空");
            return Collections.emptyMap();
        }

        // 移除Bearer前綴
        if (encryptedToken.startsWith(OAuth2.BEARER)) {
            encryptedToken = encryptedToken.replace(OAuth2.BEARER, StringUtils.EMPTY);
            log.debug("移除了Bearer前綴");
        }

        try {
            log.debug("嘗試解密令牌: {}", encryptedToken.length() > 20 ? encryptedToken.substring(0, 20) + "..." : encryptedToken);

            // 解密令牌
            String combinedToken = decryptToken(encryptedToken);
            log.debug("令牌解密成功，解密後內容前20個字串: {}",
                    combinedToken.length() > 20 ? combinedToken.substring(0, 20) + "..." : combinedToken);

            // 首先嘗試使用新分隔符 "###" 分離JWT和令牌ID
            String[] parts = combinedToken.split("###");
            log.debug("使用新分隔符'###'分割後得到 {} 個部分", parts.length);

            // 如果分割失敗，嘗試舊的分隔符":"
            if (parts.length != 2) {
                log.debug("嘗試使用舊分隔符':'進行分割");
                parts = combinedToken.split(":");
                // 舊分隔符可能導致多個部分，需要特殊處理
                if (parts.length > 2) {
                    // 假設最後一部分是tokenId，其餘部分組成JWT
                    String tokenId = parts[parts.length - 1];
                    StringBuilder jwtBuilder = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (i > 0) jwtBuilder.append(":");
                        jwtBuilder.append(parts[i]);
                    }
                    parts = new String[]{jwtBuilder.toString(), tokenId};
                    log.debug("使用舊分隔符處理後得到 {} 個部分", parts.length);
                }
            }

            // 最終檢查分割結果
            if (parts.length != 2) {
                log.warn("令牌格式無效，無法分割為兩部分: {}",
                        combinedToken.length() > 50 ? combinedToken.substring(0, 50) + "..." : combinedToken);
                return Collections.emptyMap();
            }

            String jwt = parts[0];
            String tokenId = parts[1];
            log.debug("JWT前20個字串: {}..., 令牌ID: {}",
                    jwt.length() > 20 ? jwt.substring(0, 20) : jwt, tokenId);

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

                try {
                    // 將令牌加入黑名單（如果可以解析）
                    if (StringUtils.isNotBlank(jwt)) {
                        // 檢查JWT格式
                        String[] jwtParts = jwt.split("\\.");
                        if (jwtParts.length != 3) {
                            log.warn("JWT格式無效，無法處理黑名單: {}", jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt);
                        } else {
                            DecodedJWT decodedJWT = JWT.decode(jwt);
                            String jti = decodedJWT.getId();
                            if (StringUtils.isNotBlank(jti)) {
                                redisTemplate.opsForValue().set(
                                        RedisPrefix.BLACKLIST_TOKEN + jti,
                                        "STOLEN",
                                        30,
                                        TimeUnit.DAYS
                                );
                                log.debug("已將盜用令牌加入黑名單, jti: {}", jti);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 如果JWT解析失敗，記錄錯誤但繼續處理
                    log.warn("無法解析JWT以取得jti，跳過黑名單處理: {}", e.getMessage());
                }

                // 無論如何，都刪除活躍令牌記錄
                redisTemplate.delete(RedisPrefix.ACTIVE_TOKEN + tokenId);
                log.debug("已刪除活躍令牌記錄: {}", tokenId);

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

            // 新增令牌ID到 Payload
            claims.put("tokenId", tokenId);

            return claims;
        } catch (Exception e) {
            log.error("解析加密令牌失敗: {}", e.getMessage());
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
            String[] parts = combinedToken.split("###");
            // 嘗試舊分隔符
            if (parts.length != 2) {
                log.debug("使用新分隔符分割失敗，嘗試舊分隔符");
                parts = combinedToken.split(":");
                // 舊分隔符可能導致多個部分，需要特殊處理
                if (parts.length > 2) {
                    String tokenId = parts[parts.length - 1];
                    StringBuilder jwtBuilder = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; i++) {
                        if (i > 0) jwtBuilder.append(":");
                        jwtBuilder.append(parts[i]);
                    }
                    parts = new String[]{jwtBuilder.toString(), tokenId};
                }
            }

            if (parts.length != 2) {
                log.warn("無法分割令牌: {}", combinedToken.length() > 50 ? combinedToken.substring(0, 50) + "..." : combinedToken);
                return;
            }

            String jwt = parts[0];
            String tokenId = parts[1];
            log.debug("正在註銷令牌 ID: {}", tokenId);

            try {
                // 將JWT加入黑名單 (如果格式有效)
                if (StringUtils.isNotBlank(jwt)) {
                    // 檢查JWT格式
                    String[] jwtParts = jwt.split("\\.");
                    if (jwtParts.length != 3) {
                        log.warn("JWT格式無效，跳過黑名單處理");
                    } else {
                        DecodedJWT decodedJWT = JWT.decode(jwt);
                        String jti = decodedJWT.getId();
                        if (StringUtils.isNotBlank(jti)) {
                            Date expiresAt = decodedJWT.getExpiresAt();
                            long ttl = Math.max(1, (expiresAt.getTime() - System.currentTimeMillis()) / 1000);

                            redisTemplate.opsForValue().set(
                                    RedisPrefix.BLACKLIST_TOKEN + jti,
                                    "REVOKED",
                                    ttl,
                                    TimeUnit.SECONDS
                            );
                            log.debug("已加入黑名單: {}, 有效期: {}秒", jti, ttl);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("將JWT加入黑名單時發生錯誤: {}", e.getMessage());
                // 繼續處理，確保令牌仍然被刪除
            }

            // 刪除活躍令牌
            redisTemplate.delete(RedisPrefix.ACTIVE_TOKEN + tokenId);
            log.debug("已刪除活躍令牌: {}", tokenId);
        } catch (Exception e) {
            log.error("註銷令牌時發生錯誤: {}", e.getMessage(), e);
        }
    }

    /**
     * 從JWT Payload建立認證物件
     *
     * @param payload JWT Payload
     * @return 認證物件
     */
    public static UsernamePasswordAuthenticationToken createAuthenticationFromPayload(Map<String, Object> payload) {
        SysUserDetails userDetails = new SysUserDetails();

        // 設置使用者訊息
        long userId = Long.parseLong(String.valueOf(payload.get(JwtClaim.USER_ID)));
        userDetails.setUserId(userId);
        String username = String.valueOf(payload.get(JwtClaim.SUBJECT));
        userDetails.setUsername(username);
        
        // 記錄使用者資訊以便調試
        log.debug("從JWT提取使用者資訊 - ID: {}, 使用者名稱: {}", userId, username);

        // 取得角色權限
        Object authoritiesObj = payload.get(JwtClaim.AUTHORITIES);
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        if (authoritiesObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> authList = (List<String>) authoritiesObj;
            
            // 新增偵錯日誌
            log.debug("JWT中的權限清單: {}", authList);
            
            authorities = authList.stream()
                    .map(role -> {
                        // 確保角色有 ROLE_ 前綴，沒有的話就加上
                        if (!role.startsWith("ROLE_") && role.startsWith("ADMIN") || role.startsWith("USER") || role.startsWith("OPERATOR")) {
                            log.debug("角色 {} 沒有 ROLE_ 前綴，添加前綴", role);
                            return new SimpleGrantedAuthority("ROLE_" + role);
                        }
                        return new SimpleGrantedAuthority(role);
                    })
                    .collect(Collectors.toSet());
        } else {
            String authStr = String.valueOf(authoritiesObj);

            // 解析JSON字串為List<String>
            List<String> roleList = JacksonUtils.fromJson(authStr, new TypeReference<>() {
            });

            if (roleList != null) {
                // 新增偵錯日誌
                log.debug("從JSON解析的權限清單: {}", roleList);
                
                authorities = roleList.stream()
                        .map(role -> {
                            // 確保角色有 ROLE_ 前綴，沒有的話就加上
                            if (!role.startsWith("ROLE_") && (role.startsWith("ADMIN") || role.startsWith("USER") || role.startsWith("OPERATOR"))) {
                                log.debug("角色 {} 沒有 ROLE_ 前綴，添加前綴", role);
                                return new SimpleGrantedAuthority("ROLE_" + role);
                            }
                            return new SimpleGrantedAuthority(role);
                        })
                        .collect(Collectors.toSet());
            } else {
                // 解析失敗時的備用方案
                log.warn("解析權限JSON字串失敗，使用備用方案");
                if (authStr.startsWith("[") && authStr.endsWith("]")) {
                    // 移除括號並分割
                    authStr = authStr.substring(1, authStr.length() - 1);
                    authorities = Arrays.stream(authStr.split(","))
                            .map(String::trim)
                            .map(s -> s.startsWith("\"") && s.endsWith("\"") ? s.substring(1, s.length() - 1) : s)
                            .map(role -> {
                                // 確保角色有 ROLE_ 前綴，沒有的話就加上
                                if (!role.startsWith("ROLE_") && (role.startsWith("ADMIN") || role.startsWith("USER") || role.startsWith("OPERATOR"))) {
                                    log.debug("角色 {} 沒有 ROLE_ 前綴，添加前綴", role);
                                    return new SimpleGrantedAuthority("ROLE_" + role);
                                }
                                return new SimpleGrantedAuthority(role);
                            })
                            .collect(Collectors.toSet());
                } else {
                    // 純文字格式，直接按逗號分割
                    authorities = Arrays.stream(authStr.split(","))
                            .map(String::trim)
                            .map(role -> {
                                // 確保角色有 ROLE_ 前綴，沒有的話就加上
                                if (!role.startsWith("ROLE_") && (role.startsWith("ADMIN") || role.startsWith("USER") || role.startsWith("OPERATOR"))) {
                                    log.debug("角色 {} 沒有 ROLE_ 前綴，添加前綴", role);
                                    return new SimpleGrantedAuthority("ROLE_" + role);
                                }
                                return new SimpleGrantedAuthority(role);
                            })
                            .collect(Collectors.toSet());
                }
            }
        }

        // 使用使用者名稱作為認證對象的principal，確保Authentication.getName()返回使用者名稱
        return new UsernamePasswordAuthenticationToken(username, "", authorities);
    }

    /**
     * 為使用者建立認證對象（用於重整令牌）
     */
    private Authentication getAuthenticationForUser(String username) {
        // 建立臨時登錄使用者
        LoginUser tempUser = createTempLoginUser(username);
        return new UsernamePasswordAuthenticationToken(tempUser, null, tempUser.getAuthorities());
    }

    /**
     * 建立臨時登錄使用者（用於重整令牌）
     */
    private LoginUser createTempLoginUser(String username) {
        SysUser user = SysUser.builder()
                .username(username)
                .status(Status.ENABLE.getValue()) // 啟用狀態
                .deleted(Status.DISABLE.getValue()) // 未刪除
                .build();

        SysRole defaultRole = new SysRole();
        defaultRole.setId(1L);
        defaultRole.setCode("user");
        defaultRole.setName("一般使用者");

        List<SysRole> roles = new ArrayList<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        return new LoginUser(user);
    }

    /**
     * 解析標準JWT令牌
     */
    private Map<String, Object> parseStandardJwt(String token) {
        try {
            // 基本檢查JWT格式 - 避免明顯無效的令牌
            if (StringUtils.isBlank(token)) {
                log.warn("JWT為空");
                return Collections.emptyMap();
            }

            // 檢查JWT格式 - 應該有3個部分（header.payload.signature）
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("JWT格式無效，應該有3個部分，但得到 {} 個部分", parts.length);
                return Collections.emptyMap();
            }

            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            DecodedJWT jwt = verifier.verify(token);

            Map<String, Object> claims = new HashMap<>();
            // 從JWT中取出標準聲明
            claims.put(JwtClaim.SUBJECT, jwt.getSubject());
            claims.put(JwtClaim.JWT_ID, jwt.getId());

            // 取得發行時間和過期時間
            if (jwt.getIssuedAt() != null) {
                claims.put(JwtClaim.ISSUED_AT, jwt.getIssuedAt().getTime() / 1000);
            }

            if (jwt.getExpiresAt() != null) {
                claims.put(JwtClaim.EXPIRES_AT, jwt.getExpiresAt().getTime() / 1000);
            }

            // 取出自定義聲明
            Map<String, Claim> jwtClaims = jwt.getClaims();
            for (Map.Entry<String, Claim> entry : jwtClaims.entrySet()) {
                String key = entry.getKey();
                Claim claim = entry.getValue();

                // 處理特殊聲明
                if (JwtClaim.USER_ID.equals(key)) {
                    claims.put(key, claim.asLong());
                } else if (JwtClaim.AUTHORITIES.equals(key)) {
                    claims.put(key, claim.asList(String.class));
                } else if (JwtClaim.FINGERPRINT.equals(key)) {
                    claims.put(key, claim.asString());
                } else {
                    // 其他聲明以Object方式處理
                    claims.put(key, claim.as(Object.class));
                }
            }

            log.debug("JWT解析成功，聲明數量: {}", claims.size());
            return claims;
        } catch (JWTVerificationException e) {
            log.warn("JWT驗證失敗: {}", e.getMessage());
            return Collections.emptyMap();
        } catch (Exception e) {
            log.warn("JWT解析時發生意外錯誤: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 建立設備指紋
     */
    private String createDeviceFingerprint(HttpServletRequest request) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT) != null ?
                request.getHeader(HttpHeaders.USER_AGENT) : StringUtils.EMPTY;

        String remoteAddr = WebUtils.getClientIp(request);

        // 移除session依賴，避免前後端分離環境中每次請求都有不同的sessionId
        String stableFingerprint = userAgent + remoteAddr;

        // 新增一些瀏覽器特定的標頭訊息，如果可用的話
        String acceptLanguage = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        if (StringUtils.isNotBlank(acceptLanguage)) {
            stableFingerprint += acceptLanguage;
        }

        log.debug("正在產生指紋，User-Agent: {}, IP: {}",
                userAgent.length() > 20 ? userAgent.substring(0, 20) + "..." : userAgent, remoteAddr);

        return DigestUtils.md5Hex(stableFingerprint);
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

        // 使用URL安全的Base64編碼，避免+, / 和 = 字串在URL中的問題
        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
    }

    /**
     * 解密令牌
     */
    private String decryptToken(String encryptedToken) {
        if (StringUtils.isBlank(encryptedToken)) {
            throw new IllegalArgumentException("加密令牌不能為空");
        }

        log.debug("嘗試解密令牌: {}", encryptedToken.substring(0, Math.min(20, encryptedToken.length())) + "...");

        try {
            // 使用URL安全的Base64解碼，兼容標準Base64編碼
            byte[] combined;
            try {
                // 首先嘗試URL安全的Base64解碼
                combined = Base64.getUrlDecoder().decode(encryptedToken);
            } catch (IllegalArgumentException e) {
                log.debug("URL安全Base64解碼失敗，嘗試標準Base64解碼");
                // 處理可能的URL編碼問題
                String sanitizedToken = encryptedToken.trim()
                        .replace(" ", "+") // 處理URL編碼空格問題
                        .replace("\n", "") // 移除換行
                        .replace("\r", "");

                // 嘗試標準Base64解碼
                combined = Base64.getDecoder().decode(sanitizedToken);
            }

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
            log.error("Base64解碼失敗: {}, 令牌: {}", e.getMessage(), encryptedToken);
            throw new IllegalArgumentException("無效的令牌格式");
        } catch (Exception e) {
            log.error("令牌解密失敗: {}", e.getMessage(), e);
            throw new IllegalArgumentException("令牌解密失敗");
        }
    }
}
