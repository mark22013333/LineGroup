package com.cheng.linegroup.events.message;

import com.cheng.linegroup.dto.WebhookEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author cheng
 * @since 2024/2/11 19:46
 **/
@Slf4j
@Component
public class MessageStrategyManager {

    private final List<MessageStrategy> strategies;

    @Autowired
    public MessageStrategyManager(List<MessageStrategy> strategies) {
        this.strategies = strategies;
    }

    public void handle(WebhookEvent.Event.Message message) {
        for (MessageStrategy strategy : strategies) {
            if (strategy.canHandle(message)) {
                strategy.handle(message);
                break;
            }
        }
    }
}
