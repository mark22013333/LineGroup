package com.cheng.linegroup.common.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author cheng
 * @since 2023/12/3 3:01 PM
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "line")
public class Line {
    private String apiDomain;
    private Login login;
    private Message message;

    @Data
    public static class Login {
        private String channelId;
        private String channelSecret;
    }

    @Data
    public static class Message {
        private String selfUid;
        private String channelId;
        private String channelSecret;
        private String channelToken;
    }
}
