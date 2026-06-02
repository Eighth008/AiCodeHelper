package com.eighth.aicodehelper.ai;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class AiCodeHelperTest {
    @Autowired
    AiCodeHelper aiCodeHelper;
    @Test
    void test1() {
        String test = aiCodeHelper.test("你好！");
        log.info(test);
    }

    @Test
    void test2() {
        UserMessage userMessage = UserMessage.from(
                TextContent.from("解释一下这张图片！"),
                ImageContent.from("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png")
        );
        String str = aiCodeHelper.chatWithMessage(userMessage);
        log.info(str);
    }
}