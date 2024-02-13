package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import com.cheng.linegroup.events.message.MessageStrategyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2024/2/11 20:06
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventHandler implements EventHandler {

    private final MessageStrategyManager messageStrategyManager;

    @Override
    public void handle(WebhookEvent.Event e) {
        log.info("MessageEventHandler");
        messageStrategyManager.handle(e.getMessage());
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.MESSAGE;
    }
}
