package org.rednose.springaistudy.tool;

import org.springframework.ai.chat.model.ToolContext;

import java.util.function.BiFunction;

public class WeatherTool implements BiFunction<WeatherParam, ToolContext, String> {
    @Override
    public String apply(WeatherParam param, ToolContext toolContext) {
        String city = param.getCity();
        return city +"的天气情况总会是雨天 ！";
    }
}
