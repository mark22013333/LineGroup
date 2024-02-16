package com.cheng.linegroup.service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author cheng
 * @since 2023/12/3 3:36 PM
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class LineMessage {

    @JsonProperty("to")
    private String uid;
    private String replyToken;
    private List<Message> messages;
    private List<String> customAggregationUnits;


    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        @Builder.Default
        private String type = "text";
        @JsonProperty("text")
        private String msg;
        private String originalContentUrl;
        private String previewImageUrl;
    }
}
