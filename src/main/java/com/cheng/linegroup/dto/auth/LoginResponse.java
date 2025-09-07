package com.cheng.linegroup.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登入回應 DTO，包含訪問令牌和重整令牌
 *
 * @author cheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    /**
     * 加密的安全令牌
     */
    private String secureToken;
    
    /**
     * 重整令牌
     */
    private String refreshToken;
}
