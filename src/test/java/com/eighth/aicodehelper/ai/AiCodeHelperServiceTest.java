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
        String msg = aiCodeHelperService.chat(userId,"Hello!我是Niko!");
        log.info(msg);
        msg = aiCodeHelperService.chat(userId,"我叫什么？");
        log.info(msg);
        msg = aiCodeHelperService.chat(1,"我叫什么？");
        log.info(msg);
    }

    @Test
    void chatWithStruct() {
        AiCodeHelperService.Report report = aiCodeHelperService.report("Hello!我是Niko!");
        System.out.println(report);
    }
}