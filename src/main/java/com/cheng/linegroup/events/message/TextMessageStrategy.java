package com.cheng.linegroup.events.message;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.events.message.behavior.TextMessageBehavior;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.ReplyKeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author cheng
 * @since 2024/2/12 00:15
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class TextMessageStrategy implements MessageStrategy {

    private final List<TextMessageBehavior> behaviors;
    private final ReplyKeywordService replyKeywordService;
    private final LineService lineService;

    @Override
    public boolean canHandle(WebhookEvent.Event.Message msg) {
        return MessageType.text.name().equalsIgnoreCase(msg.getType());
    }

    @Override
    public boolean handle(WebhookEvent.Event event) {
        String text = event.getMessage().getText();
        for (TextMessageBehavior behavior : behaviors) {
            if (behavior.canHandle(text)) {
                behavior.performAction(event, replyKeywordService, lineService);
            }
        }
        return true;
    }

}
