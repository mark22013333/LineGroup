package com.cheng.linegroup.dto.auth;

import lombok.Builder;
import lombok.Data;

/**
 * @author cheng
 * @since 2024/3/9 12:20
 **/
@Data
@Builder
public class Login {
    private String tokenType;
    private String refreshToken;
    private Long expires;
    private String secureToken; // 加密的JWT令牌 (JWE)，系統唯一使用的令牌類型
}
