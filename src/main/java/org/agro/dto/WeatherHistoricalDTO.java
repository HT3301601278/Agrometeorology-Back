package org.agro.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeatherHistoricalDTO {
    private Long dt;
    private BigDecimal temp;
    private BigDecimal feelsLike;
    private Integer pressure;
    private Integer humidity;
    private BigDecimal tempMin;
    private BigDecimal tempMax;
    private BigDecimal windSpeed;
    private Integer windDeg;
    private Integer cloudsAll;
    private BigDecimal rain1h;
    private BigDecimal rain3h;
    private BigDecimal snow1h;
    private BigDecimal snow3h;
    private Integer weatherId;
    private String weatherMain;
    private String weatherDescription;
    private String weatherIcon;
    private String dtTxt; // 可读的日期时间文本
}
