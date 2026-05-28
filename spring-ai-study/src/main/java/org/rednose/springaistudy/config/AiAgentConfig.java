package org.rednose.springaistudy.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import org.rednose.springaistudy.tool.*;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lixuefan
 * @since 2026/5/28
 */

@Configuration
public class AiAgentConfig {
    @Value("${spring.ai.minimax.api-key}")
    private String apiKey;

    @Bean
    public WeatherAgent weatherAgent(){

        String SYSTEM_PROMPT = """  
            作为一个表述简单直接的天气助手，需要根据用户提供的城市信息，  
            准确获取并简要说明该城市当前的天气情况，包括温度、天气状况（晴、阴、雨、雪等），            同时提供简洁实用的穿着建议，帮助用户根据天气情况选择合适的衣物。  
            """ ;

        MiniMaxApi miniMaxApi = new MiniMaxApi("sk-cp-CCBCL5WcQJwSADaMWxk7N359VVZnfRmWCG3-dNCK-6YHdqNK1bmHBRTXJ-_z21ahfLvNvBxWZHdiDzb-cqQ0mRFj-toncnftHSZceDvXKxXgQyojA3k9fzY");
        MiniMaxChatOptions options = MiniMaxChatOptions.builder()
                .model("MiniMax-M2.7")
                .build();
        MiniMaxChatModel miniMaxChatModel = new MiniMaxChatModel(miniMaxApi,options);

        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather", new WeatherTool())
                .description("获取某座城市天气")
                .inputType(WeatherParam.class)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("天气助手")
                .model(miniMaxChatModel)
                .tools(weatherTool)
                .systemPrompt(SYSTEM_PROMPT)
                .hooks(new MessageTrimmingHook())
                .saver(new MemorySaver())
                .outputType(WeatherResponse.class)
                .build();

        WeatherAgent weatherAgent = new WeatherAgent();
        weatherAgent.setAgent(agent);
        return weatherAgent;


    }








}
