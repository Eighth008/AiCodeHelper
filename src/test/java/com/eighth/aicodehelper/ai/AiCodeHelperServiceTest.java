package com.eighth.aicodehelper.ai;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class AiCodeHelperServiceTest {
    @Autowired
    AiCodeHelperService aiCodeHelperService;
    @Test
    void chat() {
        int userId = 3;
        String msg = aiCodeHelperService.chatWithMemory(userId,"Hello!我是Niko!");
        log.info(msg);
        msg = aiCodeHelperService.chatWithMemory(userId,"我叫什么？");
        log.info(msg);
        msg = aiCodeHelperService.chatWithMemory(1,"我叫什么？");
        log.info(msg);
    }

    @Test
    void chatWithStructOutPut() {
        AiCodeHelperService.Report report = aiCodeHelperService.report("Hello!我是Niko!");
        System.out.println(report);
    }

    @Test
    void chatWithRag() {
        String chat = aiCodeHelperService.chat("Java面试题是啥？");
        System.out.println(chat);
    }
}