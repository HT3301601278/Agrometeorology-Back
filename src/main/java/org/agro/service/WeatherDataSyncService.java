package org.agro.service;

import lombok.extern.slf4j.Slf4j;
import org.agro.dto.WeatherRequestDTO;
import org.agro.entity.WeatherCurrent;
import org.agro.repository.WeatherCurrentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 天气数据同步服务
 * 负责定时从 OpenWeatherMap API 获取数据，并更新到数据库
 */
@Slf4j
@Service
public class WeatherDataSyncService {

    private final WeatherService weatherService;
    private final WeatherCurrentRepository currentRepository;
    // 可注入Field Repository以获取所有地块坐标
    
    @Autowired
    public WeatherDataSyncService(WeatherService weatherService, 
                                 WeatherCurrentRepository currentRepository) {
        this.weatherService = weatherService;
        this.currentRepository = currentRepository;
    }
    
    /**
     * 每10分钟更新一次所有地块的实时天气数据
     * 实际应用中，应当从地块表中读取所有有效地块的坐标
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void syncCurrentWeatherData() {
        log.info("开始同步实时天气数据...");
        
        // 这里应该是从Field表中获取所有地块的坐标
        // 为演示简单起见，这里使用静态经纬度
        List<Coordinate> coordinates = List.of(
            new Coordinate(new BigDecimal("32.26"), new BigDecimal("110.09"))
            // 可以添加更多坐标
        );
        
        for (Coordinate coord : coordinates) {
            try {
                WeatherRequestDTO request = new WeatherRequestDTO();
                request.setLatitude(coord.getLatitude());
                request.setLongitude(coord.getLongitude());
                
                // 调用服务获取最新天气数据（该方法会自动保存到数据库）
                weatherService.getCurrentWeather(request);
                
                log.info("成功更新地块坐标({}, {})的实时天气数据", coord.getLatitude(), coord.getLongitude());
            } catch (Exception e) {
                log.error("更新地块坐标({}, {})的实时天气数据失败", coord.getLatitude(), coord.getLongitude(), e);
            }
        }
        
        log.info("实时天气数据同步完成");
    }
    
    /**
     * 每天凌晨2点更新一次未来预报数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncForecastData() {
        log.info("开始同步天气预报数据...");
        
        // 类似实时数据同步的逻辑，但调用预报API
        // 实际实现中需读取所有地块坐标
        
        log.info("天气预报数据同步完成");
    }
    
    // 坐标类，用于存储经纬度
    private static class Coordinate {
        private final BigDecimal latitude;
        private final BigDecimal longitude;
        
        public Coordinate(BigDecimal latitude, BigDecimal longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public BigDecimal getLatitude() {
            return latitude;
        }
        
        public BigDecimal getLongitude() {
            return longitude;
        }
    }
} 