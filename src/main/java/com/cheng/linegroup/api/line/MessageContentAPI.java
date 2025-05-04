package com.cheng.linegroup.api.line;

import com.cheng.linegroup.api.response.MessageContentResponse;
import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.utils.ApiUtils;
import com.cheng.linegroup.utils.JacksonUtils;
import com.cheng.linegroup.utils.OkHttpUtils;
import com.cheng.linegroup.utils.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageContentAPI {

    private final Line line;

    /**
     * [GET] https://api-data.line.me/v2/bot/message/{messageId}/content
     *
     * @apiNote 取得使用者透過 webhook 接收訊息 ID 的內容<p>
     * 訊息發送一段時間後，內容會自動刪除，無法保證內容的儲存時間<p>
     * <p>
     * {messageId} 只能取得圖像、視訊、音訊、檔案<p>
     * <p>
     * response<p>
     * http status 200 和二進位內容，資料檔案格式則在 response header Content-Type
     */
    public <T> T getMessageContent(String messageId, Class<T> messageContentResponse) {
        String url = combineParamsUrl(Api.LINE_MESSAGE_CONTENT, messageId);
        return handleApiResponse(url, messageContentResponse);
    }

    /**
     * [GET] https://api-data.line.me/v2/bot/message/{messageId}/content/transcoding
     *
     * @apiNote 取得使用者透過 webhook 接收訊息 ID 的內容<p>
     * <p>
     * {messageId} 主要取得視訊或音訊準備狀態<p>
     * <p>
     * response<p>
     * processing: 準備取得內容<p>
     * succeeded: 可以取得使用者發送的內容<p>
     * failed: 取得內容失敗<p>
     */
    public <T> T getVideoOrAudioStatus(String messageId, Class<T> messageContentResponse) {
        String url = combineParamsUrl(Api.LINE_MESSAGE_CONTENT_TRANSCODING, messageId);
        return handleApiResponse(url, messageContentResponse);
    }

    /**
     * [GET] https://api-data.line.me/v2/bot/message/{messageId}/content/preview
     *
     * @apiNote 取得使用者透過 webhook 接收訊息 ID 的內容<p>
     * 預覽影像是轉換為比原始內容更小的資料大小的影像資料<p>
     * <p>
     * {messageId} 主要取得影像或影片的預覽影像<p>
     * <p>
     * response<p>
     * http status 200 和以二進位形式回傳預覽影像
     */
    public <T> T getImageOrVideoPreview(String messageId, Class<T> messageContentResponse) {
        String url = combineParamsUrl(Api.LINE_MESSAGE_CONTENT_PREVIEW, messageId);
        return handleApiResponse(url, messageContentResponse);
    }

    private String combineParamsUrl(Api api, Object... param) {
        return ApiUtils.getUrl(line.getApiDataDomain(), String.format(api.getUri(), param));
    }

    private <T> T handleApiResponse(String url, Class<T> responseType) {
        ApiResponse apiResponse = OkHttpUtils.builder().addLineAuthHeader(line.getMessage().getChannelToken()).get(url).sync();
        int httpStatusCode = apiResponse.getHttpStatusCode();
        String contentType = apiResponse.getContentType();
        String resultData = apiResponse.getResultData();
        if (httpStatusCode == HttpStatus.OK.value()) {
            if ("application/json".equals(contentType)) {
                log.info("resultData:{}", resultData);
                return JacksonUtils.decodeFromJson(resultData, responseType);
            } else if (contentType.startsWith("audio") || contentType.startsWith("video")) {
                log.info("contentType: {}", contentType);
                MessageContentResponse response = MessageContentResponse.builder()
                        .contentType(contentType)
                        .binaryData(apiResponse.getBinaryData())
                        .build();
                return responseType.cast(response);
            } else {
                log.error("Unexpected contentType: {}", contentType);
                return null;
            }
        } else {
            return JacksonUtils.decodeFromJson(resultData, responseType);
//            throw BizException.error(httpStatusCode, resultData);
        }
    }
}
