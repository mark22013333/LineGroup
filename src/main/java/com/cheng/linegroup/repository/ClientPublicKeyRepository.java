package com.cheng.linegroup.repository;

import com.cheng.linegroup.entity.ClientPublicKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 客戶端公鑰的資料庫操作介面
 */
@Repository
public interface ClientPublicKeyRepository extends JpaRepository<ClientPublicKey, Long> {

    /**
     * 根據客戶端ID查找公鑰
     *
     * @param clientId 客戶端唯一識別碼
     * @return 對應的公鑰實體
     */
    Optional<ClientPublicKey> findByClientId(String clientId);

    /**
     * 根據客戶端IP查找全部公鑰
     *
     * @param clientIp 客戶端IP地址
     * @return 所有對應IP的公鑰列表
     */
    Iterable<ClientPublicKey> findAllByClientIp(String clientIp);
}
