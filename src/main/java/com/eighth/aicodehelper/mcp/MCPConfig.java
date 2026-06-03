package com.eighth.aicodehelper.mcp;

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.StreamableHttpMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MCPConfig {

    @Value("${tongyi-model.api-key}")
    private String apiKey;

    @Bean
    public McpToolProvider toolProvider(){
        Map<String,String> headers = new HashMap<>();
        headers.put("Authorization","Bearer " + apiKey);
        
        McpTransport transport = new StreamableHttpMcpTransport.Builder()
                .url("https://dashscope.aliyuncs.com/api/v1/mcps/WebSearch/mcp")
                .customHeaders(headers)
                .logRequests(true) // 打印请求
                .logResponses(true) // 打印响应
                .build();
        McpClient mcpClient = new DefaultMcpClient.Builder()
                .key("MyMCPClient")
                .transport(transport)
                .build();
        McpToolProvider toolProvider = McpToolProvider.builder()
                .mcpClients(mcpClient)
                .build();
        return toolProvider;
    }
}
