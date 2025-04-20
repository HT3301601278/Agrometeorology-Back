package org.agro.entity;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "weather_historical", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"latitude", "longitude", "dt"}))
public class WeatherHistorical {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;
    
    @Column(nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;
    
    @Column(nullable = false)
    private Long dt; // 历史时间戳
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temp;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal feelsLike;
    
    private Integer pressure;
    
    private Integer humidity;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal tempMin;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal tempMax;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal windSpeed;
    
    private Integer windDeg;
    
    private Integer cloudsAll;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal rain1h;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal rain3h;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal snow1h;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal snow3h;
    
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