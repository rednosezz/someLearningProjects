package org.rednose.springaistudy;


import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import jakarta.annotation.Resource;
import org.rednose.springaistudy.tool.WeatherTool;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author lixf
 */
@SpringBootApplication
public class SpringAiStudyApplication {



    public static void main(String[] args) throws GraphRunnerException {
        SpringApplication.run(SpringAiStudyApplication.class, args);
    }

}
