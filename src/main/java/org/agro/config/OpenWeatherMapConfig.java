package org.agro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "openweathermap")
public class OpenWeatherMapConfig {
    
    private String apiKey;
    private String currentWeatherUrl = "https://api.openweathermap.org/data/2.5/weather";
    private String hourlyForecastUrl = "https://pro.openweathermap.org/data/2.5/forecast/hourly";
    private String dailyForecastUrl = "https://api.openweathermap.org/data/2.5/forecast/daily";
    private String climateForecastUrl = "https://pro.openweathermap.org/data/2.5/forecast/climate";
    private String historicalWeatherUrl = "https://history.openweathermap.org/data/2.5/history/city";
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 