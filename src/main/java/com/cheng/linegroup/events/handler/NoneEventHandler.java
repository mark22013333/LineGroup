package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cheng
 * @since 2024/2/11 19:47
 **/
@Slf4j
public class NoneEventHandler implements EventHandler {
    @Override
    public void handle(WebhookEvent.Event e) {
        log.warn("NoneEventHandler...");
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.NONE;
    }
}
