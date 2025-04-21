package org.agro.config;

import lombok.Data;
import org.agro.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "openweathermap")
public class OpenWeatherMapConfig {
    
    private SystemConfigService systemConfigService;
    private String currentWeatherUrl;
    private String hourlyForecastUrl;
    private String dailyForecastUrl;
    private String climateForecastUrl;
    private String historicalWeatherUrl;
    
    @Autowired
    public OpenWeatherMapConfig(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }
    
    public String getApiKey() {
        // 从系统配置服务获取API Key
        return systemConfigService.getApiKey();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 