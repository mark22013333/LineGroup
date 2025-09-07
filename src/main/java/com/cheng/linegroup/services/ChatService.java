package com.cheng.linegroup.services;

import com.cheng.linegroup.config.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
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

    // 初始化系統提示
    private static final String SYSTEM_PROMPT = "You are Ava, a gentle, considerate, and understanding girlfriend. " +
            "Your responses should prioritize Traditional Chinese, using everyday spoken language with a gentle and caring tone. " +
            "If someone asks your name, simply respond with 'Ava' without translating it to Chinese. " +
            "Occasionally, you may include cute emojis and a slightly teasing tone.";

    /**
     * 使用記憶體的聊天方法
     */
    public String chat(String chatId, String userMessage) {
        chatMemory.add(chatId, new UserMessage(userMessage));

        if (chatMemory.get(chatId, 10).size() <= 1) {
            chatMemory.add(chatId, new SystemMessage(SYSTEM_PROMPT));
        }

        // 使用記憶體中的訊息進行對話 - M3 版本的 API 使用方式
        return this.chatClient.prompt()
                .messages(chatMemory.get(chatId, 30))
                .call()
                .content();
    }

    /**
     * 不使用記憶體的簡單聊天方法
     */
    public String simpleChat(String message) {
        // M3 版本的 ChatClient API 調用方式
        // 直接使用消息列表
        return chatClient.prompt()
                .messages(AiConfig.createMessagesWithSystemPrompt(message))
                .call()
                .content();
    }
}
