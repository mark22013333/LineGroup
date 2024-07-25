package com.cheng.linegroup.controller;

import com.cheng.linegroup.common.domain.Line;
import com.cheng.linegroup.common.domain.LineHeader;
import com.cheng.linegroup.dto.WebhookEvent;
import com.cheng.linegroup.enums.LineEvent;
import com.cheng.linegroup.events.EventHandler;
import com.cheng.linegroup.events.EventHandlerRegistry;
import com.cheng.linegroup.service.LineNotifyService;
import com.cheng.linegroup.utils.JacksonUtils;
import com.cheng.linegroup.utils.TraceUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author cheng
 * @since 2023/12/3 1:12 AM
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
public class LineController {

    private final Line line;
    private final LineHeader lineHeader;
    private final EventHandlerRegistry eventHandlerRegistry;
    private final LineNotifyService lineNotifyService;
    private final ThreadPoolTaskExecutor executor;

    @PostMapping("webhook")
    public ResponseEntity<Object> webhook(HttpServletRequest request, @RequestBody String data) {
        log.info("webhook.rawData:{}", data);

        if (verifySignature(request, data)) {
            WebhookEvent event = JacksonUtils.decodeFromJson(data, WebhookEvent.class);
            log.info("event:{}", event);
            Assert.notNull(event, "webhook event can not be null");

            String traceId = TraceUtils.getTraceId();
            for (WebhookEvent.Event e : event.getEvents()) {
                e.setTraceId(traceId);
                EventHandler eventHandler = eventHandlerRegistry.getEventHandler(LineEvent.getEvent(e.getType()));
                executor.execute(() -> eventHandler.handle(e));
            }

            // Method for tasks handled by a thread pool
//            lineNotifyService.notifyCentralControlGroup(event);
        } else {
            String msg = "===> Signature verification failed";
            log.warn(msg);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(msg);
        }

        return ResponseEntity.ok().build();
    }

    private boolean verifySignature(HttpServletRequest request, String data) {
        String requestSignature = request.getHeader(lineHeader.getSignature());
        try {
            String channelSecret = line.getMessage().getChannelSecret();
            SecretKeySpec key = new SecretKeySpec(channelSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] source = data.getBytes(StandardCharsets.UTF_8);
            byte[] calculatedSignature = mac.doFinal(source);
            byte[] receivedSignature = Base64.decodeBase64(requestSignature);

            // Compare x-line-signature request header string and the signature
            return MessageDigest.isEqual(calculatedSignature, receivedSignature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("===> Signature verification error: {}", e.getMessage());
            return false;
        }
    }

}
