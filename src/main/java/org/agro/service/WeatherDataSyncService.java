package org.agro.service;

import lombok.extern.slf4j.Slf4j;
import org.agro.dto.WeatherForecastDTO;
import org.agro.dto.WeatherRequestDTO;
import org.agro.entity.Field;
import org.agro.repository.FieldRepository;
import org.agro.repository.WeatherCurrentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 天气数据同步服务
 * 负责定时从 OpenWeatherMap API 获取数据，并更新到数据库
 */
@Slf4j
@Service
public class WeatherDataSyncService implements SchedulingConfigurer {

    private final WeatherService weatherService;
    private final WeatherCurrentRepository currentRepository;
    private final FieldRepository fieldRepository;
    private final SystemConfigService systemConfigService;

    @Autowired
    public WeatherDataSyncService(WeatherService weatherService,
                                 WeatherCurrentRepository currentRepository,
                                 FieldRepository fieldRepository,
                                 SystemConfigService systemConfigService) {
        this.weatherService = weatherService;
        this.currentRepository = currentRepository;
        this.fieldRepository = fieldRepository;
        this.systemConfigService = systemConfigService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 配置动态调度任务，采用从SystemConfigService获取的时间间隔
        taskRegistrar.addTriggerTask(
            // 要执行的任务
            this::syncCurrentWeatherData,
            // 触发器（根据配置的分钟数计算）
            triggerContext -> {
                int intervalMinutes = systemConfigService.getDataFetchInterval();
                // 确保最小值为1分钟
                intervalMinutes = Math.max(1, intervalMinutes);
                String cronExpression = String.format("0 0/%d * * * ?", intervalMinutes);
                log.debug("使用数据同步频率: {} 分钟, cron表达式: {}", intervalMinutes, cronExpression);
                return new CronTrigger(cronExpression).nextExecutionTime(triggerContext);
            }
        );
    }

    /**
     * 同步实时天气数据
     * 注意：此方法不再使用@Scheduled注解，而是通过SchedulingConfigurer动态配置
     */
    public void syncCurrentWeatherData() {
        log.info("开始同步实时天气数据...");

        // 从数据库获取所有地块
        List<Field> fields = fieldRepository.findAll();

        if (fields.isEmpty()) {
            log.warn("数据库中没有地块信息，无需同步天气数据");
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
