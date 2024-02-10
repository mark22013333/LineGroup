package com.cheng.linegroup.controller;

import com.cheng.linegroup.dto.LineNotifyMessage;
import com.cheng.linegroup.dto.LineNotifyOauth;
import com.cheng.linegroup.service.LineNotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author cheng
 * @since 2024/1/4 14:40
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("notify")
public class LineNotifyController {

    private final LineNotifyService lineNotifyService;

    @PostMapping("authCode")
    public ResponseEntity<?> callback(LineNotifyOauth lineNotifyOauth) throws MalformedURLException {
        log.info("lineNotifyOauth:{}", lineNotifyOauth);
        String s = lineNotifyService.notifyToken(lineNotifyOauth);
        log.info("====>s:{}", s);
        lineNotifyService.notifyMessage(
                new LineNotifyMessage()
                        .setToken(s)
                        .setMessage("test notify")
                        .setImageUrl(new URL("https://s4.aconvert.com/convert/p3r68-cdx67/abfqv-0f59i.webp"))
        );
        return ResponseEntity.ok("success");
    }
}
