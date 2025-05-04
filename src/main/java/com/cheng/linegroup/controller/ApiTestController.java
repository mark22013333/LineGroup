package com.cheng.linegroup.controller;

import com.cheng.linegroup.api.response.MessageContentResponse;
import com.cheng.linegroup.common.R;
import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.common.domain.LineNotify;
import com.cheng.linegroup.dto.LineNotifyMessage;
import com.cheng.linegroup.entity.LineUser;
import com.cheng.linegroup.enums.Api;
import com.cheng.linegroup.enums.MessageType;
import com.cheng.linegroup.exception.BizException;
import com.cheng.linegroup.services.LineNotifyService;
import com.cheng.linegroup.services.LineService;
import com.cheng.linegroup.services.dto.LineMessage;
import com.cheng.linegroup.utils.JacksonUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;

/**
 * @author cheng
 * @since 2023/12/3 2:53 PM
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/test")
public class ApiTestController {

    private final Line line;
    private final LineService lineService;
    private final LineNotify lineNotify;
    private final LineNotifyService lineNotifyService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("r")
    public ResponseEntity<R> testR() {
        LineUser user = new LineUser();
        user.setId(2L);
        user.setNickname("試試水溫");
        redisTemplate.opsForValue().set("user", user);

        LineUser userCache = (LineUser) redisTemplate.opsForValue().get("user");
        log.info("userCache:{}", userCache);
        return ResponseEntity.ok(R.success(userCache));
    }

    @GetMapping("oauth")
    public ResponseEntity<R> oauth() {
        lineNotifyService.notifyOauth();
        return ResponseEntity.ok().build();
    }

    @GetMapping("member/ids")
    public ResponseEntity<R> testMemberIds() {
        String groupId = "Cb8f920d5c155f93d5a44464a5d46439b";
        Set<String> groupAllMemberIds;
        try {
            groupAllMemberIds = lineService.getGroupAllMemberIds(groupId);
        } catch (IOException e) {
            throw BizException.error(e, e.getMessage());
        }

        return ResponseEntity.ok(R.success(groupAllMemberIds));
    }

    @GetMapping("notify")
    public ResponseEntity<R> testNotify() throws MalformedURLException {
        log.info("test/notify");
        lineNotifyService.notifyMessage(
                new LineNotifyMessage()
                        .setToken(lineNotify.getSelfToken())
                        .setMessage("test notify")
                        .setImageUrl(new URL("https://s4.aconvert.com/convert/p3r68-cdx67/abfqv-0f59i.webp"))
        );
        return ResponseEntity.ok(R.success());
    }

    @GetMapping("msg/push")
    public ResponseEntity<R> testMsgPush() {
        log.info("test/msg/push");
        String selfUid = line.getMessage().getSelfUid();

        LineMessage lineMessage = LineMessage.builder()
                .uid(selfUid)
                .messages(Arrays.asList(
                        LineMessage.Message.builder()
                                .msg("嗨")
                                .build(),
                        LineMessage.Message.builder()
                                .msg("測試一下囉")
                                .build(),
                        LineMessage.Message.builder()
                                .type(MessageType.image)
                                .originalContentUrl("https://unsplash.it/800/600?image=1008")
                                .previewImageUrl("https://unsplash.it/800/600?image=1008")
                                .build()
                ))
                .build();

        lineService.CallMessageAPI(lineMessage, Api.LINE_MESSAGE_PUSH);
        return ResponseEntity.ok(R.success());
    }

    @GetMapping("msg/content/{messageId}")
    public void testMsgContent(@PathVariable String messageId, HttpServletResponse response) {
        log.info("test/msg/content/{}", messageId);
        MessageContentResponse result = lineService.getMessageContent(messageId);

        if (result.getContentType() == null) {
            response.setContentType("application/json");
            try (PrintWriter writer = response.getWriter()) {
                String jsonResponse = JacksonUtils.encodeToJson(result);
                writer.write(jsonResponse);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error writing JSON response", e);
            }
        } else {
            response.setContentType(result.getContentType());
            response.setContentLength(result.getBinaryData().length);
            try (OutputStream out = response.getOutputStream()) {
                out.write(result.getBinaryData());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error writing binary response", e);
            }
        }
    }
}
