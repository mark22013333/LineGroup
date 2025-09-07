package com.cheng.linegroup.controller;

import com.cheng.linegroup.config.GoogleMapsConfig;
import com.cheng.linegroup.entity.ApiKeyAccessLog;
import com.cheng.linegroup.entity.ClientPublicKey;
import com.cheng.linegroup.enums.common.LogType;
import com.cheng.linegroup.repository.ApiKeyAccessLogRepository;
import com.cheng.linegroup.repository.ClientPublicKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/maps")
public class GoogleMapsController {

    private final GoogleMapsConfig googleMapsConfig;
    private final ApiKeyAccessLogRepository apiKeyAccessLogRepository;
    private final ClientPublicKeyRepository clientPublicKeyRepository;

    // 限制：每個 IP 每分鐘最多請求 5 次
    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(1);

    /**
     * 提供 Google Maps API Key
     * 實作以下安全機制:
     * 1. Referer 檢查：確保請求來自自己的網站
     * 2. 請求頻率限制：防止頻繁請求濫用 API Key
     * 3. 記錄請求 IP 地址：便於追蹤潛在濫用
     * <p>
     * 注意: 即使此端點返回原始 API Key，安全性仍取決於:
     * - 在 Google Cloud Console 設定的 HTTP Referer 限制（這個也已經設定了）
     * - 設定的使用量配額
     * - 監控與記錄
     *
     * @return Google Maps API Key
     */
    @GetMapping("/key")
    public ResponseEntity<String> getApiKey(HttpServletRequest request) {
        String referer = request.getHeader("referer");

        // 如果沒有 referer 則拒絕請求
        if (referer == null) {
            log.warn("API Key request denied - missing referer header");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: No referer header");
        }

        try {
            // 解析 referer 以取得主機名
            URI refererUri = new URI(referer);
            String refererHost = refererUri.getHost();
            String requestHost = request.getServerName();

            // 檢查 referer 是否來自同一主機
            if (!refererHost.equals(requestHost)) {
                log.warn("API Key request denied - invalid referer: {} (expected: {})", refererHost, requestHost);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Invalid referer");
            }

            // 取得客戶端 IP 地址
            String clientIp = getClientIp(request);
            Instant now = Instant.now();

            // 檢查該 IP 的請求頻率是否超過限制
            if (isRateLimitExceeded(clientIp, now)) {
                log.warn("API Key request denied - rate limit exceeded for IP: {}", clientIp);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests. Please try again later.");
            }

            // 記錄此次請求
            logApiKeyAccess(clientIp, now);

            log.info("API Key accessed by IP: {}", clientIp);

            // 返回原始 API Key
            return ResponseEntity.ok(googleMapsConfig.getApiKey());
        } catch (URISyntaxException e) {
            log.error("API Key request denied - invalid referer format", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Invalid referer format");
        }
    }

    /**
     * 取得客戶端真實 IP 地址
     * 處理代理伺服器和負載均衡器情況
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 取得第一個 IP（最初的客戶端 IP）
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 檢查 IP 的請求頻率是否超過限制
     *
     * @param clientIp    客戶端 IP
     * @param currentTime 當前時間
     * @return 如果超過限制返回 true，否則返回 false
     */
    private boolean isRateLimitExceeded(String clientIp, Instant currentTime) {
        Instant windowStart = currentTime.minus(RATE_LIMIT_WINDOW);

        // 查詢在時間窗口內此 IP 的請求次數
        List<ApiKeyAccessLog> recentLogs = apiKeyAccessLogRepository.findByClientIpAndAccessTimeBetween(
                clientIp, windowStart, currentTime);

        return recentLogs.size() >= MAX_REQUESTS_PER_MINUTE;
    }

    /**
     * 記錄 API Key 存取，以便日後審核和監控
     * 儲存到資料庫並實現頻率限制
     */
    private void logApiKeyAccess(String clientIp, Instant accessTime) {
        ApiKeyAccessLog accessLog = new ApiKeyAccessLog();
        accessLog.setClientIp(clientIp);
        accessLog.setAccessTime(accessTime);
        accessLog.setApiType(LogType.GOOGLE_MAPS);
        apiKeyAccessLogRepository.save(accessLog);

        log.info("API Key Access - IP: {}, Time: {}", clientIp, accessTime);
    }

    @PostMapping("/register-key")
    public ResponseEntity<?> registerPublicKey(@RequestBody Map<String, String> keyData,
                                               HttpServletRequest request) {
        String clientId = keyData.get("clientId");
        String publicKeyBase64 = keyData.get("publicKey");

        if (clientId == null || publicKeyBase64 == null) {
            return ResponseEntity.badRequest().body("Client ID and public key are required");
        }

        // 取得客戶端 IP 地址
        String clientIp = getClientIp(request);

        try {
            // 儲存客戶端公鑰
            ClientPublicKey keyEntity = new ClientPublicKey();
            keyEntity.setClientId(clientId);
            keyEntity.setPublicKey(publicKeyBase64);
            keyEntity.setClientIp(clientIp);
            keyEntity.setCreatedAt(Instant.now());

            clientPublicKeyRepository.save(keyEntity);

            log.info("Client public key registered - Client ID: {}, IP: {}", clientId, clientIp);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Public key registered successfully"));
        } catch (Exception e) {
            log.error("Failed to register public key", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Failed to register public key"));
        }
    }

    /**
     * 取得加密的 Google Maps API Key
     *
     * @param clientId - 客戶端ID
     * @param request  - HTTP請求
     * @return 加密的API Key
     */
    @GetMapping("/encrypted-key")
    public ResponseEntity<?> getEncryptedKey(@RequestParam(required = false) String clientId,
                                             HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        Instant now = Instant.now();

        // 驗證請求者
        if (!validateReferer(request)) {
            log.warn("Invalid referer for API key request from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Invalid referer");
        }

        // 檢查請求頻率
        if (isRateLimited(clientIp, now)) {
            log.warn("Rate limit exceeded for API key request from IP: {}", clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Rate limit exceeded");
        }

        try {
            // 檢查此客戶端ID是否已註冊公鑰
            if (clientId == null || clientId.isEmpty()) {
                log.warn("No client ID provided for encrypted API key request from IP: {}", clientIp);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Client ID is required");
            }

            // 查詢客戶端的公鑰
            ClientPublicKey keyEntity = clientPublicKeyRepository.findByClientId(clientId)
                    .orElse(null);
            if (keyEntity == null) {
                log.warn("No public key found for client ID: {} from IP: {}", clientId, clientIp);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No public key registered for this client ID");
            }

            // 解析公鑰
            try {
                // 記錄公鑰詳細訊息用於調試
                String publicKeyString = keyEntity.getPublicKey();
                log.info("嘗試解析公鑰, 長度: {}, 前20個字串: {}...", 
                        publicKeyString.length(), 
                        publicKeyString.substring(0, Math.min(20, publicKeyString.length())));
                
                // 使用Java標準庫解析公鑰
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey;
                
                try {
                    // 嘗試方法1：直接解碼Base64並解析為X509
                    byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
                    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
                    publicKey = keyFactory.generatePublic(keySpec);
                    log.info("使用方法1成功解析公鑰");
                } catch (Exception e) {
                    log.info("方法1解析失敗: {}", e.getMessage());
                    
                    // 如果第一種方法失敗，嘗試第二種方法
                    try {
                        // 嘗試方法2：使用Java的標準Base64編碼
                        byte[] decoded = java.util.Base64.getMimeDecoder().decode(publicKeyString);
                        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
                        publicKey = keyFactory.generatePublic(spec);
                        log.info("使用方法2成功解析公鑰");
                    } catch (Exception e2) {
                        log.info("方法2解析失敗: {}", e2.getMessage());
                        
                        // 如果方法2也失敗，拋出原始異常
                        throw e;
                    }
                }
                
                if (publicKey == null) {
                    throw new Exception("無法解析公鑰");
                }
                
                log.info("成功解析為RSA公鑰");
                
                // 使用公鑰加密 API Key
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                byte[] encryptedBytes = cipher.doFinal(googleMapsConfig.getApiKey().getBytes(StandardCharsets.UTF_8));
                String encryptedKey = Base64.getEncoder().encodeToString(encryptedBytes);

                logApiKeyAccess(clientIp, now);
                log.info("Encrypted API Key accessed by IP: {} for client ID: {}", clientIp, clientId);

                // 直接返回加密後的API Key（純文本格式）
                return ResponseEntity.ok(encryptedKey);
            } catch (Exception e) {
                log.error("Failed to encrypt API key: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error encrypting API key: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Failed to process encrypted API key request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing encrypted API key request");
        }
    }

    private boolean validateReferer(HttpServletRequest request) {
        String referer = request.getHeader("referer");
        if (referer == null || referer.isEmpty()) {
            return false;
        }
        try {
            URI refererUri = new URI(referer);
            String refererHost = refererUri.getHost();
            String requestHost = request.getServerName();
            return refererHost.equals(requestHost);
        } catch (URISyntaxException e) {
            log.error("Invalid referer format", e);
            return false;
        }
    }

    private boolean isRateLimited(String clientIp, Instant now) {
        Instant windowStart = now.minus(RATE_LIMIT_WINDOW);
        List<ApiKeyAccessLog> recentLogs = apiKeyAccessLogRepository.findByClientIpAndAccessTimeBetween(
                clientIp, windowStart, now);
        return recentLogs.size() >= MAX_REQUESTS_PER_MINUTE;
    }
}
