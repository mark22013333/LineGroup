package com.cheng.linegroup.events.message;

import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.ReplyKeyword;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.service.ReplyKeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2024/2/12 00:15
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class TextMessageStrategy implements MessageStrategy {

    private final Line line;
    private final ReplyKeywordService replyKeywordService;

    @Override
    public boolean canHandle(WebhookEvent.Event.Message msg) {
        return MessageType.TEXT.name().equalsIgnoreCase(msg.getType());
    }

    @Override
    public boolean handle(WebhookEvent.Event event) {
        log.info("===>TextMessageStrategy handle");
        String userId = event.getSource().getUserId();
        String text = event.getMessage().getText();

        if (text.startsWith("欸唐董看到")) {
            String replace = text.replaceFirst("欸唐董看到", "").replace("就回", ",");
            log.info("===>replace:{}", replace);
            String[] keywordSplitString = replace.split(",");
            replyKeywordService.save(ReplyKeyword.builder()
                    .uid(userId)
                    .keyword(keywordSplitString[0])
                    .reply(keywordSplitString[1])
                    .build());
        }


        return false;
    }
}
