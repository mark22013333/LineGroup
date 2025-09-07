package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.enums.BehaviorKeyword;
import com.cheng.linegroup.services.ChatService;
import com.cheng.linegroup.services.LineService;
import com.cheng.linegroup.services.ReplyKeywordService;
import com.cheng.linegroup.services.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Cheng
 * @since 2024/8/13 23:32
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class AiChatBehavior implements TextMessageBehavior {

    private static final String[] KEYWORDS = BehaviorKeyword.AI_CHAT.getKeywords();

    private final ChatModel chatModel;
    private final ChatService chatService;

    @Override
    public boolean canHandle(String text) {
        return Arrays.stream(KEYWORDS).anyMatch(text::startsWith);
    }

    @Override
    public boolean performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService) {
        log.info("Performing AI chat behavior");
        String userId = event.getSource().getUserId();
        String groupId = event.getSource().getGroupId();
        log.info("User ID: {}", userId);
        log.info("Group ID: {}", groupId);
        String text = event.getMessage().getText();
        String chatId = String.format("%s-%s", groupId, userId);
        String aiResponse = chatService.chat(chatId, text);

//        String aiResponse = chatModel.call(new Prompt(text,
//                OpenAiChatOptions.builder()
//                        .withFunction("CurrentDateTime")
//                        .build())
//        ).getResult().getOutput().getContent();

        LineMessage lineMessage = LineMessage.builder()
                .replyToken(event.getReplyToken())
                .messages(Collections.singletonList(LineMessage.Message.builder().msg(aiResponse).build()))
                .build();
        lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);

        return true;
    }
}
