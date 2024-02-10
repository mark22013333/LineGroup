package com.cheng.linegroup.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author cheng
 * @since 2024/1/4 14:40
 **/
@Data
public class LineNotifyOauth {
    private String code;
    private String state;
    private String error;
    
    @JsonProperty("error_description")
    private String errorDescription;
}
