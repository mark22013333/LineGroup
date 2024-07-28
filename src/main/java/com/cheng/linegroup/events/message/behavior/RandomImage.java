package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.controller.ImageController;
import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.ReplyKeywordService;
import com.cheng.linegroup.service.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Cheng
 * @since 2024/7/28 02:33
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class RandomImage implements TextMessageBehavior {

    private static final String[] KEYWORDS = {"抽"};

    @Value("${image.domain}")
    private String domain;

    @Override
    public boolean canHandle(String text) {
        return Arrays.stream(KEYWORDS).anyMatch(text::startsWith);
    }

    @Override
    public boolean performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService) {
        List<String> images = ImageController.getIMAGES();
        String userId = event.getSource().getUserId();

        if (images.isEmpty()) {
            log.warn("IMAGES 列表為空，無法選擇隨機圖片");
            LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                    .uid(userId)
                    .messages(Collections.singletonList(
                            LineMessage.Message.builder().msg("IMAGES 列表為空，無法選擇隨機圖片").build()
                    )).build();
            lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);
            return true;
        }

        String imgName = images.get(ThreadLocalRandom.current().nextInt(images.size()));
        imgName = domain.concat(imgName);
        log.info("imgName: {}", imgName);

        LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                .uid(userId)
                .messages(Collections.singletonList(
                        LineMessage.Message.builder()
                                .type(MessageType.image)
                                .originalContentUrl(imgName)
                                .previewImageUrl(imgName)
                                .build()
                )).build();

        lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);

        return true;
    }

}
