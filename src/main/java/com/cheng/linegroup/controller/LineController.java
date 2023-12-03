package com.cheng.linegroup.controller;

import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.utils.JacksonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cheng
 * @since 2023/12/3 1:12 AM
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
public class LineController {


    @PostMapping("webhook")
    public ResponseEntity<Object> webhook(@RequestBody String data) {
        log.info("rawData = {}", data);
        WebhookEvent event = JacksonUtils.decode(data, WebhookEvent.class);
        log.info("event:{}", event);
        return ResponseEntity.ok(event);
    }
}
