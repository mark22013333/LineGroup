package com.cheng.linegroup.controller;

import com.cheng.linegroup.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Cheng
 * @since 2024/11/2 23:35
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("ai")
public class AiController {
    
    private final ChatModel chatModel;
    private final ChatService chatService;


    @GetMapping("chat")
    public ResponseEntity<String> generate(@RequestParam(value = "message") String message) {
        log.info("message: {}", message);
        String call = chatModel.call(message);
        log.info("call: {}", call);
        return ResponseEntity.ok(call);
    }

    @GetMapping("memchat")
    public String chat(@RequestParam String chatId, @RequestParam String prompt) {
        return chatService.chat(chatId, prompt);
    }
}
