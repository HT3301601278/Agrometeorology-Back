package org.agro.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeatherCurrentDTO {
    private Long dt;
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
    private BigDecimal rain1h;
    private BigDecimal snow1h;
    private Integer weatherId;
    private String weatherMain;
    private String weatherDescription;
    private String weatherIcon;
    private String locationName;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
