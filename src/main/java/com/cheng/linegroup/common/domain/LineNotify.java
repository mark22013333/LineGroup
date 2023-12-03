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
public class LineNotify {
    private String selfToken;
    private String apiDomain;
    private String centralControlGroupToken;
}
