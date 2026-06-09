package org.rednose.springaistudy.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;
import java.util.function.BiFunction;

import static com.alibaba.cloud.ai.graph.agent.tools.ToolContextConstants.AGENT_CONFIG_CONTEXT_KEY;

/**
 * @author A
 */
@Slf4j
public class UserLocationTool implements BiFunction<UserLocationParam, ToolContext, String> {

    //模拟数据库用户所在城市信息
    private Map<String, String> userInfo = Map.of("1", "福建",
            "2", "深圳",
            "3", "上海",
            "4", "北京");


    @Override
    public String apply(
            @ToolParam(description = "User query") UserLocationParam query,
            ToolContext toolContext) {
        // 永远从 ToolContext 里的 RunnableConfig.metadata 取真实 userId，
        // 不依赖 LLM 传入的 query.userId（LLM 不知道真实 ID，只能传占位符）。
        String userId = null;
        if (toolContext != null
                && toolContext.getContext() != null
                && toolContext.getContext().get(AGENT_CONFIG_CONTEXT_KEY) instanceof RunnableConfig runnableConfig) {
            userId = String.valueOf(runnableConfig.metadata("userId").orElse(""));
        }

        log.info("[UserLocationTool] userId={}, query.userId={}", userId,
                query == null ? null : query.getUserId());

        //判空校验
        if (userId == null || userId.isEmpty() || "null".equals(userId)) {
            throw new RuntimeException("请指定用户信息");
        }
        if (!userInfo.containsKey(userId)) {
            throw new RuntimeException("用户信息不存在: " + userId);
        }

        return "用户" + userId + "的所在地是" + userInfo.get(userId);
    }
}