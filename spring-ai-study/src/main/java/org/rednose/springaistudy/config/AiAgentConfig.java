package org.rednose.springaistudy.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.skills.SkillsAgentHook;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.skills.registry.SkillRegistry;
import com.alibaba.cloud.ai.graph.skills.registry.classpath.ClasspathSkillRegistry;
import org.rednose.springaistudy.tool.*;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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
            准确获取并简要说明该城市当前的天气情况，包括温度、天气状况（晴、阴、雨、雪等），
            同时提供简洁实用的穿着建议，帮助用户根据天气情况选择合适的衣物。

            如果用户没有提供具体城市，或者提到"本地"、"当地"、"这里"等模糊表述，
            需要先通过 userId 调用 get_user_city 工具获取用户所在城市，
            再用该城市名称调用 get_weather 查询天气。
            """ ;

        MiniMaxApi miniMaxApi = new MiniMaxApi(apiKey);
        MiniMaxChatOptions options = MiniMaxChatOptions.builder()
                .model("MiniMax-M2.7")
                .build();
        MiniMaxChatModel miniMaxChatModel = new MiniMaxChatModel(miniMaxApi,options);


        ToolCallback weatherTool = FunctionToolCallback.builder("get_weather", new WeatherTool())
                .description("获取某座城市天气")
                .inputType(WeatherParam.class)
                .build();

        ToolCallback userLocationTool = FunctionToolCallback.builder("get_user_city", new UserLocationTool())
                .description("获取用户对应的城市地址，通过userId获取用户所在城市")
                .inputType(UserLocationParam.class)
                .build();


        SkillRegistry registry = ClasspathSkillRegistry.builder()
                .classpathPath("skills")
                .build();

        SkillsAgentHook skillsHook = SkillsAgentHook.builder()
                .skillRegistry(registry)
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("天气助手")
                .model(miniMaxChatModel)
                .hooks(new MessageTrimmingHook(),
                        new RAGAgentHook(SpringUtils.getBean(VectorStore.class)),
                        skillsHook)
                .tools(List.of(weatherTool,userLocationTool))
                .systemPrompt(SYSTEM_PROMPT)
                .saver(new MemorySaver())
                //Agent 通过状态自动维护对话历史。使用 MemorySaver 配置持久化存储,默认使用hashmap
                .outputType(WeatherResponse.class)
                .interceptors(new RAGContextInterceptor())
                .build();


        WeatherAgent weatherAgent = new WeatherAgent();
        weatherAgent.setAgent(agent);
        return weatherAgent;
    }








}
