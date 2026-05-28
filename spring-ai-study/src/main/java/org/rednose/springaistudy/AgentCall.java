package org.rednose.springaistudy;


import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.rednose.springaistudy.tool.WeatherParam;
import org.rednose.springaistudy.tool.WeatherTool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.minimax.MiniMaxChatModel;
import org.springframework.ai.minimax.MiniMaxChatOptions;
import org.springframework.ai.minimax.api.MiniMaxApi;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.stereotype.Component;

/**
 * @author lixuefan
 * @since 2026/5/28
 */
@Component
public class AgentCall {

    public static void main(String[] args) throws GraphRunnerException {

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

        // 4. Agent

        ReactAgent agent = ReactAgent.builder()
                .name("天气助手")
                .model(miniMaxChatModel)//指定模型
                .tools(weatherTool)//指定工具
                .systemPrompt(SYSTEM_PROMPT)//系统提示词
                .saver(new MemorySaver())//保存上下文记忆
                .build();


        // 5. 调用
        AssistantMessage response = agent.call("深圳今天是什么天气");

        System.out.println(response.getText());

        response = agent.call("明天呢");

        System.out.println(response.getText());

    }


}
