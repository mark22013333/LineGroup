package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.R;
import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.common.domain.LineNotify;
import com.cheng.linegroup.dto.LineNotifyMessage;
import com.cheng.linegroup.service.LineNotifyService;
import com.cheng.linegroup.service.LineService;
import com.cheng.linegroup.service.dto.LineMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

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
                                .build()
                ))
                .build();

        lineService.pushMessage(lineMessage);
        return ResponseEntity.ok(R.success());
    }
}
