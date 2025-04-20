package org.agro.service;

import lombok.extern.slf4j.Slf4j;
import org.agro.dto.WeatherRequestDTO;
import org.agro.dto.WeatherForecastDTO;
import org.agro.entity.Field;
import org.agro.repository.FieldRepository;
import org.agro.repository.WeatherCurrentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    private final FieldRepository fieldRepository;

    @Autowired
    public WeatherDataSyncService(WeatherService weatherService,
                                 WeatherCurrentRepository currentRepository,
                                 FieldRepository fieldRepository) {
        this.weatherService = weatherService;
        this.currentRepository = currentRepository;
        this.fieldRepository = fieldRepository;
    }

    /**
     * 每10分钟更新一次所有地块的实时天气数据
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    public void syncCurrentWeatherData() {
        log.info("开始同步实时天气数据...");

        // 从数据库获取所有地块
        List<Field> fields = fieldRepository.findAll();
        
        if (fields.isEmpty()) {
            log.warn("数据库中没有地块信息，无法同步天气数据");
            return;
        }

        for (Field field : fields) {
            try {
                WeatherRequestDTO request = new WeatherRequestDTO();
                request.setLatitude(field.getLatitude());
                request.setLongitude(field.getLongitude());

                // 调用服务获取最新天气数据（该方法会自动保存到数据库）
                weatherService.getCurrentWeather(request);

                log.info("成功更新地块[{}]({}, {})的实时天气数据", 
                         field.getName(), field.getLatitude(), field.getLongitude());
            } catch (Exception e) {
                log.error("更新地块[{}]({}, {})的实时天气数据失败", 
                         field.getName(), field.getLatitude(), field.getLongitude(), e);
            }
        }

        log.info("实时天气数据同步完成，共同步{}个地块", fields.size());
    }

    /**
     * 每天凌晨2点更新一次未来预报数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncForecastData() {
        log.info("开始同步天气预报数据...");

        // 从数据库获取所有地块
        List<Field> fields = fieldRepository.findAll();
        
        if (fields.isEmpty()) {
            log.warn("数据库中没有地块信息，无法同步天气预报数据");
            return;
        }
        
        for (Field field : fields) {
            try {
                WeatherRequestDTO request = new WeatherRequestDTO();
                request.setLatitude(field.getLatitude());
                request.setLongitude(field.getLongitude());
                
                // 调用WeatherService的getWeatherForecast方法获取预报数据
                List<WeatherForecastDTO> forecastList = weatherService.getWeatherForecast(request);
                
                log.info("成功更新地块[{}]({}, {})的天气预报数据，获取到{}条预报记录", 
                         field.getName(), field.getLatitude(), field.getLongitude(), 
                         forecastList != null ? forecastList.size() : 0);
            } catch (Exception e) {
                log.error("更新地块[{}]({}, {})的天气预报数据失败", 
                         field.getName(), field.getLatitude(), field.getLongitude(), e);
            }
        }

        log.info("天气预报数据同步完成，共同步{}个地块", fields.size());
    }
}
