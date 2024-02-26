package com.cheng.linegroup.service;

import com.cheng.linegroup.api.line.GroupAPI;
import com.cheng.linegroup.api.line.MessageContentAPI;
import com.cheng.linegroup.api.response.LineGroupResponse;
import com.cheng.linegroup.api.response.MessageContentResponse;
import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.enums.ApiResult;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.service.dto.LineMessage;
import com.cheng.linegroup.utils.ApiUtils;
import com.cheng.linegroup.utils.JacksonUtils;
import com.cheng.linegroup.utils.OkHttpUtils;
import com.cheng.linegroup.utils.dto.ApiResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author cheng
 * @since 2023/12/3 3:20 PM
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LineService {

    private final Line line;
    private final GroupAPI groupAPI;
    private final MessageContentAPI messageContentAPI;

    public void CallMessageAPI(LineMessage lineMessage, Api api) {
        String channelToken = line.getMessage().getChannelToken();

        try {
            String url = ApiUtils.getUrl(line.getApiDomain(), api);
            ObjectNode jsonNodes = JacksonUtils.toNode(lineMessage);

            ApiResponse apiResponse = OkHttpUtils.builder().addLineAuthHeader(channelToken)
                    .addParam(jsonNodes)
                    .post(url).sync();

            int httpStatusCode = apiResponse.getHttpStatusCode();
            String resultData = apiResponse.getResultData();

            if (HttpStatus.OK.value() == httpStatusCode) {
                log.info("resp:{}", resultData);
            } else {
                throw BizException.error(httpStatusCode, String.format(ApiResult.ERROR.getMsg(), resultData));
            }

        } catch (Exception e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
        }

    }

    public String getGroupName(String gid) {
        LineGroupResponse groupChatSummary = groupAPI.getGroupChatSummary(gid, LineGroupResponse.class);
        return groupChatSummary.getGroupName();
    }

    /**
     * <b>此 API 僅能讓已驗證的 OA 使用</b>
     * <br>
     * 透過 LINE API 取得特定群組中所有成員的 UID。
     * 此方法以遞迴方式呼叫，以處理超過100人的大型群組成員獲取。
     * 如果群組成員數量超過100人，LINE API 將分頁返回結果，並提供一個 nextToken 用於後續的 API 調用。
     * 此方法會遞迴呼叫自己，直到收集完所有成員的 UID 或達到最大呼叫次數限制。
     *
     * @param groupId   群組ID，用於識別需要取成員 UID 的 LINE 群組。
     * @param nextToken 分頁標記，群組超過 100 人會返回的參數，若有 nextToken，則應在 API 請求 URL 中加入此標記<br>
     *                  e.g. {apiUrl}?start={nextToken}。初次呼叫時應為 null 或空字串
     * @param memberIds 用於儲存已取得的群組成員 UID 的集合。初次呼叫時應傳遞一個空集合。
     * @param times     當前的遞迴呼叫次數。用於限制最大呼叫次數，避免無限遞迴。
     * @return 一個包含群組所有成員 UID 的 Set 集合。如果達到呼叫次數限制，則返回當前已收集到的成員 UID。
     * @throws IOException 當 LINE API 調用失敗或返回非預期響應時拋出。
     */
    public Set<String> getGroupMemberIds(String groupId, String nextToken, Set<String> memberIds, int times) throws IOException {
        final int MAX_TIMES = 50;
        times++;

        String url = ApiUtils.getUrl(line.getApiDomain(), String.format(Api.LINE_GROUP_MEMBER_IDS.getUri(), groupId));
        if (nextToken != null) {
            url += "?start=" + nextToken;
        }

        ApiResponse sync = OkHttpUtils.builder()
                .addLineAuthHeader(line.getMessage().getChannelToken())
                .get(url).sync();

        if (sync.getHttpStatusCode() != HttpStatus.OK.value()) {
            throw new IOException("Failed to get group member IDs: " + sync.getResultData());
        }

        ObjectNode jsonNodes = JacksonUtils.decodeObject(sync.getResultData());
        jsonNodes.get("memberIds").forEach(node -> memberIds.add(node.asText()));

        if (jsonNodes.has("next") && times <= MAX_TIMES) {
            return getGroupMemberIds(groupId, jsonNodes.get("next").asText(), memberIds, times);
        }

        return memberIds;
    }

    public Set<String> getGroupAllMemberIds(String groupId) throws IOException {
        return getGroupMemberIds(groupId, null, new HashSet<>(), 0);
    }

    public MessageContentResponse getMessageContent(String messageId) {
        return messageContentAPI.getMessageContent(messageId, MessageContentResponse.class);
    }
}
