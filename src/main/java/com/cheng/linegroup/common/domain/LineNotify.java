package com.cheng.linegroup.common.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cheng
 * @since 2023/12/3 2:09 PM
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "line.notify")
@Deprecated(since = "2024-11-02", forRemoval = true)
public class LineNotify {
    private String clientId;
    private String clientSecret;
    private String selfToken;
    private String apiDomain;
    private String oauthDomain;
    private String centralControlGroupToken;
    private String callbackDomain;
}
