package com.eighth.aicodehelper.ai;

import com.eighth.aicodehelper.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.util.List;

@InputGuardrails({SafeInputGuardrail.class})
public interface AiCodeHelperService {
    @SystemMessage(fromResource = "SystemProperties.txt")
    public String chatWithMemory(@MemoryId int memoryId,@UserMessage String msg);

    @SystemMessage(fromResource = "SystemProperties.txt")
    Report report(String msg);

    @SystemMessage(fromResource = "SystemProperties.txt")
    String chat(String msg);

    @SystemMessage(fromResource = "SystemProperties.txt")
    public Flux<String> streamChat(@MemoryId int memoryId, @UserMessage String msg);

    record Report(String name, List<String> suggestionList){}
}
