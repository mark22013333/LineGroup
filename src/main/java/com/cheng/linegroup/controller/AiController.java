package com.cheng.linegroup.controller;

import com.cheng.linegroup.config.AiConfig;
import com.cheng.linegroup.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
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
        // 使用 ChatModel (不同於 ChatClient) 調用
        Prompt prompt = AiConfig.createPromptWithSystemPrompt(message);
        String response = chatModel.call(prompt)
                .getResult().getOutput().getContent();
        log.info("response: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("simplechat")
    public ResponseEntity<String> simpleChat(@RequestParam(value = "message") String message) {
        log.info("簡單聊天，訊息: {}", message);
        String response = chatService.simpleChat(message);
        log.info("回應: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("memchat")
    public String chat(@RequestParam String chatId, @RequestParam String prompt) {
        log.info("記憶聊天，chatId: {}, 訊息: {}", chatId, prompt);
        String response = chatService.chat(chatId, prompt);
        log.info("回應: {}", response);
        return response;
    }
}
