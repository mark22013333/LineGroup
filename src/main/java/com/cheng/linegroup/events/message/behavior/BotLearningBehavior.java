package com.cheng.linegroup.events.message.behavior;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.entity.ReplyKeyword;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.ReplyKeywordService;
import com.cheng.linegroup.service.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author cheng
 * @since 2024/2/25 22:57
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class BotLearningBehavior implements TextMessageBehavior {

    private static final String[] KEYWORDS = {"欸唐董看到", "唐董", "唐懂", "唐墥", "唐"};

    private static final String[] SPLIT_KEYWORDS = {"就回", "就說", ":", "："};

    @Override
    public boolean canHandle(String text) {
        return Arrays.stream(KEYWORDS).anyMatch(text::startsWith);
    }

    @Override
    public void performAction(WebhookEvent.Event event, ReplyKeywordService replyKeywordService, LineService lineService) {
        log.info("===>TextMessageStrategy handle");
        String userId = event.getSource().getUserId();
        String groupId = event.getSource().getGroupId();
        String text = event.getMessage().getText();

        try {
            String s = removeKeywords(text).trim();

            String replace = Arrays.stream(SPLIT_KEYWORDS)
                    .reduce(s, (result, target) -> result.replace(target, ","));

            log.info("===>replace:{}", replace);

            String[] keywordSplitString = replace.split(",");

            boolean isDataExist = saveReplyKeyword(userId, groupId, keywordSplitString, replyKeywordService);
            String msg = isDataExist ? "好我學到了" : "我已經會了！";
            LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                    .messages(Collections.singletonList(
                            LineMessage.Message.builder().msg(msg).build())
                    ).build();
            lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);

        } catch (BizException b) {
            log.error("Error processing the message{}", b.getMessage());
            LineMessage lineMessage = LineMessage.builder().replyToken(event.getReplyToken())
                    .messages(Collections.singletonList(
                            LineMessage.Message.builder().msg(b.getMessage()).build())
                    ).build();
            lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_REPLY);
        } catch (Exception e) {
            log.error("Error processing the message", e);
        }

    }

    private String removeKeywords(String text) {
        return Arrays.stream(KEYWORDS)
                .filter(text::startsWith)
                .findFirst()
                .map(k -> text.replaceFirst(k.trim(), ""))
                .orElse(text);
    }

    private boolean saveReplyKeyword(String userId, String groupId, String[] keywordSplitString, ReplyKeywordService replyKeywordService) {
        if (keywordSplitString.length < 2) {
            throw BizException.error("""
                    格式錯誤，請用以下範例讓唐董學習
                    KEYWORDS = {"欸唐董看到", "唐董", "唐懂", "唐墥", "唐"}
                    SPLIT_KEYWORDS = {"就回", "就說", ":", "："};
                    例如：欸唐董看到A就回B、唐董A就說B、唐A:B、唐A：B""");
        }
        String keyword = keywordSplitString[0];
        ReplyKeyword replyKeywordByKeyword = replyKeywordService.getReplyKeywordByKeywordAndUidAndGid(keyword, userId, groupId);
        if (replyKeywordByKeyword == null) {
            replyKeywordService.save(ReplyKeyword.builder()
                    .uid(userId)
                    .gid(groupId != null ? groupId : "personal")
                    .keyword(keyword)
                    .reply(keywordSplitString[1])
                    .build());
            return true;
        }
        return false;
    }
}
