package com.cheng.linegroup.events.message;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2024/2/12 00:15
 **/
@Slf4j
@Component
public class TextMessageStrategy implements MessageStrategy {

    @Override
    public boolean canHandle(WebhookEvent.Event.Message msg) {
        return MessageType.TEXT.name().equalsIgnoreCase(msg.getType());
    }

    @Override
    public boolean handle(WebhookEvent.Event.Message msg) {
        log.info("===>TextMessageStrategy handle");
        return false;
    }
}
