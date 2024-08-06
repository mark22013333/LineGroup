package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.common.contants.RedisPrefix;
import com.cheng.linegroup.controller.ImageController;
import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.service.ImageSentRecordsService;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.ReplyKeywordService;
import com.cheng.linegroup.service.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
    @Value("${image.subscript-url}")
    private String subscriptUrl;

    private final ImageSentRecordsService imageSentRecordsService;

    @Override
    public boolean canHandle(String text) {
        return Arrays.stream(KEYWORDS).anyMatch(text::startsWith);
    }

    @Override
    public boolean performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService) {
        List<String> images = ImageController.getIMAGES();
        String userId = event.getSource().getUserId();
        String groupId = event.getSource().getGroupId() == null ? RedisPrefix.PERSONAL : event.getSource().getGroupId();

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

        // 紀錄推送圖片的次數，超過五次則推送訂閱連結
        int sentCount;
        if (RedisPrefix.PERSONAL.equals(groupId)) {
            sentCount = imageSentRecordsService.countByGroupIdAndUid(groupId, userId);
        } else {
            sentCount = imageSentRecordsService.countByGroupId(groupId);
        }

        if (sentCount > 5) {
            LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                    .uid(userId)
                    .messages(Collections.singletonList(
                            LineMessage.Message.builder().msg("哎呀...我沒有照片可以發了，用這個連結就可以看更多了～\n".concat(subscriptUrl)).build()
                    )).build();
            lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);
            return true;
        }

        try {
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

            imageSentRecordsService.saveImageSentRecords(userId, groupId, imgName);
        } catch (Exception e) {
            log.error("ERR:{}", ExceptionUtils.getStackTrace(e));
        }


        return true;
    }

}
