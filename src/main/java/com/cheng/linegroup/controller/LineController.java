package com.cheng.linegroup.controller;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.service.LineNotifyService;
import com.cheng.linegroup.utils.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

/**
 * @author cheng
 * @since 2023/12/3 1:12 AM
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
public class LineController {

    private final LineNotifyService lineNotifyService;

    @PostMapping("webhook")
    public ResponseEntity<Object> webhook(@RequestBody String data) throws ExecutionException, InterruptedException {
        log.info("rawData = {}", data);
        WebhookEvent event = JacksonUtils.decode(data, WebhookEvent.class);
        log.info("event:{}", event);
        Assert.notNull(event, "webhook event can not be null");

        // Method for tasks handled by a thread pool
        lineNotifyService.notifyCentralControlGroup(event);

        return ResponseEntity.ok(event);
    }
}
