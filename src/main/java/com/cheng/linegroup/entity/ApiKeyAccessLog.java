package com.cheng.linegroup.entity;

import com.cheng.linegroup.enums.common.LogType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 用於記錄 API Key 的存取情況並實現頻率限制
 */
@Data
@NoArgsConstructor
@Entity
@Table(
        indexes = {
                @Index(name = "idx_client_ip_access_time", columnList = "clientIp,accessTime")
        })
public class ApiKeyAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 客戶端 IP 地址
     */
    @Column(nullable = false, length = 50)
    private String clientIp;

    /**
     * 存取時間
     */
    @Column(nullable = false)
    private Instant accessTime;

    /**
     * API 類型 (例如: "GOOGLE_MAPS", "OPENAI", 等)
     */
    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private LogType apiType;

    /**
     * 存取結果 (例如: 成功、失敗、拒絕)
     */
    @Column(length = 20)
    private String accessResult;

    /**
     * 額外資訊 (可儲存任何相關的附加資訊)
     */
    @Column(length = 500)
    private String additionalInfo;
}
