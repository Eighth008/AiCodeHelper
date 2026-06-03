package com.eighth.aicodehelper.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;

import java.util.Set;

public class SafeInputGuardrail implements InputGuardrail {
    private static final Set<String> SENSITIVE_WORD = Set.of("kill","evil");

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String lowerCase = userMessage.singleText().toLowerCase();
        String[] split = lowerCase.split("\\W+");
        for (String word : split) {
            if(SENSITIVE_WORD.contains(word)){
                return failure("Sensitive word detected:"+word);
            }
        }
        return success();
    }
}
