package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.services.LineService;
import com.cheng.linegroup.services.ReplyKeywordService;
import com.cheng.linegroup.services.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 當收到 food 或 美食 關鍵字時，自動回覆美食查詢網址
 */
@Component
@RequiredArgsConstructor
public class FoodKeywordReplyBehavior implements TextMessageBehavior {
    private static final String[] KEYWORDS = {"food", "美食"};
    private static final String REPLY_MSG = "https://cheng.tplinkdns.com/apps/YummyQuest\n搜尋餐廳～";

    @Override
    public boolean canHandle(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();
        for (String keyword : KEYWORDS) {
            if (lower.contains(keyword) || text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService) {
        LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                .messages(Collections.singletonList(
                        LineMessage.Message.builder().msg(REPLY_MSG).build())
                ).build();
        lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);
        return true;
    }
}
