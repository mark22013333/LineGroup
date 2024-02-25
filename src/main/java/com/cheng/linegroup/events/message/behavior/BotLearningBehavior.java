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
 * @since 2024/2/25 22:57
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class BotLearningBehavior implements TextMessageBehavior {

    @Override
    public boolean canHandle(String text) {
        return text.startsWith("欸唐董看到");
    }

    @Override
    public void performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService) {
        log.info("===>TextMessageStrategy handle");
        String userId = event.getSource().getUserId();
        String groupId = event.getSource().getGroupId();
        String text = event.getMessage().getText();

        try {
            String replace = text.replaceFirst("欸唐董看到", "").replace("就回", ",");
            log.info("===>replace:{}", replace);
            String[] keywordSplitString = replace.split(",");

            boolean isDataExist = saveReplyKeyword(userId, groupId, keywordSplitString, replyKeywordService);
            String msg = isDataExist ? "好我學到了" : "我已經會了！";
            LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                    .messages(Collections.singletonList(
                            LineMessage.Message.builder().msg(msg).build())
                    ).build();
            lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);

        } catch (Exception e) {
            log.error("Error processing the message", e);
        }

    }

    private boolean saveReplyKeyword(String userId, String groupId, String[] keywordSplitString, ReplyKeywordService replyKeywordService) {
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
