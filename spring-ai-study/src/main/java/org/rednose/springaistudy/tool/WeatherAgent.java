package org.rednose.springaistudy.tool;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import lombok.Data;

/**
 * @author lixuefan
 * @since 2026/5/28
 */
@Data
public class WeatherAgent {

    private ReactAgent agent;

    public String call(String message, RunnableConfig runnableConfig) throws Exception {
        return agent.call(message, runnableConfig).getText();
    }
}
