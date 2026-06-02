package com.eighth.aicodehelper.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiCodeHelper {
    @Autowired
    private ChatModel qwenChatModel;

    private final static String SYSTEM_MSG = """
            你是编程小助手，用于帮助用户解决编程与求职问题，并给出建议，重点关注：
            1.规划清晰的学习路线
            2.给出高频学习路线与学习计划
            """;

    String test(String msg) {
        UserMessage chatMessage = UserMessage.from(msg);
        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MSG);
        ChatResponse response = qwenChatModel.chat(systemMessage,chatMessage);
        AiMessage message = response.aiMessage();
        log.info("Ai消息：" + message.toString());
        return message.text();
    }

    String chatWithMessage(UserMessage userMessage) {
        ChatResponse response = qwenChatModel.chat(userMessage);
        AiMessage message = response.aiMessage();
        log.info("Ai消息：" + message.toString());
        return message.text();
    }
}
