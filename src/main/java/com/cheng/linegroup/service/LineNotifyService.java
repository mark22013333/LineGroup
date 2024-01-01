package com.cheng.linegroup.service;

import com.cheng.linegroup.annotation.LogApi;
import com.cheng.linegroup.api.line.GroupAPI;
import com.cheng.linegroup.api.response.LineGroupResponse;
import com.cheng.linegroup.common.domain.LineNotify;
import com.cheng.linegroup.dto.LineNotifyMessage;
import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.utils.ApiUtils;
import com.cheng.linegroup.utils.JacksonUtils;
import com.cheng.linegroup.utils.OkHttpUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author cheng
 * @since 2023/12/3 1:47 PM
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class LineNotifyService {

    private final GroupAPI groupAPI;
    private final LineNotify lineNotify;

    @Async
    @LogApi(description = "通知中控群組")
    public void notifyCentralControlGroup(WebhookEvent event) {
        List<WebhookEvent.Event> events = event.getEvents();
        if (events.isEmpty()){
            log.info("events:{}", events);
            log.info("===> events is empty, skip");
            return;
        }
        WebhookEvent.Event e = events.get(0);
        WebhookEvent.Event.Source source = e.getSource();
        String groupId = source.getGroupId();
        if (Objects.isNull(groupId)) {
            log.info("===> group is null, skip");
            return;
        }
        WebhookEvent.Event.Message message = e.getMessage();
        if (Objects.isNull(message)){
            log.info("===> message is null, skip");
            return;
        }
        String text = message.getText();
        String userId = source.getUserId();
        String groupToken = lineNotify.getCentralControlGroupToken();

        // TODO: 要改成先從DB撈，沒有再call group api取得群組資訊
        LineGroupResponse groupChatSummary = groupAPI.getGroupChatSummary(groupId, LineGroupResponse.class);
        log.info("groupSummary = {}", groupChatSummary);
        LineGroupResponse memberProfile = groupAPI.getGroupChatMemberProfile(groupId, userId, LineGroupResponse.class);
        log.info("memberProfile = {}", memberProfile);

        String groupName = groupChatSummary.getGroupName();
        String displayName = memberProfile.getDisplayName();
        // TODO: 目前只有純文字，後續可新增貼圖
        String content = String.format("由群組『%s』的 %s 說：%s", groupName, displayName, text);
        notifyMessage(new LineNotifyMessage().setToken(groupToken).setMessage(content));
    }

    public void notifyMessage(LineNotifyMessage lineMessage) {
        String url = ApiUtils.getUrl(lineNotify.getApiDomain(), Api.LINE_NOTIFY_PUSH);
        ObjectNode params = JacksonUtils.genJsonObject()
                .put("message", lineMessage.getMessage());

        if (lineMessage.getImageUrl() != null) {
            params.put("imageThumbnail", lineMessage.getImageUrl().toString())
                    .put("imageFullsize", lineMessage.getImageUrl().toString());
        }

        OkHttpUtils.builder()
                .addHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", lineMessage.getToken()))
                .addParam(params)
                .post(url, false)
                .sync();
    }

}
