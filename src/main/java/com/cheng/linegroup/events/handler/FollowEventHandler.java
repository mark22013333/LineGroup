package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2024/2/11 14:00
 **/
@Slf4j
@Component
public class FollowEventHandler implements EventHandler {
    @Override
    public void handle(WebhookEvent.Event e) {
        log.info("FollowEventHandler");
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.FOLLOW;
    }
}
