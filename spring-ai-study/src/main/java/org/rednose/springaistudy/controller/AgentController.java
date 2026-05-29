package org.rednose.springaistudy.controller;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.rednose.springaistudy.tool.WeatherAgent;
import org.rednose.springaistudy.tool.WeatherResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lixuefan
 * @since 2026/5/29
 */

@RestController
@RequestMapping("/agent")
@Slf4j
public class AgentController {

    @Resource
    WeatherAgent weatherAgent;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/weather")
    public WeatherResponse weather(@RequestParam String message) {
        try {
            String json = weatherAgent.call(message, RunnableConfig.builder().build());
            return objectMapper.readValue(json, WeatherResponse.class);
        } catch (Exception e) {
            log.error("天气查询失败", e);
            WeatherResponse error = new WeatherResponse();
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }

    @GetMapping("/weatherWithUser")
    public WeatherResponse weatherWithUser(@RequestParam String message, @RequestParam String userId) {
        try {
            RunnableConfig config = RunnableConfig.builder()
                    .addMetadata("userId", userId)
                    .build();
            String json = weatherAgent.call(message, config);
            return objectMapper.readValue(json, WeatherResponse.class);
        } catch (Exception e) {
            log.error("天气查询失败", e);
            WeatherResponse error = new WeatherResponse();
            error.setErrorMessage(e.getMessage());
            return error;
        }
    }

}