package com.cheng.linegroup.service;

import com.cheng.linegroup.common.domain.LineNotify;
import com.cheng.linegroup.dto.LineNotifyMessage;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.utils.ApiUtils;
import com.cheng.linegroup.utils.JacksonUtils;
import com.cheng.linegroup.utils.OkHttpUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * @author cheng
 * @since 2023/12/3 1:47 PM
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LineNotifyService {

    private final LineNotify lineNotify;

    public void notifyMessage(LineNotifyMessage lineMessage) {
        String url = ApiUtils.getUrl(lineNotify.getApiDomain(), Api.LINE_NOTIFY_PUSH);
        ObjectNode params = JacksonUtils.genJsonObject()
                .put("message", lineMessage.getMessage());

        if (lineMessage.getImageUrl() != null) {
            params.put("imageThumbnail", lineMessage.getImageUrl().toString())
                    .put("imageFullsize", lineMessage.getImageUrl().toString());
        }

        OkHttpUtils.builder()
                .addHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", lineNotify.getSelfToken()))
                .addParam(params)
                .post(url, false)
                .sync();
    }
}
