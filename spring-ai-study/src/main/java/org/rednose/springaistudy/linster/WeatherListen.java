package org.rednose.springaistudy.linster;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import org.rednose.springaistudy.tool.WeatherAgent;
import org.rednose.springaistudy.tool.WeatherResponse;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Scanner;

/**
 * @author lixuefan
 * @since 2026/5/29
 */
@Component
public class WeatherListen {

    @Resource
    WeatherAgent weatherAgent;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== 天气查询系统已启动 ===");
        System.out.println("请输入城市名称（输入 stop 退出）：");

        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(String.valueOf(Thread.currentThread().getId()))
                .addMetadata("userId", "1")
                .build();

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if ("stop".equalsIgnoreCase(input)) {
                System.out.println("程序已退出");
                break;
            }

            if (!input.isEmpty()) {
                System.out.println("查询城市：" + input);
                String call = weatherAgent.call(input, runnableConfig);
                WeatherResponse weatherResponse = JSONObject.parseObject(call, WeatherResponse.class);
                System.out.println(weatherResponse);
            }
        }
    }
}