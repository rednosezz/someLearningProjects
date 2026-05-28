package org.rednose.springaistudy.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.tools.ToolContextConstants;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author A
 */
public class UserLocationTool implements BiFunction<String, ToolContext, String> {

    //模拟数据库用户所在城市信息
    private Map<String, String> userInfo = Map.of("1", "福建",
            "2", "深圳",
            "3", "上海",
            "4", "北京");


    @Override
    public String apply(
            @ToolParam(description = "User query") String query,
            ToolContext toolContext) {
        // 从上下文中获取用户信息
        String userId = "";
        if (toolContext != null && toolContext.getContext() != null) {
            //拿到运行配置
            RunnableConfig runnableConfig = (RunnableConfig) toolContext.getContext().get(ToolContextConstants.AGENT_CONFIG_CONTEXT_KEY);
            // 拿到元数据 userId
            Optional<Object> userIdObjOptional = runnableConfig.metadata("userId");
            if (userIdObjOptional.isPresent()) {
                userId = (String) userIdObjOptional.get();
            }
        }
        //判空校验
        if (!userInfo.containsKey(userId)) {
            throw new RuntimeException("用户信息不存在");
        }


        return "用户" + userId + "的所在地是" + userInfo.get(userId);
    }
}
