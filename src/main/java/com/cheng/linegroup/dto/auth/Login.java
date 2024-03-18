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
    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private Long expires;
}
