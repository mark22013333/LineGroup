package com.cheng.linegroup.api.line;

import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.utils.ApiUtils;
import com.cheng.linegroup.utils.JacksonUtils;
import com.cheng.linegroup.utils.OkHttpUtils;
import com.cheng.linegroup.utils.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2023/12/3 5:39 PM
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class GroupAPI {

    private final Line line;

    public <T> T getGroupChatSummary(String groupId, Class<T> lineGroupResponse) {
        String url = combineParamsUrl(Api.LINE_GROUP_SUMMARY, groupId);
        return handleApiResponse(url, lineGroupResponse);
    }

    public <T> T getGroupChatMemberProfile(String groupId, String userId, Class<T> lineGroupResponse) {
        String url = combineParamsUrl(Api.LINE_GROUP_MEMBER_PROFILE, groupId, userId);
        return handleApiResponse(url, lineGroupResponse);
    }

    private String combineParamsUrl(Api api, Object... param) {
        return ApiUtils.getUrl(line.getApiDomain(), String.format(api.getUri(), param));
    }

    private <T> T handleApiResponse(String url, Class<T> responseType) {
        ApiResponse apiResponse = OkHttpUtils.builder().addLineAuthHeader(line.getMessage().getChannelToken()).get(url).sync();
        int httpStatusCode = apiResponse.getHttpStatusCode();
        String resultData = apiResponse.getResultData();
        if (httpStatusCode == HttpStatus.OK.value()) {
            log.info("resultData:{}", resultData);
            return JacksonUtils.decodeFromJson(resultData, responseType);
        } else {
            throw BizException.error(httpStatusCode, resultData);
        }
    }
}
