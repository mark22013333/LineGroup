package com.cheng.linegroup.events.message;

import com.cheng.linegroup.dto.WebhookEvent;

/**
 * @author cheng
 * @since 2024/2/11 20:07
 **/
public interface MessageStrategy {
    boolean canHandle(WebhookEvent.Event.Message msg);
    boolean handle(WebhookEvent.Event event);

}
