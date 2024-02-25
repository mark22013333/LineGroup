package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.ReplyKeywordService;

/**
 * @author cheng
 * @since 2024/2/25 22:55
 **/
public interface TextMessageBehavior {

    boolean canHandle(String text);

    void performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService);
}
