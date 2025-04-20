package org.agro.controller;

import org.agro.dto.ResponseResult;
import org.agro.dto.WeatherCurrentDTO;
import org.agro.dto.WeatherForecastDTO;
import org.agro.dto.WeatherHistoricalDTO;
import org.agro.dto.WeatherRequestDTO;
import org.agro.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * 获取实时天气数据
     */
    @PostMapping("/current")
    public ResponseResult<WeatherCurrentDTO> getCurrentWeather(@RequestBody WeatherRequestDTO request) {
        return ResponseResult.success(weatherService.getCurrentWeather(request));
    }

    /**
     * 获取特定地点的天气预报
     */
    @PostMapping("/forecast")
    public ResponseResult<List<WeatherForecastDTO>> getWeatherForecast(@RequestBody WeatherRequestDTO request) {
        return ResponseResult.success(weatherService.getWeatherForecast(request));
    }

    /**
     * 获取特定地点的历史天气数据
     */
    @PostMapping("/historical")
    public ResponseResult<List<WeatherHistoricalDTO>> getHistoricalWeather(@RequestBody WeatherRequestDTO request) {
        return ResponseResult.success(weatherService.getHistoricalWeather(request));
    }
} 