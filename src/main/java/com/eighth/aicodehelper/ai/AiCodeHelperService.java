package com.eighth.aicodehelper.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

//@AiService
public interface AiCodeHelperService {
    @SystemMessage(fromResource = "SystemProperties.txt")
    public String chat(@MemoryId int memoryId,@UserMessage String msg);

    @SystemMessage(fromResource = "SystemProperties.txt")
    Report report(String msg);

    record Report(String name, List<String> suggestionList){}
}
