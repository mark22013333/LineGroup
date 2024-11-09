package com.cheng.linegroup.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

/**
 * @author Cheng
 * @since 2024/11/9 20:38
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory = new InMemoryChatMemory();

    public String chat(String chatId, String userMessage) {
        chatMemory.add(chatId, new UserMessage(userMessage));
        return this.chatClient.prompt()
                // 透過 chatId 從記憶體取出近 30 筆的對話紀錄
                .messages(chatMemory.get(chatId, 30))
                .call()
                .content();
    }

}
