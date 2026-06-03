package com.eighth.aicodehelper.ai;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class AiCodeHelperServiceFactory {
    @Autowired
    private ChatModel qwenChatModel;

    @Resource
    private StreamingChatModel qwenStreamingChatModel;

    @Resource
    ContentRetriever contentRetriever;

    @Resource
    McpToolProvider toolProvider;

    @Bean
    AiCodeHelperService aiServiceFactory() {
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        AiCodeHelperService aiServices = AiServices.builder(AiCodeHelperService.class)
                .chatModel(qwenChatModel)
                .streamingChatModel(qwenStreamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(contentRetriever)
                .toolProvider(toolProvider)
                .build();
        return aiServices;
    }
}
