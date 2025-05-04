package com.cheng.linegroup.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 儲存客戶端的公鑰訊息，用於API Key的非對稱加密
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientPublicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客戶端的唯一識別碼 (通常是UUID或裝置識別碼)
     */
    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    /**
     * 客戶端的公鑰 (Base64編碼)
     */
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;

    /**
     * 客戶端的IP地址
     */
    @Column(name = "client_ip")
    private String clientIp;

    /**
     * 公鑰註冊時間
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * 最後使用時間
     */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;
}
