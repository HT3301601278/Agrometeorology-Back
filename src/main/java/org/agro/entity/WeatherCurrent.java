package org.agro.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "weather_current", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"latitude", "longitude", "dt"}))
public class WeatherCurrent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;
    
    @Column(nullable = false)
    private Long dt; // 数据时间戳
    
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
    
    @Column(precision = 5, scale = 2)
    private BigDecimal rain1h;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal snow1h;
    
    private Integer weatherId;
    
    @Column(length = 50)
    private String weatherMain;
    
    @Column(length = 100)
    private String weatherDescription;
    
    @Column(length = 20)
    private String weatherIcon;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 