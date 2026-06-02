package com.eighth.aicodehelper.ai;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.spring.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AiCodeHelperServiceFactory {
    @Autowired
    private ChatModel qwenChatModel;

    @Bean
    AiCodeHelperService aiServiceFactory() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        AiCodeHelperService aiServices = AiServices.builder(AiCodeHelperService.class)
                .chatModel(qwenChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();
        return aiServices;
    }
}
