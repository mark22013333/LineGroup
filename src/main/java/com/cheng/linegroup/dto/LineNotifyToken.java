package com.cheng.linegroup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author cheng
 * @since 2024/1/4 15:21
 **/
@Data
public class LineNotifyToken {
    private int status;
    private String message;

    @JsonProperty("access_token")
    private String accessToken;
}
