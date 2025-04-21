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
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;
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
        
        // 初始化weatherData变量
        WeatherCurrent weatherData = null;
        
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
                
                // 查询是否已存在相同坐标和时间戳的记录
                Optional<WeatherCurrent> existingRecord = currentRepository.findByLatitudeAndLongitudeAndDt(
                        latitude, longitude, weatherData.getDt());
                
                if (existingRecord.isPresent()) {
                    // 如果存在相同时间戳的数据，直接使用已存在的记录
                    log.info("Found existing weather data with same timestamp, using it instead of inserting new record");
                    weatherData = existingRecord.get();
                } else {
                    try {
                        // 保存到数据库
                        weatherData = currentRepository.save(weatherData);
                        log.info("Successfully saved weather data for lat={}, lon={}, dt={}, forceRefresh={}", 
                                latitude, longitude, weatherData.getDt(), request.getForceRefresh());
                    } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                        // 唯一约束冲突，可能是并发插入导致，尝试重新查询
                        log.warn("Constraint violation detected when saving weather data: {}", e.getMessage());
                        
                        // 延迟一小段时间，确保数据已提交
                        try {
                            Thread.sleep(15);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        
                        // 1. 首先尝试精确查询（相同坐标和时间戳）
                        Optional<WeatherCurrent> conflictRecord = currentRepository.findByLatitudeAndLongitudeAndDt(
                                latitude, longitude, weatherData.getDt());
                        
                        if (conflictRecord.isPresent()) {
                            weatherData = conflictRecord.get();
                            log.info("Successfully retrieved existing record after constraint violation");
                        } else {
                            // 2. 如果精确查询找不到，尝试根据经纬度查找最新记录
                            List<WeatherCurrent> latestRecords = currentRepository.findTopByCoordinatesOrderByDtDesc(
                                    latitude, longitude);
                            if (!latestRecords.isEmpty()) {
                                weatherData = latestRecords.get(0);
                                log.info("Using latest available record as fallback after constraint violation, dt={}", weatherData.getDt());
                            } else if (latestData.isPresent()) {
                                // 3. 使用查询时的最新记录
                                weatherData = latestData.get();
                                log.info("Using initial query result as fallback after constraint violation");
                            } else {
                                // 由于我们已经获取了API的数据，直接使用它而不抛出异常
                                log.warn("Unable to find any existing record after constraint violation, using API data without persisting");
                                // 不修改weatherData，继续使用API返回的数据
                            }
                        }
                    } catch (Exception e) {
                        // 其他类型的异常
                        log.error("Failed to save weather data: {}", e.getMessage(), e);
                        if (latestData.isPresent()) {
                            weatherData = latestData.get();
                            log.info("Using cached data due to save error");
                        } else {
                            // 继续使用API数据
                            log.warn("Using API data without persisting due to error");
                        }
                    }
                }
            } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                // 顶层捕获唯一约束冲突
                log.warn("Constraint violation detected at top level: {}", e.getMessage());
                
                Long dt = (weatherData != null) ? weatherData.getDt() : null;
                
                if (dt != null) {
                    // 多次尝试查询
                    for (int i = 0; i < 3; i++) {
                        try {
                            Thread.sleep(100 * (i + 1));  // 递增延迟
                            
                            Optional<WeatherCurrent> conflictRecord = currentRepository.findByLatitudeAndLongitudeAndDt(
                                    latitude, longitude, dt);
                            
                            if (conflictRecord.isPresent()) {
                                weatherData = conflictRecord.get();
                                log.info("Successfully retrieved existing record at top level after {} retries", i);
                                break;
                            }
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                
                // 如果重试后仍未找到记录
                if (weatherData == null || weatherData.getId() == null) {
                    // 尝试查找最新记录
                    List<WeatherCurrent> latestRecords = currentRepository.findTopByCoordinatesOrderByDtDesc(
                            latitude, longitude);
                    if (!latestRecords.isEmpty()) {
                        weatherData = latestRecords.get(0);
                        log.info("Using latest available record as fallback at top level");
                    } else if (latestData.isPresent()) {
                        // 回退使用最新缓存数据
                        weatherData = latestData.get();
                        log.info("Using cached data as fallback at top level");
                    }
                    // 如果所有方法都失败，继续使用API获取的数据
                }
            } catch (Exception e) {
                // 处理可能的API调用异常或数据库操作异常
                log.error("Error during weather data operation: {}", e.getMessage(), e);
                
                // 如果有缓存数据，则回退使用缓存数据
                if (latestData.isPresent()) {
                    weatherData = latestData.get();
                    log.info("Using cached data due to error for lat={}, lon={}", latitude, longitude);
                }
                // 否则继续使用API数据（如果已获取）或抛出异常（如果API调用也失败）
                else if (weatherData == null) {
                    throw new RuntimeException("Unable to fetch weather data", e);
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
            
            // 添加处理sea_level和grnd_level字段
            if (!root.path("main").path("sea_level").isMissingNode()) {
                weatherCurrent.setSeaLevel(root.path("main").path("sea_level").asInt());
            }
            if (!root.path("main").path("grnd_level").isMissingNode()) {
                weatherCurrent.setGrndLevel(root.path("main").path("grnd_level").asInt());
            }
            
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
            
            // 添加处理country, sunrise, sunset, timezone和name字段
            if (!root.path("sys").isMissingNode()) {
                if (!root.path("sys").path("country").isMissingNode()) {
                    weatherCurrent.setCountry(root.path("sys").path("country").asText());
                }
                if (!root.path("sys").path("sunrise").isMissingNode()) {
                    weatherCurrent.setSunrise(root.path("sys").path("sunrise").asLong());
                }
                if (!root.path("sys").path("sunset").isMissingNode()) {
                    weatherCurrent.setSunset(root.path("sys").path("sunset").asLong());
                }
            }
            
            if (!root.path("timezone").isMissingNode()) {
                weatherCurrent.setTimezone(root.path("timezone").asInt());
            }
            
            if (!root.path("name").isMissingNode()) {
                weatherCurrent.setName(root.path("name").asText());
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
            try {
                forecastData = fetchHourlyForecastFromApi(latitude, longitude, units, lang);
                forecastData = forecastData.stream()
                        .filter(f -> f.getDt() >= startTime && f.getDt() <= endTime)
                        .collect(Collectors.toList());
                forecastData = deduplicateAndMergeForecasts(latitude, longitude, WeatherForecast.TYPE_HOURLY, forecastData);
                
                try {
                    // 逐个保存，避免批量保存时的唯一约束冲突
                    List<WeatherForecast> result = new ArrayList<>();
                    for (WeatherForecast forecast : forecastData) {
                        try {
                            // 先检查是否已存在
                            Optional<WeatherForecast> existing = forecastRepository.findByCoordinatesAndTypeAndDt(
                                    latitude, longitude, WeatherForecast.TYPE_HOURLY, forecast.getDt());
                            
                            if (existing.isPresent()) {
                                // 如果存在，直接使用现有记录
                                result.add(existing.get());
                                log.debug("Using existing hourly forecast record for dt={}", forecast.getDt());
                            } else {
                                // 如果不存在，保存新记录
                                result.add(forecastRepository.save(forecast));
                                log.debug("Saved new hourly forecast record for dt={}", forecast.getDt());
                            }
                        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                            // 处理唯一约束冲突
                            log.warn("Constraint violation when saving hourly forecast for dt={}: {}", 
                                    forecast.getDt(), e.getMessage());
                            
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                            
                            // 尝试查找冲突记录
                            Optional<WeatherForecast> conflictRecord = forecastRepository.findByCoordinatesAndTypeAndDt(
                                    latitude, longitude, WeatherForecast.TYPE_HOURLY, forecast.getDt());
                            
                            if (conflictRecord.isPresent()) {
                                result.add(conflictRecord.get());
                                log.info("Retrieved existing hourly forecast after constraint violation for dt={}", 
                                        forecast.getDt());
                            } else {
                                // 如果找不到，使用原始对象（不含ID）
                                log.warn("Could not find existing record after constraint violation, using original data for dt={}", 
                                        forecast.getDt());
                                result.add(forecast);
                            }
                        } catch (Exception e) {
                            // 其他异常
                            log.error("Error saving hourly forecast for dt={}: {}", 
                                    forecast.getDt(), e.getMessage());
                            result.add(forecast); // 添加原始对象
                        }
                    }
                    forecastData = result;
                    log.info("Processed {} hourly forecast records with individual save", forecastData.size());
                } catch (Exception e) {
                    // 如果整个保存过程失败，记录错误但继续使用API数据
                    log.error("Failed to save hourly forecast data: {}", e.getMessage(), e);
                }
            } catch (Exception e) {
                // 处理API调用或其他处理错误
                log.error("Error fetching hourly forecast data: {}", e.getMessage(), e);
                
                // 尝试从数据库获取可用数据
                forecastData = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                        latitude, longitude, WeatherForecast.TYPE_HOURLY, startTime, endTime);
                
                if (forecastData.isEmpty()) {
                    log.warn("No hourly forecast data available in database, returning empty list");
                } else {
                    log.info("Using {} existing hourly forecast records as fallback", forecastData.size());
                }
            }
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
                
                if (!item.path("rain").isMissingNode()) {
                    // 处理降雨，优先使用1h字段，如果没有则使用3h字段并将值除以3（大致估算）
                    if (!item.path("rain").path("1h").isMissingNode()) {
                        forecast.setRain1h(BigDecimal.valueOf(item.path("rain").path("1h").asDouble()));
                    } else if (!item.path("rain").path("3h").isMissingNode()) {
                        // 3h降雨量近似转换为1h降雨量（简单除以3）
                        BigDecimal rain3h = BigDecimal.valueOf(item.path("rain").path("3h").asDouble());
                        forecast.setRain1h(rain3h.divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP));
                    }
                }
                
                if (!item.path("snow").isMissingNode()) {
                    // 处理降雪，逻辑同降雨
                    if (!item.path("snow").path("1h").isMissingNode()) {
                        forecast.setSnow1h(BigDecimal.valueOf(item.path("snow").path("1h").asDouble()));
                    } else if (!item.path("snow").path("3h").isMissingNode()) {
                        BigDecimal snow3h = BigDecimal.valueOf(item.path("snow").path("3h").asDouble());
                        forecast.setSnow1h(snow3h.divide(BigDecimal.valueOf(3), 2, BigDecimal.ROUND_HALF_UP));
                    }
                }
                
                if (item.path("weather").isArray() && item.path("weather").size() > 0) {
                    JsonNode weather = item.path("weather").get(0);
                    forecast.setWeatherId(weather.path("id").asInt());
                    forecast.setWeatherMain(weather.path("main").asText());
                    forecast.setWeatherDescription(weather.path("description").asText());
                    forecast.setWeatherIcon(weather.path("icon").asText());
                }
                
                // 设置可读的日期时间
                if (item.has("dt_txt")) {
                    forecast.setDtTxt(item.path("dt_txt").asText());
                } else {
                    LocalDateTime dateTime = Instant.ofEpochSecond(forecast.getDt())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    forecast.setDtTxt(dateTime.format(dtFormatter));
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
            try {
                forecastData = fetchDailyForecastFromApi(latitude, longitude, units, lang, cnt);
                // 过滤出所需时间范围
                forecastData = forecastData.stream()
                        .filter(f -> f.getDt() >= startTime && f.getDt() <= endTime)
                        .collect(Collectors.toList());
                forecastData = deduplicateAndMergeForecasts(latitude, longitude, WeatherForecast.TYPE_DAILY_16, forecastData);
                
                try {
                    // 逐个保存，避免批量保存时的唯一约束冲突
                    List<WeatherForecast> result = new ArrayList<>();
                    for (WeatherForecast forecast : forecastData) {
                        try {
                            // 先检查是否已存在
                            Optional<WeatherForecast> existing = forecastRepository.findByCoordinatesAndTypeAndDt(
                                    latitude, longitude, WeatherForecast.TYPE_DAILY_16, forecast.getDt());
                            
                            if (existing.isPresent()) {
                                // 如果存在，直接使用现有记录
                                result.add(existing.get());
                                log.debug("Using existing daily forecast record for dt={}", forecast.getDt());
                            } else {
                                // 如果不存在，保存新记录
                                result.add(forecastRepository.save(forecast));
                                log.debug("Saved new daily forecast record for dt={}", forecast.getDt());
                            }
                        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                            // 处理唯一约束冲突
                            log.warn("Constraint violation when saving daily forecast for dt={}: {}", 
                                    forecast.getDt(), e.getMessage());
                            
                            try {
                                Thread.sleep(45);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                            
                            // 尝试查找冲突记录
                            Optional<WeatherForecast> conflictRecord = forecastRepository.findByCoordinatesAndTypeAndDt(
                                    latitude, longitude, WeatherForecast.TYPE_DAILY_16, forecast.getDt());
                            
                            if (conflictRecord.isPresent()) {
                                result.add(conflictRecord.get());
                                log.info("Retrieved existing daily forecast after constraint violation for dt={}", 
                                        forecast.getDt());
                            } else {
                                // 如果找不到，使用原始对象（不含ID）
                                log.warn("Could not find existing record after constraint violation, using original data for dt={}", 
                                        forecast.getDt());
                                result.add(forecast);
                            }
                        } catch (Exception e) {
                            // 其他异常
                            log.error("Error saving daily forecast for dt={}: {}", 
                                    forecast.getDt(), e.getMessage());
                            result.add(forecast); // 添加原始对象
                        }
                    }
                    forecastData = result;
                    log.info("Processed {} daily forecast records with individual save", forecastData.size());
                } catch (Exception e) {
                    // 如果整个保存过程失败，记录错误但继续使用API数据
                    log.error("Failed to save daily forecast data: {}", e.getMessage(), e);
                }
            } catch (Exception e) {
                // 处理API调用或其他处理错误
                log.error("Error fetching daily forecast data: {}", e.getMessage(), e);
                
                // 尝试从数据库获取可用数据
                forecastData = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                        latitude, longitude, WeatherForecast.TYPE_DAILY_16, startTime, endTime);
                
                if (forecastData.isEmpty()) {
                    log.warn("No daily forecast data available in database, returning empty list");
                } else {
                    log.info("Using {} existing daily forecast records as fallback", forecastData.size());
                }
            }
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
                
                // 添加sunrise和sunset
                if (!item.path("sunrise").isMissingNode()) {
                    forecast.setSunrise(item.path("sunrise").asLong());
                }
                if (!item.path("sunset").isMissingNode()) {
                    forecast.setSunset(item.path("sunset").asLong());
                }
                
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
                    // 将daily中的rain值存入rain1h
                    forecast.setRain1h(BigDecimal.valueOf(item.path("rain").asDouble()));
                }
                
                if (item.has("snow")) {
                    // 将daily中的snow值存入snow1h
                    forecast.setSnow1h(BigDecimal.valueOf(item.path("snow").asDouble()));
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
            try {
                forecastData = fetchClimateForecastFromApi(latitude, longitude, units, lang, cnt);
                forecastData = forecastData.stream()
                        .filter(f -> f.getDt() >= startTime && f.getDt() <= endTime)
                        .filter(f -> {
                            LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochSecond(f.getDt()), ZoneId.systemDefault());
                            return dt.getHour() == 12;
                        })
                        .collect(Collectors.toList());
                forecastData = deduplicateAndMergeForecasts(latitude, longitude, WeatherForecast.TYPE_CLIMATE_30, forecastData);
                
                try {
                    // 逐个保存，避免批量保存时的唯一约束冲突
                    List<WeatherForecast> result = new ArrayList<>();
                    for (WeatherForecast forecast : forecastData) {
                        try {
                            // 先检查是否已存在
                            Optional<WeatherForecast> existing = forecastRepository.findByCoordinatesAndTypeAndDt(
                                    latitude, longitude, WeatherForecast.TYPE_CLIMATE_30, forecast.getDt());
                            
                            if (existing.isPresent()) {
                                // 如果存在，直接使用现有记录
                                result.add(existing.get());
                                log.debug("Using existing climate forecast record for dt={}", forecast.getDt());
                            } else {
                                // 如果不存在，保存新记录
                                result.add(forecastRepository.save(forecast));
                                log.debug("Saved new climate forecast record for dt={}", forecast.getDt());
                            }
                        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
                            // 处理唯一约束冲突
                            log.warn("Constraint violation when saving climate forecast for dt={}: {}", 
                                    forecast.getDt(), e.getMessage());
                            
                            try {
                                Thread.sleep(45);
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                            
                            // 尝试查找冲突记录
                            Optional<WeatherForecast> conflictRecord = forecastRepository.findByCoordinatesAndTypeAndDt(
                                    latitude, longitude, WeatherForecast.TYPE_CLIMATE_30, forecast.getDt());
                            
                            if (conflictRecord.isPresent()) {
                                result.add(conflictRecord.get());
                                log.info("Retrieved existing climate forecast after constraint violation for dt={}", 
                                        forecast.getDt());
                            } else {
                                // 如果找不到，使用原始对象（不含ID）
                                log.warn("Could not find existing record after constraint violation, using original data for dt={}", 
                                        forecast.getDt());
                                result.add(forecast);
                            }
                        } catch (Exception e) {
                            // 其他异常
                            log.error("Error saving climate forecast for dt={}: {}", 
                                    forecast.getDt(), e.getMessage());
                            result.add(forecast); // 添加原始对象
                        }
                    }
                    forecastData = result;
                    log.info("Processed {} climate forecast records with individual save", forecastData.size());
                } catch (Exception e) {
                    // 如果整个保存过程失败，记录错误但继续使用API数据
                    log.error("Failed to save climate forecast data: {}", e.getMessage(), e);
                }
            } catch (Exception e) {
                // 处理API调用或其他处理错误
                log.error("Error fetching climate forecast data: {}", e.getMessage(), e);
                
                // 尝试从数据库获取可用数据
                forecastData = forecastRepository.findByCoordinatesAndTypeInTimeRange(
                        latitude, longitude, WeatherForecast.TYPE_CLIMATE_30, startTime, endTime);
                
                if (forecastData.isEmpty()) {
                    log.warn("No climate forecast data available in database, returning empty list");
                } else {
                    log.info("Using {} existing climate forecast records as fallback", forecastData.size());
                }
            }
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
                
                // 添加sunrise和sunset
                if (!item.path("sunrise").isMissingNode()) {
                    forecast.setSunrise(item.path("sunrise").asLong());
                }
                if (!item.path("sunset").isMissingNode()) {
                    forecast.setSunset(item.path("sunset").asLong());
                }
                
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
                
                if (item.has("rain")) {
                    // 将climate中的rain值存入rain1h
                    forecast.setRain1h(BigDecimal.valueOf(item.path("rain").asDouble()));
                }
                
                if (item.has("snow")) {
                    // 将climate中的snow值存入snow1h
                    forecast.setSnow1h(BigDecimal.valueOf(item.path("snow").asDouble()));
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
        
        // 如果存在location name，则优先使用name字段，否则使用locationName
        if (entity.getName() != null && !entity.getName().isEmpty()) {
            dto.setLocationName(entity.getName());
        }
        
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