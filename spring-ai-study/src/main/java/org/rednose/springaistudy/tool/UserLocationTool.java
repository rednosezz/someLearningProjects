package org.rednose.springaistudy.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.tools.ToolContextConstants;
import org.springframework.ai.chat.model.ToolContext;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author A
 */
public class UserLocationTool implements BiFunction<UserLocationParam, ToolContext, String> {

    private Map<String, String> userInfo = Map.of("1", "福建",
            "2", "深圳",
            "3", "上海",
            "4", "北京");


    @Override
    public String apply(UserLocationParam param, ToolContext toolContext) {
        String userId = null;
        if (toolContext != null && toolContext.getContext() != null) {
            RunnableConfig runnableConfig = (RunnableConfig) toolContext.getContext().get(ToolContextConstants.AGENT_CONFIG_CONTEXT_KEY);
            Optional<Object> userIdObjOptional = runnableConfig.metadata("userId");
            if (userIdObjOptional.isPresent()) {
                userId = (String) userIdObjOptional.get();
            }
        }
        if (userId == null || !userInfo.containsKey(userId)) {
            return "用户" + userId + "的所在地是" + userInfo.get(userId);
        }
        return "用户" + userId + "的所在地是" + userInfo.get(userId);
    }
}
