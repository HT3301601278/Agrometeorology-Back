package org.agro.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeatherForecastDTO {
    private Long dt;
    private Byte forecastType;
    private BigDecimal temp;
    private BigDecimal feelsLike;
    private BigDecimal tempMin;
    private BigDecimal tempMax;
    private Integer pressure;
    private Integer humidity;
    private BigDecimal windSpeed;
    private Integer windDeg;
    private BigDecimal windGust;
    private Integer cloudsAll;
    private Integer visibility;
    private BigDecimal pop;
    private BigDecimal rain3h;
    private BigDecimal snow3h;
    private Integer weatherId;
    private String weatherMain;
    private String weatherDescription;
    private String weatherIcon;
    private String dtTxt; // 可读的日期时间文本
}
