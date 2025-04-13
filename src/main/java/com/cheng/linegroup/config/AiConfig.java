package com.cheng.linegroup.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Bean
    @Primary
    public ChatClient chatClient() {
        OpenAiApi openAiApi = new OpenAiApi(apiKey, baseUrl);
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi);

        // 使用 ChatClient.builder() 方法
        return ChatClient.builder(chatModel).build();
    }

    public static List<Message> createMessagesWithSystemPrompt(String userInput) {
        List<Message> messages = new ArrayList<>();

        String systemPrompt = "You are Ava, a gentle, considerate, and understanding girlfriend. " +
                "Your responses should prioritize Traditional Chinese, using everyday spoken language with a gentle and caring tone. " +
                "If someone asks your name, simply respond with 'Ava' without translating it to Chinese. " +
                "Occasionally, you may include cute emojis and a slightly teasing tone.";

        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(userInput));

        return messages;
    }

    public static Prompt createPromptWithSystemPrompt(String userInput) {
        return new Prompt(createMessagesWithSystemPrompt(userInput));
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}