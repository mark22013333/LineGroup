package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.ReplyKeyword;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.ReplyKeywordService;
import com.cheng.linegroup.service.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author cheng
 * @since 2024/2/25 23:05
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultReplyBehavior implements TextMessageBehavior {

    @Override
    public boolean canHandle(String text) {
        return true;
    }

    @Override
    public boolean performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService) {
        String userId = event.getSource().getUserId();
        String groupId = event.getSource().getGroupId();
        String text = event.getMessage().getText();

        ReplyKeyword replyKeywordByKeyword = replyKeywordService.getReplyKeywordByKeywordAndUidAndGid(text, userId, groupId);
        if (replyKeywordByKeyword != null) {
            String reply = replyKeywordByKeyword.getReply();
            LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                    .messages(Collections.singletonList(
                            LineMessage.Message.builder().msg(reply).build())
                    ).build();
            lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);
        } else {
            return false;
        }

        return true;
    }


}
