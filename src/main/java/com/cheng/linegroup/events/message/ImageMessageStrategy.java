package com.cheng.linegroup.events.message;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.MessageEventRecord;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.service.MessageEventRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cheng
 * @since 2024/2/12 00:17
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageMessageStrategy implements MessageStrategy {

    private final MessageEventRecordService messageEventRecordService;

    @Override
    public boolean canHandle(WebhookEvent.Event e) {
        return MessageType.image.name().equalsIgnoreCase(e.getMessage().getType());
    }

    @Override
    public boolean handle(WebhookEvent.Event event) {
        log.info("Image handle");
        MessageEventRecord messageEventRecord = MessageEventRecord.builder()
                .messageId(event.getMessage().getId())
                .messageType(event.getMessage().getType())
                .uid(event.getSource().getUserId())
                .build();
        messageEventRecordService.save(messageEventRecord);
        return false;
    }
}
