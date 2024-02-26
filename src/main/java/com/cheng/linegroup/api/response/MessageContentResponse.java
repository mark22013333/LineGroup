package com.cheng.linegroup.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageContentResponse {
    /**
     * If you specify a message ID other than video or audio (400 Bad Request)
     * 400 Bad Request
     * <p>
     * If you specify a non-existent message ID (404 Not Found)
     * 404 Not found
     * <p>
     * If the user unsends a message (410 Gone)
     * 410 Gone
     */
    private String message;

    /**
     * processing: 準備獲取內容
     * succeeded: 可以獲取用戶發送的內容
     * failed: 取得內容失敗
     */
    private String status;

    private String contentType;
    private byte[] binaryData;

}
