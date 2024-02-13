package com.cheng.linegroup.events.handler;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2024/2/11 14:06
 **/
@Slf4j
@Component
public class MemberLeftEventHandler implements EventHandler {
    @Override
    public void handle(WebhookEvent.Event e) {
        log.info("member left event handler");
    }

    @Override
    public LineEvent getSupportedEventType() {
        return LineEvent.MEMBER_LEFT;
    }
}
