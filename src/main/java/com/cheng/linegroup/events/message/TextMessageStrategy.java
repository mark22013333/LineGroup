package com.cheng.linegroup.events.message;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.ReplyKeyword;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.ReplyKeywordService;
import com.cheng.linegroup.service.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * @author cheng
 * @since 2024/2/12 00:15
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class TextMessageStrategy implements MessageStrategy {

    private final ReplyKeywordService replyKeywordService;
    private final LineService lineService;

    @Override
    public boolean canHandle(WebhookEvent.Event.Message msg) {
        return MessageType.TEXT.name().equalsIgnoreCase(msg.getType());
    }

    @Override
    public boolean handle(WebhookEvent.Event event) {
        log.info("===>TextMessageStrategy handle");
        String userId = event.getSource().getUserId();
        String groupId = event.getSource().getGroupId();
        String text = event.getMessage().getText();

        try {
            if (text.startsWith("欸唐董看到")) {
                String replace = text.replaceFirst("欸唐董看到", "").replace("就回", ",");
                log.info("===>replace:{}", replace);
                String[] keywordSplitString = replace.split(",");

                boolean isDataExist = saveReplyKeyword(userId, groupId, keywordSplitString);
                String msg = isDataExist ? "好我學到了" : "我已經會了！";
                LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                        .messages(Collections.singletonList(
                                LineMessage.Message.builder().msg(msg).build())
                        ).build();
                lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);
            } else {
                ReplyKeyword replyKeywordByKeyword = replyKeywordService.getReplyKeywordByKeywordAndUidAndGid(text, userId, groupId);
                if (replyKeywordByKeyword != null) {
                    String reply = replyKeywordByKeyword.getReply();
                    LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                            .messages(Collections.singletonList(
                                    LineMessage.Message.builder().msg(reply).build())
                            ).build();
                    lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);
                }
            }

        } catch (Exception e) {
            log.error("Error processing the message", e);
            return false;
        }

        return true;
    }

    private boolean saveReplyKeyword(String userId, String groupId, String[] keywordSplitString) {
        String keyword = keywordSplitString[0];
        ReplyKeyword replyKeywordByKeyword = replyKeywordService.getReplyKeywordByKeywordAndUidAndGid(keyword, userId, groupId);
        if (replyKeywordByKeyword == null) {
            replyKeywordService.save(ReplyKeyword.builder()
                    .uid(userId)
                    .gid(groupId != null ? groupId : "personal")
                    .keyword(keyword)
                    .reply(keywordSplitString[1])
                    .build());
            return true;
        }
        return false;
    }
}
