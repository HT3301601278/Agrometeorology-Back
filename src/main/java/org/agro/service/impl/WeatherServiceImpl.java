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
import org.agro.service.SystemConfigService;
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
    private final SystemConfigService systemConfigService;
    
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public WeatherServiceImpl(RestTemplate restTemplate,
                             OpenWeatherMapConfig weatherConfig,
                             WeatherCurrentRepository currentRepository,
                             WeatherForecastRepository forecastRepository,
                             WeatherHistoricalRepository historicalRepository,
                             SystemConfigService systemConfigService) {
        this.restTemplate = restTemplate;
        this.weatherConfig = weatherConfig;
        this.currentRepository = currentRepository;
        this.forecastRepository = forecastRepository;
        this.historicalRepository = historicalRepository;
        this.systemConfigService = systemConfigService;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 从系统配置中获取数据拉取频率（秒）
     */
    private int getMaxAgeCurrentWeather() {
        return systemConfigService.getDataFetchInterval() * 60; // 将分钟转换为秒
    }

    @Override
    public WeatherCurrentDTO getCurrentWeather(WeatherRequestDTO request) {
        BigDecimal latitude = request.getLatitude();
        BigDecimal longitude = request.getLongitude();
        
        // 添加调试日志，显示请求参数
        log.debug("getCurrentWeather request: lat={}, lon={}, forceRefresh={}", 
                latitude, longitude, request.getForceRefresh());
        
        // 检查数据库中是否有足够新的数据
        List<WeatherCurrent> latestDataList = currentRepository.findTopByCoordinatesOrderByDtDesc(latitude, longitude);
        Optional<WeatherCurrent> latestData = latestDataList.isEmpty() ? Optional.empty() : Optional.of(latestDataList.get(0));
        long currentTime = System.currentTimeMillis() / 1000; // 当前时间戳，单位秒
        
        if (latestData.isPresent()) {
            log.debug("Found latest data, dt={}, current time={}, diff={} seconds", 
                    latestData.get().getDt(), currentTime, (currentTime - latestData.get().getDt()));
        } else {
            log.debug("No existing data found for coordinates");
        }
        
        WeatherCurrent weatherData;
        
        // 检查是否需要强制刷新或缓存是否有效
        boolean shouldUseCache = latestData.isPresent() && 
                               (currentTime - latestData.get().getDt() < getMaxAgeCurrentWeather()) &&
                               (request.getForceRefresh() == null || !request.getForceRefresh());
        
        log.debug("Should use cache: {}, cache max age: {} seconds", shouldUseCache, getMaxAgeCurrentWeather());
        
        if (shouldUseCache) {
            // 使用数据库中的数据
            weatherData = latestData.get();
            log.info("Using cached current weather data for lat={}, lon={}, cache time={}min, forceRefresh={}", 
                    latitude, longitude, systemConfigService.getDataFetchInterval(), request.getForceRefresh());
        } else {
            try {
                // 调用API获取新数据
                log.debug("Fetching new data from API for lat={}, lon={}", latitude, longitude);
                weatherData = fetchCurrentWeatherFromApi(latitude, longitude, request.getUnits(), request.getLang());
                log.debug("API returned data with dt={}", weatherData.getDt());
                
                // 首先检查是否已存在相同坐标和时间戳的记录
                Optional<WeatherCurrent> existingRecord = currentRepository.findByLatitudeAndLongitudeAndDt(
                        latitude, longitude, weatherData.getDt());
                
                if (existingRecord.isPresent()) {
                    // 如果存在，则更新而不是插入
                    WeatherCurrent existing = existingRecord.get();
                    weatherData.setId(existing.getId()); // 保留ID
                    weatherData.setCreatedAt(existing.getCreatedAt()); // 保留创建时间
                    log.info("Updating existing weather data for lat={}, lon={}, dt={}", 
                            latitude, longitude, weatherData.getDt());
                }
                
                // 保存到数据库
                try {
                    weatherData = currentRepository.save(weatherData);
                    log.info("Successfully saved weather data for lat={}, lon={}, dt={}, forceRefresh={}", 
                            latitude, longitude, weatherData.getDt(), request.getForceRefresh());
                } catch (Exception e) {
                    log.error("Failed to save weather data: {}", e.getMessage());
                    
                    // 如果是唯一约束冲突，尝试查找并返回已存在的记录
                    if (e.getMessage() != null && e.getMessage().contains("Duplicate entry") && 
                            e.getMessage().contains("UKf490mp5tlo8xekcyuag0jy08h")) {
                        log.warn("Unique constraint violation detected, attempting to find existing record");
                        Optional<WeatherCurrent> duplicateRecord = currentRepository.findByLatitudeAndLongitudeAndDt(
                                latitude, longitude, weatherData.getDt());
                        
                        if (duplicateRecord.isPresent()) {
                            weatherData = duplicateRecord.get();
                            log.info("Using existing record instead of creating new one");
                        } else {
                            throw e; // 如果找不到记录，仍然抛出异常
                        }
                    } else {
                        throw e; // 其他类型的异常直接抛出
                    }
                }
            } catch (Exception e) {
                // 处理可能的API调用异常或数据库操作异常
                log.error("Error during weather data operation: {}", e.getMessage(), e);
                
                // 如果有缓存数据，则回退使用缓存数据
                if (latestData.isPresent()) {
                    weatherData = latestData.get();
                    log.info("Using cached data due to error for lat={}, lon={}", latitude, longitude);
                } else {
                    // 如果完全没有数据可用，则抛出异常
                    throw new RuntimeException("Unable to fetch or retrieve weather data", e);
                }
            }
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

        // 自动扩展endTime到当天23:00:00（如果endTime为某天0点）
        LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(endTime), ZoneId.systemDefault());
        if (endDateTime.getHour() == 0 && endDateTime.getMinute() == 0 && endDateTime.getSecond() == 0) {
            endTime = endTime + 23 * 3600;
        }

        long secondsInDay = 24 * 60 * 60;
        long fourDays = currentTime + 4 * secondsInDay;
        long sixteenDays = currentTime + 16 * secondsInDay;
        long thirtyDays = currentTime + 30 * secondsInDay;

        if (endTime <= fourDays) {
            // 全部在0-4天内，用小时级API
            return getHourlyForecast(latitude, longitude, startTime, endTime, request.getUnits(), request.getLang(), request.getForceRefresh());
        } else if (endTime <= sixteenDays) {
            // 覆盖到4-16天，用16天API
            return getDailyForecast(latitude, longitude, startTime, endTime, request.getUnits(), request.getLang(), request.getForceRefresh());
        } else {
            // 覆盖到16-30天，用30天API
            return getClimateForecast(latitude, longitude, startTime, endTime, request.getUnits(), request.getLang(), request.getForceRefresh());
        }
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
        long dataCount = historicalRepository.countByCoordinatesInTimeRange(latitude, longitude, startTime, endTime);
        int expectedCount = (int) ((endTime - startTime) / 3600) + 1; // 每小时一条数据
        List<WeatherHistorical> historicalData;
        // 100%完成度
        boolean shouldUseCache = dataCount == expectedCount && (request.getForceRefresh() == null || !request.getForceRefresh());
        if (shouldUseCache) {
            historicalData = historicalRepository.findByCoordinatesInTimeRange(latitude, longitude, startTime, endTime);
            log.info("Using {} cached historical weather records for lat={}, lon={}", dataCount, latitude, longitude);
        } else {
            historicalData = fetchHistoricalWeatherFromApi(latitude, longitude, startTime, endTime, request.getUnits(), request.getLang());
            historicalRepository.saveAll(historicalData);
            log.info("Fetched and saved {} new historical weather records for lat={}, lon={}, forceRefresh={}", historicalData.size(), latitude, longitude, request.getForceRefresh());
        }
        return historicalData.stream().map(this::convertToHistoricalDTO).collect(Collectors.toList());
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
    
    /**
     * 批量去重并更新已存在的WeatherForecast数据，避免唯一索引冲突
     */
    private List<WeatherForecast> deduplicateAndMergeForecasts(BigDecimal latitude, BigDecimal longitude, byte forecastType, List<WeatherForecast> apiData) {
        if (apiData == null || apiData.isEmpty()) return apiData;
        List<Long> dts = apiData.stream().map(WeatherForecast::getDt).collect(Collectors.toList());
        List<WeatherForecast> existList = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                latitude, longitude, forecastType,
                dts.stream().min(Long::compareTo).orElse(0L),
                dts.stream().max(Long::compareTo).orElse(0L)
        );
        // 用dt做key
        java.util.Map<Long, WeatherForecast> existMap = existList.stream().collect(Collectors.toMap(WeatherForecast::getDt, x -> x));
        for (WeatherForecast f : apiData) {
            WeatherForecast exist = existMap.get(f.getDt());
            if (exist != null) {
                f.setId(exist.getId());
                f.setCreatedAt(exist.getCreatedAt());
            }
        }
        return apiData;
    }

    private List<WeatherForecastDTO> getHourlyForecast(BigDecimal latitude, BigDecimal longitude, Long startTime, Long endTime, String units, String lang, Boolean forceRefresh) {
        long dataCount = forecastRepository.countByCoordinatesAndTypeInTimeRange(
                latitude, longitude, WeatherForecast.TYPE_HOURLY, startTime, endTime);
        int expectedCount = (int) ((endTime - startTime) / 3600) + 1; // 每小时一条数据
        List<WeatherForecast> forecastData;
        // 100%完成度
        boolean shouldUseCache = dataCount == expectedCount && (forceRefresh == null || !forceRefresh);
        if (shouldUseCache) {
            forecastData = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                    latitude, longitude, WeatherForecast.TYPE_HOURLY, startTime, endTime);
            log.info("Using {} cached hourly forecast records", dataCount);
        } else {
            forecastData = fetchHourlyForecastFromApi(latitude, longitude, units, lang);
            forecastData = forecastData.stream()
                    .filter(f -> f.getDt() >= startTime && f.getDt() <= endTime)
                    .collect(Collectors.toList());
            forecastData = deduplicateAndMergeForecasts(latitude, longitude, WeatherForecast.TYPE_HOURLY, forecastData);
            forecastRepository.saveAll(forecastData);
            log.info("Fetched and saved {} new hourly forecast records, forceRefresh={}", forecastData.size(), forceRefresh);
        }
        return forecastData.stream().map(this::convertToForecastDTO).collect(Collectors.toList());
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
    
    private List<WeatherForecastDTO> getDailyForecast(BigDecimal latitude, BigDecimal longitude, Long startTime, Long endTime, String units, String lang, Boolean forceRefresh) {
        int days = (int) ((endTime - startTime) / (24 * 60 * 60)) + 1;
        int cnt = Math.min(days, 16); // 每天一条，最多16天
        long dataCount = forecastRepository.countByCoordinatesAndTypeInTimeRange(
                latitude, longitude, WeatherForecast.TYPE_DAILY_16, startTime, endTime);
        int expectedCount = days; // 每天一条
        List<WeatherForecast> forecastData;
        // 100%完成度
        boolean shouldUseCache = dataCount == expectedCount && (forceRefresh == null || !forceRefresh);
        if (shouldUseCache) {
            forecastData = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                    latitude, longitude, WeatherForecast.TYPE_DAILY_16, startTime, endTime);
            log.info("Using {} cached 16天 daily forecast records", dataCount);
        } else {
            forecastData = fetchDailyForecastFromApi(latitude, longitude, units, lang, cnt);
            // 过滤出所需时间范围
            forecastData = forecastData.stream()
                    .filter(f -> f.getDt() >= startTime && f.getDt() <= endTime)
                    .collect(Collectors.toList());
            forecastData = deduplicateAndMergeForecasts(latitude, longitude, WeatherForecast.TYPE_DAILY_16, forecastData);
            forecastRepository.saveAll(forecastData);
            log.info("Fetched and saved {} new 16天 daily forecast records, forceRefresh={}", forecastData.size(), forceRefresh);
        }
        return forecastData.stream().map(this::convertToForecastDTO).collect(Collectors.toList());
    }

    private List<WeatherForecast> fetchDailyForecastFromApi(BigDecimal latitude, BigDecimal longitude, String units, String lang, int cnt) {
        cnt = Math.min(cnt, 16); // 官方最大16天
        String url = UriComponentsBuilder.fromHttpUrl(weatherConfig.getDailyForecastUrl())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("units", units)
                .queryParam("lang", lang)
                .queryParam("cnt", cnt)
                .queryParam("appid", weatherConfig.getApiKey())
                .build()
                .toUriString();
        log.info("调用16天API，实际请求URL: {}", url);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        List<WeatherForecast> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode list = root.path("list");
            for (JsonNode item : list) {
                WeatherForecast forecast = new WeatherForecast();
                forecast.setLatitude(latitude);
                forecast.setLongitude(longitude);
                forecast.setForecastType(WeatherForecast.TYPE_DAILY_16);
                forecast.setDt(item.path("dt").asLong());
                
                // 温度是一个对象，包含不同时间段的温度
                JsonNode temp = item.path("temp");
                forecast.setTemp(BigDecimal.valueOf(temp.path("day").asDouble()));      // 白天温度
                forecast.setTempMin(BigDecimal.valueOf(temp.path("min").asDouble()));   // 最低温度 
                forecast.setTempMax(BigDecimal.valueOf(temp.path("max").asDouble()));   // 最高温度
                
                // 体感温度也是一个对象
                JsonNode feelsLike = item.path("feels_like");
                forecast.setFeelsLike(BigDecimal.valueOf(feelsLike.path("day").asDouble())); // 白天体感温度
                
                forecast.setPressure(item.path("pressure").asInt());
                forecast.setHumidity(item.path("humidity").asInt());
                forecast.setWindSpeed(BigDecimal.valueOf(item.path("speed").asDouble()));
                forecast.setWindDeg(item.path("deg").asInt());
                
                if (item.has("gust")) {
                    forecast.setWindGust(BigDecimal.valueOf(item.path("gust").asDouble()));
                }
                
                forecast.setCloudsAll(item.path("clouds").asInt());
                forecast.setPop(item.has("pop") ? BigDecimal.valueOf(item.path("pop").asDouble()) : null);
                
                if (item.has("rain")) {
                    forecast.setRain3h(BigDecimal.valueOf(item.path("rain").asDouble()));
                }
                
                if (item.has("snow")) {
                    forecast.setSnow3h(BigDecimal.valueOf(item.path("snow").asDouble()));
                }
                
                if (item.path("weather").isArray() && item.path("weather").size() > 0) {
                    JsonNode weather = item.path("weather").get(0);
                    forecast.setWeatherId(weather.path("id").asInt());
                    forecast.setWeatherMain(weather.path("main").asText());
                    forecast.setWeatherDescription(weather.path("description").asText());
                    forecast.setWeatherIcon(weather.path("icon").asText());
                }
                
                // 设置可读的日期时间
                LocalDateTime dateTime = Instant.ofEpochSecond(forecast.getDt())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                forecast.setDtTxt(dateTime.format(dtFormatter));
                
                result.add(forecast);
            }
            return result;
        } catch (Exception e) {
            log.error("Error parsing 16天 daily forecast API response: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching 16天 daily forecast data", e);
        }
    }

    private List<WeatherForecastDTO> getClimateForecast(BigDecimal latitude, BigDecimal longitude, Long startTime, Long endTime, String units, String lang, Boolean forceRefresh) {
        int days = (int) ((endTime - startTime) / (24 * 60 * 60)) + 1;
        int cnt = days; // 每天1个整点
        long dataCount = forecastRepository.countByCoordinatesAndTypeInTimeRange(
                latitude, longitude, WeatherForecast.TYPE_CLIMATE_30, startTime, endTime);
        int expectedCount = days;
        List<WeatherForecast> forecastData;
        // 100%完成度
        boolean shouldUseCache = dataCount == expectedCount && (forceRefresh == null || !forceRefresh);
        if (shouldUseCache) {
            forecastData = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                    latitude, longitude, WeatherForecast.TYPE_CLIMATE_30, startTime, endTime);
            log.info("Using {} cached 30天 climate forecast records", dataCount);
        } else {
            forecastData = fetchClimateForecastFromApi(latitude, longitude, units, lang, cnt);
            forecastData = forecastData.stream()
                    .filter(f -> f.getDt() >= startTime && f.getDt() <= endTime)
                    .filter(f -> {
                        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(f.getDt()), ZoneId.systemDefault());
                        return dt.getHour() == 12;
                    })
                    .collect(Collectors.toList());
            forecastData = deduplicateAndMergeForecasts(latitude, longitude, WeatherForecast.TYPE_CLIMATE_30, forecastData);
            forecastRepository.saveAll(forecastData);
            log.info("Fetched and saved {} new 30天 climate forecast records, forceRefresh={}", forecastData.size(), forceRefresh);
        }
        return forecastData.stream().map(this::convertToForecastDTO).collect(Collectors.toList());
    }

    private List<WeatherForecast> fetchClimateForecastFromApi(BigDecimal latitude, BigDecimal longitude, String units, String lang, int cnt) {
        String url = UriComponentsBuilder.fromHttpUrl(weatherConfig.getClimateForecastUrl())
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .queryParam("units", units)
                .queryParam("lang", lang)
                .queryParam("cnt", cnt)
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
                forecast.setForecastType(WeatherForecast.TYPE_CLIMATE_30);
                forecast.setDt(item.path("dt").asLong());
                JsonNode temp = item.path("temp");
                forecast.setTemp(BigDecimal.valueOf(temp.path("day").asDouble()));
                forecast.setTempMin(BigDecimal.valueOf(temp.path("min").asDouble()));
                forecast.setTempMax(BigDecimal.valueOf(temp.path("max").asDouble()));
                forecast.setPressure(item.path("pressure").asInt());
                forecast.setHumidity(item.path("humidity").asInt());
                forecast.setWindSpeed(BigDecimal.valueOf(item.path("speed").asDouble()));
                forecast.setWindDeg(item.path("deg").asInt());
                forecast.setCloudsAll(item.path("clouds").asInt());
                forecast.setPop(item.has("pop") ? BigDecimal.valueOf(item.path("pop").asDouble()) : null);
                if (item.has("rain")) forecast.setRain3h(BigDecimal.valueOf(item.path("rain").asDouble()));
                if (item.has("snow")) forecast.setSnow3h(BigDecimal.valueOf(item.path("snow").asDouble()));
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
            log.error("Error parsing 30天 climate forecast API response", e);
            throw new RuntimeException("Error fetching 30天 climate forecast data", e);
        }
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