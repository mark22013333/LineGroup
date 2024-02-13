package com.cheng.linegroup.common.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cheng
 * @since 2024/2/11 12:46
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "line.header")
public class LineHeader {
    private String signature;
    private String requestId;
}
