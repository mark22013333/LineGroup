package com.cheng.linegroup.service;

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

/**
 * @author cheng
 * @since 2023/12/3 3:20 PM
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LineService {

    private final Line line;

    public void pushMessage(LineMessage lineMessage) {
        String channelToken = line.getMessage().getChannelToken();

        try {
            String url = ApiUtils.getUrl(line.getApiDomain(), Api.LINE_MESSAGE_PUSH);
            String encode = JacksonUtils.encode(lineMessage);
            ObjectNode jsonNodes = JacksonUtils.toObjectNode(encode);

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
}
