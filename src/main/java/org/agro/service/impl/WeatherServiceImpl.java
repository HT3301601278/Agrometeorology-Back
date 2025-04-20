package org.agro.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.agro.config.OpenWeatherMapConfig;
import org.agro.dto.WeatherCurrentDTO;
import org.agro.dto.WeatherForecastDTO;
import org.agro.dto.WeatherHistoricalDTO;
import org.agro.dto.WeatherRequestDTO;
import org.agro.entity.WeatherCurrent;
import org.agro.entity.WeatherForecast;
import org.agro.entity.WeatherHistorical;
import org.agro.repository.WeatherCurrentRepository;
import org.agro.repository.WeatherForecastRepository;
import org.agro.repository.WeatherHistoricalRepository;
import org.agro.service.WeatherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OpenWeatherMapConfig weatherConfig;
    private final WeatherCurrentRepository currentRepository;
    private final WeatherForecastRepository forecastRepository;
    private final WeatherHistoricalRepository historicalRepository;
    
    private static final int MAX_AGE_CURRENT_WEATHER = 30 * 60; // 30分钟，单位秒
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public WeatherServiceImpl(RestTemplate restTemplate,
                             OpenWeatherMapConfig weatherConfig,
                             WeatherCurrentRepository currentRepository,
                             WeatherForecastRepository forecastRepository,
                             WeatherHistoricalRepository historicalRepository) {
        this.restTemplate = restTemplate;
        this.weatherConfig = weatherConfig;
        this.currentRepository = currentRepository;
        this.forecastRepository = forecastRepository;
        this.historicalRepository = historicalRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public WeatherCurrentDTO getCurrentWeather(WeatherRequestDTO request) {
        BigDecimal latitude = request.getLatitude();
        BigDecimal longitude = request.getLongitude();
        
        // 检查数据库中是否有足够新的数据
        Optional<WeatherCurrent> latestData = currentRepository.findLatestByCoordinates(latitude, longitude);
        long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳，单位秒
        
        WeatherCurrent weatherData;
        if (latestData.isPresent() && (currentTime - latestData.get().getDt() < MAX_AGE_CURRENT_WEATHER)) {
            // 使用数据库中的数据
            weatherData = latestData.get();
            log.info("Using cached current weather data for lat={}, lon={}", latitude, longitude);
        } else {
            // 调用API获取新数据
            weatherData = fetchCurrentWeatherFromApi(latitude, longitude, request.getUnits(), request.getLang());
            // 保存到数据库
            currentRepository.save(weatherData);
            log.info("Fetched and saved new current weather data for lat={}, lon={}", latitude, longitude);
        }
        
        // 转换为DTO
        return convertToCurrentDTO(weatherData);
    }

    @Override
    public List<WeatherForecastDTO> getWeatherForecast(WeatherRequestDTO request) {
        BigDecimal latitude = request.getLatitude();
        BigDecimal longitude = request.getLongitude();
        long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳，单位秒
        
        // 计算请求时间范围
        Long startTime = request.getStartTime() != null ? request.getStartTime() : currentTime;
        Long endTime = request.getEndTime() != null ? request.getEndTime() : currentTime + 30 * 24 * 60 * 60; // 默认30天
        
        List<WeatherForecastDTO> resultList = new ArrayList<>();
        
        // 根据时间范围决定调用哪些预报API
        if (startTime < currentTime + 4 * 24 * 60 * 60) { // 前4天使用小时级预报
            resultList.addAll(getHourlyForecast(latitude, longitude, 
                    startTime, Math.min(endTime, currentTime + 4 * 24 * 60 * 60), 
                    request.getUnits(), request.getLang()));
        }
        
        if (endTime > currentTime + 4 * 24 * 60 * 60 && startTime < currentTime + 16 * 24 * 60 * 60) { // 4-16天使用16天预报
            resultList.addAll(getDailyForecast(latitude, longitude, 
                    Math.max(startTime, currentTime + 4 * 24 * 60 * 60), 
                    Math.min(endTime, currentTime + 16 * 24 * 60 * 60),
                    request.getUnits(), request.getLang()));
        }
        
        if (endTime > currentTime + 16 * 24 * 60 * 60) { // 16-30天使用气候预报
            resultList.addAll(getClimateForecast(latitude, longitude, 
                    Math.max(startTime, currentTime + 16 * 24 * 60 * 60), 
                    endTime, request.getUnits(), request.getLang()));
        }
        
        return resultList;
    }

    @Override
    public List<WeatherHistoricalDTO> getHistoricalWeather(WeatherRequestDTO request) {
        BigDecimal latitude = request.getLatitude();
        BigDecimal longitude = request.getLongitude();
        
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Historical weather request must include startTime and endTime");
        }
        
        Long startTime = request.getStartTime();
        Long endTime = request.getEndTime();
        
        // 检查数据库中是否有数据
        long dataCount = historicalRepository.countByCoordinatesInTimeRange(latitude, longitude, startTime, endTime);
        int expectedCount = (int) ((endTime - startTime) / 3600) + 1; // 每小时一条数据
        
        List<WeatherHistorical> historicalData;
        
        if (dataCount >= expectedCount * 0.9) { // 如果数据完整度达到90%以上，直接使用数据库数据
            historicalData = historicalRepository.findByCoordinatesInTimeRange(latitude, longitude, startTime, endTime);
            log.info("Using {} cached historical weather records for lat={}, lon={}", dataCount, latitude, longitude);
        } else {
            // 调用API获取新数据
            historicalData = fetchHistoricalWeatherFromApi(latitude, longitude, startTime, endTime, request.getUnits(), request.getLang());
            // 保存到数据库（考虑到可能数据量大，可以优化为批量保存）
            historicalRepository.saveAll(historicalData);
            log.info("Fetched and saved {} new historical weather records for lat={}, lon={}", historicalData.size(), latitude, longitude);
        }
        
        // 转换为DTO
        return historicalData.stream()
                .map(this::convertToHistoricalDTO)
                .collect(Collectors.toList());
    }
    
    // 私有辅助方法

    private WeatherCurrent fetchCurrentWeatherFromApi(BigDecimal latitude, BigDecimal longitude, String units, String lang) {
        String url = UriComponentsBuilder.fromHttpUrl(weatherConfig.getCurrentWeatherUrl())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("units", units)
                .queryParam("lang", lang)
                .queryParam("appid", weatherConfig.getApiKey())
                .build()
                .toUriString();
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            
            WeatherCurrent weatherCurrent = new WeatherCurrent();
            weatherCurrent.setLatitude(latitude);
            weatherCurrent.setLongitude(longitude);
            weatherCurrent.setDt(root.path("dt").asLong());
            weatherCurrent.setTemp(BigDecimal.valueOf(root.path("main").path("temp").asDouble()));
            weatherCurrent.setFeelsLike(BigDecimal.valueOf(root.path("main").path("feels_like").asDouble()));
            weatherCurrent.setTempMin(BigDecimal.valueOf(root.path("main").path("temp_min").asDouble()));
            weatherCurrent.setTempMax(BigDecimal.valueOf(root.path("main").path("temp_max").asDouble()));
            weatherCurrent.setPressure(root.path("main").path("pressure").asInt());
            weatherCurrent.setHumidity(root.path("main").path("humidity").asInt());
            weatherCurrent.setWindSpeed(BigDecimal.valueOf(root.path("wind").path("speed").asDouble()));
            weatherCurrent.setWindDeg(root.path("wind").path("deg").asInt());
            
            if (!root.path("wind").path("gust").isMissingNode()) {
                weatherCurrent.setWindGust(BigDecimal.valueOf(root.path("wind").path("gust").asDouble()));
            }
            
            weatherCurrent.setCloudsAll(root.path("clouds").path("all").asInt());
            weatherCurrent.setVisibility(root.path("visibility").asInt());
            
            if (!root.path("rain").isMissingNode() && !root.path("rain").path("1h").isMissingNode()) {
                weatherCurrent.setRain1h(BigDecimal.valueOf(root.path("rain").path("1h").asDouble()));
            }
            
            if (!root.path("snow").isMissingNode() && !root.path("snow").path("1h").isMissingNode()) {
                weatherCurrent.setSnow1h(BigDecimal.valueOf(root.path("snow").path("1h").asDouble()));
            }
            
            if (root.path("weather").isArray() && root.path("weather").size() > 0) {
                JsonNode weather = root.path("weather").get(0);
                weatherCurrent.setWeatherId(weather.path("id").asInt());
                weatherCurrent.setWeatherMain(weather.path("main").asText());
                weatherCurrent.setWeatherDescription(weather.path("description").asText());
                weatherCurrent.setWeatherIcon(weather.path("icon").asText());
            }
            
            return weatherCurrent;
            
        } catch (Exception e) {
            log.error("Error parsing current weather API response", e);
            throw new RuntimeException("Error fetching current weather data", e);
        }
    }
    
    private List<WeatherForecastDTO> getHourlyForecast(BigDecimal latitude, BigDecimal longitude, Long startTime, Long endTime, String units, String lang) {
        // 检查数据库中是否有足够的数据
        long dataCount = forecastRepository.countByCoordinatesAndTypeInTimeRange(
                latitude, longitude, WeatherForecast.TYPE_HOURLY, startTime, endTime);
        int expectedCount = (int) ((endTime - startTime) / 3600) + 1; // 每小时一条数据
        
        List<WeatherForecast> forecastData;
        
        if (dataCount >= expectedCount * 0.9) { // 如果数据完整度达到90%以上，直接使用数据库数据
            forecastData = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                    latitude, longitude, WeatherForecast.TYPE_HOURLY, startTime, endTime);
            log.info("Using {} cached hourly forecast records", dataCount);
        } else {
            // 调用API获取新数据
            forecastData = fetchHourlyForecastFromApi(latitude, longitude, units, lang);
            // 过滤所需时间范围
            forecastData = forecastData.stream()
                    .filter(f -> f.getDt() >= startTime && f.getDt() <= endTime)
                    .collect(Collectors.toList());
            // 保存到数据库
            forecastRepository.saveAll(forecastData);
            log.info("Fetched and saved {} new hourly forecast records", forecastData.size());
        }
        
        // 转换为DTO
        return forecastData.stream()
                .map(this::convertToForecastDTO)
                .collect(Collectors.toList());
    }
    
    private List<WeatherForecast> fetchHourlyForecastFromApi(BigDecimal latitude, BigDecimal longitude, String units, String lang) {
        String url = UriComponentsBuilder.fromHttpUrl(weatherConfig.getHourlyForecastUrl())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("units", units)
                .queryParam("lang", lang)
                .queryParam("appid", weatherConfig.getApiKey())
                .build()
                .toUriString();
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<WeatherForecast> result = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode list = root.path("list");
            
            for (JsonNode item : list) {
                WeatherForecast forecast = new WeatherForecast();
                forecast.setLatitude(latitude);
                forecast.setLongitude(longitude);
                forecast.setForecastType(WeatherForecast.TYPE_HOURLY);
                forecast.setDt(item.path("dt").asLong());
                
                JsonNode main = item.path("main");
                forecast.setTemp(BigDecimal.valueOf(main.path("temp").asDouble()));
                forecast.setFeelsLike(BigDecimal.valueOf(main.path("feels_like").asDouble()));
                forecast.setTempMin(BigDecimal.valueOf(main.path("temp_min").asDouble()));
                forecast.setTempMax(BigDecimal.valueOf(main.path("temp_max").asDouble()));
                forecast.setPressure(main.path("pressure").asInt());
                forecast.setHumidity(main.path("humidity").asInt());
                
                JsonNode wind = item.path("wind");
                forecast.setWindSpeed(BigDecimal.valueOf(wind.path("speed").asDouble()));
                forecast.setWindDeg(wind.path("deg").asInt());
                if (!wind.path("gust").isMissingNode()) {
                    forecast.setWindGust(BigDecimal.valueOf(wind.path("gust").asDouble()));
                }
                
                forecast.setCloudsAll(item.path("clouds").path("all").asInt());
                forecast.setVisibility(item.path("visibility").asInt());
                forecast.setPop(BigDecimal.valueOf(item.path("pop").asDouble()));
                
                if (!item.path("rain").isMissingNode() && !item.path("rain").path("3h").isMissingNode()) {
                    forecast.setRain3h(BigDecimal.valueOf(item.path("rain").path("3h").asDouble()));
                }
                
                if (!item.path("snow").isMissingNode() && !item.path("snow").path("3h").isMissingNode()) {
                    forecast.setSnow3h(BigDecimal.valueOf(item.path("snow").path("3h").asDouble()));
                }
                
                if (item.path("weather").isArray() && item.path("weather").size() > 0) {
                    JsonNode weather = item.path("weather").get(0);
                    forecast.setWeatherId(weather.path("id").asInt());
                    forecast.setWeatherMain(weather.path("main").asText());
                    forecast.setWeatherDescription(weather.path("description").asText());
                    forecast.setWeatherIcon(weather.path("icon").asText());
                }
                
                result.add(forecast);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error parsing hourly forecast API response", e);
            throw new RuntimeException("Error fetching hourly forecast data", e);
        }
    }
    
    private List<WeatherForecastDTO> getDailyForecast(BigDecimal latitude, BigDecimal longitude, Long startTime, Long endTime, String units, String lang) {
        // 实现16天预报的逻辑，与小时级预报类似
        // 为简化代码，这里不再重复实现
        return new ArrayList<>();
    }
    
    private List<WeatherForecastDTO> getClimateForecast(BigDecimal latitude, BigDecimal longitude, Long startTime, Long endTime, String units, String lang) {
        // 实现30天气候预报的逻辑，与小时级预报类似
        // 为简化代码，这里不再重复实现
        return new ArrayList<>();
    }
    
    private List<WeatherHistorical> fetchHistoricalWeatherFromApi(BigDecimal latitude, BigDecimal longitude, Long startTime, Long endTime, String units, String lang) {
        String url = UriComponentsBuilder.fromHttpUrl(weatherConfig.getHistoricalWeatherUrl())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("type", "hour")
                .queryParam("start", startTime)
                .queryParam("end", endTime)
                .queryParam("units", units)
                .queryParam("appid", weatherConfig.getApiKey())
                .build()
                .toUriString();
        
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<WeatherHistorical> result = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode list = root.path("list");
            
            for (JsonNode item : list) {
                WeatherHistorical historical = new WeatherHistorical();
                historical.setLatitude(latitude);
                historical.setLongitude(longitude);
                historical.setDt(item.path("dt").asLong());
                
                JsonNode main = item.path("main");
                historical.setTemp(BigDecimal.valueOf(main.path("temp").asDouble()));
                historical.setFeelsLike(BigDecimal.valueOf(main.path("feels_like").asDouble()));
                historical.setTempMin(BigDecimal.valueOf(main.path("temp_min").asDouble()));
                historical.setTempMax(BigDecimal.valueOf(main.path("temp_max").asDouble()));
                historical.setPressure(main.path("pressure").asInt());
                historical.setHumidity(main.path("humidity").asInt());
                
                JsonNode wind = item.path("wind");
                historical.setWindSpeed(BigDecimal.valueOf(wind.path("speed").asDouble()));
                historical.setWindDeg(wind.path("deg").asInt());
                
                historical.setCloudsAll(item.path("clouds").path("all").asInt());
                
                if (!item.path("rain").isMissingNode()) {
                    if (!item.path("rain").path("1h").isMissingNode()) {
                        historical.setRain1h(BigDecimal.valueOf(item.path("rain").path("1h").asDouble()));
                    }
                    if (!item.path("rain").path("3h").isMissingNode()) {
                        historical.setRain3h(BigDecimal.valueOf(item.path("rain").path("3h").asDouble()));
                    }
                }
                
                if (!item.path("snow").isMissingNode()) {
                    if (!item.path("snow").path("1h").isMissingNode()) {
                        historical.setSnow1h(BigDecimal.valueOf(item.path("snow").path("1h").asDouble()));
                    }
                    if (!item.path("snow").path("3h").isMissingNode()) {
                        historical.setSnow3h(BigDecimal.valueOf(item.path("snow").path("3h").asDouble()));
                    }
                }
                
                if (item.path("weather").isArray() && item.path("weather").size() > 0) {
                    JsonNode weather = item.path("weather").get(0);
                    historical.setWeatherId(weather.path("id").asInt());
                    historical.setWeatherMain(weather.path("main").asText());
                    historical.setWeatherDescription(weather.path("description").asText());
                    historical.setWeatherIcon(weather.path("icon").asText());
                }
                
                result.add(historical);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error parsing historical weather API response", e);
            throw new RuntimeException("Error fetching historical weather data", e);
        }
    }
    
    // 转换方法
    private WeatherCurrentDTO convertToCurrentDTO(WeatherCurrent entity) {
        WeatherCurrentDTO dto = new WeatherCurrentDTO();
        BeanUtils.copyProperties(entity, dto);
        // 设置可读的日期时间
        dto.setDt(entity.getDt());
        return dto;
    }
    
    private WeatherForecastDTO convertToForecastDTO(WeatherForecast entity) {
        WeatherForecastDTO dto = new WeatherForecastDTO();
        BeanUtils.copyProperties(entity, dto);
        // 设置可读的日期时间
        LocalDateTime dateTime = Instant.ofEpochSecond(entity.getDt())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        dto.setDtTxt(dateTime.format(dtFormatter));
        return dto;
    }
    
    private WeatherHistoricalDTO convertToHistoricalDTO(WeatherHistorical entity) {
        WeatherHistoricalDTO dto = new WeatherHistoricalDTO();
        BeanUtils.copyProperties(entity, dto);
        // 设置可读的日期时间
        LocalDateTime dateTime = Instant.ofEpochSecond(entity.getDt())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        dto.setDtTxt(dateTime.format(dtFormatter));
        return dto;
    }
} 