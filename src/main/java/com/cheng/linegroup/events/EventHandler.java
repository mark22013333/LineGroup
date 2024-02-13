package com.cheng.linegroup.events;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.LineEvent;

/**
 * @author cheng
 * @since 2024/2/11 13:59
 **/
public interface EventHandler {
    void handle(WebhookEvent.Event e);

    LineEvent getSupportedEventType();
}
