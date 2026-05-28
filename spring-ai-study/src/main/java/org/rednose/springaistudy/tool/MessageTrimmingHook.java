package org.rednose.springaistudy.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * @author lixuefan
 * @since 2026/5/28
 */

@HookPositions({HookPosition.AFTER_AGENT})
@Slf4j
public class MessageTrimmingHook extends MessagesModelHook{

    private static final int MAX_MESSAGES = 6;

    @Override
    public String getName() {
        return "message_trimming";
    }


    @Override
    public AgentCommand afterModel(List<Message> previousMessages, RunnableConfig config) {
        log.info("消息管理,当前消息数:{}", previousMessages.size());
        log.info("消息管理,当前消息:{}", JSONObject.toJSON(previousMessages));


        if (previousMessages.size() <= MAX_MESSAGES) {
            return new AgentCommand(previousMessages);
        }
        //超过6条,即完成一次完整的一轮天气查询后,进行消息修剪
        return new AgentCommand(previousMessages.subList(previousMessages.size() - 2, previousMessages.size()));
    }
}
