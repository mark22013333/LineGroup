package com.cheng.linegroup.security.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 令牌訊息類，用於存儲活躍令牌的相關訊息
 * 
 * @author cheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 使用者ID
     */
    private Long userId;
    
    /**
     * 設備指紋 - 用於防止令牌被盜用
     */
    private String deviceFingerprint;
    
    /**
     * 最後活動時間
     */
    private Date lastActivityTime;
    
    /**
     * 建立時間
     */
    private Date createdTime;
    
    public TokenInfo(Long userId, String deviceFingerprint, Date lastActivityTime) {
        this.userId = userId;
        this.deviceFingerprint = deviceFingerprint;
        this.lastActivityTime = lastActivityTime;
        this.createdTime = new Date();
    }
}
