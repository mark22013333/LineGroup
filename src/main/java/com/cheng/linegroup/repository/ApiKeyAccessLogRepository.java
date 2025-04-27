package com.cheng.linegroup.repository;

import com.cheng.linegroup.entity.ApiKeyAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * API Key 存取日誌儲存庫
 * 用於存取和查詢 API Key 的使用記錄
 */
@Repository
public interface ApiKeyAccessLogRepository extends JpaRepository<ApiKeyAccessLog, Long> {

    /**
     * 查詢指定 IP 在特定時間範圍內的存取記錄
     * 用於實現請求頻率限制功能
     *
     * @param clientIp  客戶端 IP 地址
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 符合條件的存取記錄列表
     */
    List<ApiKeyAccessLog> findByClientIpAndAccessTimeBetween(String clientIp, Instant startTime, Instant endTime);

    /**
     * 查詢指定 API 類型在特定時間範圍內的存取記錄
     *
     * @param apiType   API 類型
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 符合條件的存取記錄列表
     */
    List<ApiKeyAccessLog> findByApiTypeAndAccessTimeBetween(String apiType, Instant startTime, Instant endTime);

    /**
     * 查詢指定 IP 和 API 類型在特定時間範圍內的存取記錄
     *
     * @param clientIp  客戶端 IP 地址
     * @param apiType   API 類型
     * @param startTime 開始時間
     * @param endTime   結束時間
     * @return 符合條件的存取記錄列表
     */
    List<ApiKeyAccessLog> findByClientIpAndApiTypeAndAccessTimeBetween(
            String clientIp, String apiType, Instant startTime, Instant endTime);
}
