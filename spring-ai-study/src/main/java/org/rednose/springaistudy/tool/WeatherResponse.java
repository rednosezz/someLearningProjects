package org.rednose.springaistudy.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lixuefan
 * @since 2026/5/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherResponse {

    /**
     * 城市名称
     */
    @JsonProperty("city")
    private String city;

    /**
     * 天气状况
     */
    @JsonProperty("weather")
    private String weather;

    /**
     * 天气建议
     */
    @JsonProperty("tip")
    private String tip;

    /**
     * 异常说明(非必填)
     */
    @JsonProperty("error_message")
    private String errorMessage;

}

