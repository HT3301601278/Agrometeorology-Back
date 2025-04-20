package org.agro.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "weather_forecast", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"latitude", "longitude", "dt", "forecast_type"}))
public class WeatherForecast {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;
    
    @Column(nullable = false)
    private Long dt; // 预报时间戳
    
    @Column(name = "forecast_type", nullable = false)
    private Byte forecastType; // 1:小时级, 2:16天, 3:30天
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temp;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal feelsLike;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal tempMin;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal tempMax;
    
    private Integer pressure;
    
    private Integer humidity;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal windSpeed;
    
    private Integer windDeg;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal windGust;
    
    private Integer cloudsAll;
    
    private Integer visibility;
    
    @Column(precision = 3, scale = 2)
    private BigDecimal pop; // 降水概率
    
    @Column(precision = 5, scale = 2)
    private BigDecimal rain3h; // 3小时降雨量
    
    @Column(precision = 5, scale = 2)
    private BigDecimal snow3h; // 3小时降雪量
    
    private Integer weatherId;
    
    @Column(length = 50)
    private String weatherMain;
    
    @Column(length = 100)
    private String weatherDescription;
    
    @Column(length = 20)
    private String weatherIcon;
    
    @Column(length = 30)
    private String dtTxt; // 可读的日期时间文本
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public static final byte TYPE_HOURLY = 1;
    public static final byte TYPE_DAILY_16 = 2;
    public static final byte TYPE_CLIMATE_30 = 3;
} 