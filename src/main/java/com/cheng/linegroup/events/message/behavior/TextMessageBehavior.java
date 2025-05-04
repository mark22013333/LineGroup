package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.services.LineService;
import com.cheng.linegroup.services.ReplyKeywordService;

/**
 * @author cheng
 * @since 2024/2/25 22:55
 **/
public interface TextMessageBehavior {

    /**
     * 是否要處理此事件
     *
     * @param text 從 LINE BOT 收到的事件訊息，通常是使用者輸入的文字
     * @return true: 處理此事件，false: 不處理此事件
     */
    boolean canHandle(String text);

    /**
     * 是否中斷後續的行為
     *
     * @param event               WebhookEvent
     * @param replyKeywordService 回覆關鍵字的相關服務
     * @param lineService         有關LINE推播的API
     * @return true: 中斷後續的行為，false: 不中斷後續的行為
     */
    boolean performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService);

}
