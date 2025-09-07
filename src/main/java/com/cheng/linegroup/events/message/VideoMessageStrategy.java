package com.cheng.linegroup.events.message;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.MessageEventRecord;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.services.MessageEventRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Vincent
 * @since 2024/2/25 18:17
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoMessageStrategy implements MessageStrategy {

    private final MessageEventRecordService messageEventRecordService;

    @Override
    public boolean canHandle(WebhookEvent.Event e) {
        return MessageType.video.name().equalsIgnoreCase(e.getMessage().getType());
    }

    @Override
    public boolean handle(WebhookEvent.Event event) {
        log.info("Video handle");
        MessageEventRecord messageEventRecord = MessageEventRecord.builder()
                .messageId(event.getMessage().getId())
                .messageType(event.getMessage().getType())
                .uid(event.getSource().getUserId())
                .build();
        messageEventRecordService.save(messageEventRecord);
        return false;
    }
}
