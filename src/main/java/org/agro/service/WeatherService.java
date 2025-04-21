package org.agro.service;

import org.agro.dto.WeatherCurrentDTO;
import org.agro.dto.WeatherForecastDTO;
import org.agro.dto.WeatherHistoricalDTO;
import org.agro.dto.WeatherRequestDTO;

import java.util.List;

public interface WeatherService {
    
    /**
     * 获取实时天气数据
     * @param request 请求参数
     * @return 实时天气数据
     */
    WeatherCurrentDTO getCurrentWeather(WeatherRequestDTO request);
    
    /**
     * 获取天气预报数据
     * @param request 请求参数
     * @return 天气预报数据列表
     */
    List<WeatherForecastDTO> getWeatherForecast(WeatherRequestDTO request);
    
    /**
     * 获取历史天气数据
     * @param request 请求参数
     * @return 历史天气数据列表
     */
    List<WeatherHistoricalDTO> getHistoricalWeather(WeatherRequestDTO request);
} 