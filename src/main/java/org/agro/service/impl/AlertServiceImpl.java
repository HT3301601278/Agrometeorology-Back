package org.agro.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.agro.dto.AlertRecordDTO;
import org.agro.dto.AlertRuleDTO;
import org.agro.dto.WeatherForecastDTO;
import org.agro.dto.WeatherRequestDTO;
import org.agro.entity.AlertRecord;
import org.agro.entity.AlertRule;
import org.agro.entity.Field;
import org.agro.entity.User;
import org.agro.entity.WeatherForecast;
import org.agro.repository.AlertRecordRepository;
import org.agro.repository.AlertRuleRepository;
import org.agro.repository.FieldRepository;
import org.agro.repository.UserRepository;
import org.agro.repository.WeatherForecastRepository;
import org.agro.security.SecurityUtils;
import org.agro.service.AlertService;
import org.agro.service.NotificationService;
import org.agro.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlertServiceImpl implements AlertService {

    private final AlertRuleRepository ruleRepository;
    private final AlertRecordRepository recordRepository;
    private final WeatherService weatherService;
    private final FieldRepository fieldRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final WeatherForecastRepository forecastRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int NOTIFICATION_TYPE_ALERT = 2; // 预警通知类型
    
    @Autowired
    public AlertServiceImpl(AlertRuleRepository ruleRepository,
                          AlertRecordRepository recordRepository,
                          WeatherService weatherService,
                          FieldRepository fieldRepository,
                          UserRepository userRepository,
                          NotificationService notificationService,
                          WeatherForecastRepository forecastRepository) {
        this.ruleRepository = ruleRepository;
        this.recordRepository = recordRepository;
        this.weatherService = weatherService;
        this.fieldRepository = fieldRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.forecastRepository = forecastRepository;
    }

    @Override
    @Transactional
    public AlertRuleDTO createRule(AlertRuleDTO ruleDTO) {
        // 获取当前用户
        Long userId = SecurityUtils.getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("无法获取当前用户信息"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证字段存在
        Field field = fieldRepository.findById(ruleDTO.getFieldId())
                .orElseThrow(() -> new RuntimeException("地块不存在: " + ruleDTO.getFieldId()));
        
        // 创建规则
        AlertRule rule = ruleDTO.toEntity();
        rule.setUser(user);
        rule.setField(field);
        
        rule = ruleRepository.save(rule);
        return AlertRuleDTO.fromEntity(rule);
    }

    @Override
    @Transactional
    public AlertRuleDTO updateRule(Long ruleId, AlertRuleDTO ruleDTO) {
        AlertRule existingRule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("预警规则不存在: " + ruleId));
        
        // 验证字段存在
        Field field = fieldRepository.findById(ruleDTO.getFieldId())
                .orElseThrow(() -> new RuntimeException("地块不存在: " + ruleDTO.getFieldId()));
        
        // 更新规则
        AlertRule updatedRule = ruleDTO.toEntity();
        updatedRule.setId(existingRule.getId());
        updatedRule.setCreatedAt(existingRule.getCreatedAt());
        updatedRule.setUser(existingRule.getUser());
        updatedRule.setField(field);
        
        updatedRule = ruleRepository.save(updatedRule);
        return AlertRuleDTO.fromEntity(updatedRule);
    }

    @Override
    @Transactional
    public void deleteRule(Long ruleId) {
        if (ruleRepository.existsById(ruleId)) {
            ruleRepository.deleteById(ruleId);
        } else {
            throw new RuntimeException("预警规则不存在: " + ruleId);
        }
    }

    @Override
    @Transactional
    public AlertRuleDTO toggleRuleStatus(Long ruleId, boolean enabled) {
        AlertRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("预警规则不存在: " + ruleId));
        
        rule.setEnabled(enabled);
        rule = ruleRepository.save(rule);
        
        return AlertRuleDTO.fromEntity(rule);
    }

    @Override
    public AlertRuleDTO getRuleById(Long ruleId) {
        AlertRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("预警规则不存在: " + ruleId));
        
        return AlertRuleDTO.fromEntity(rule);
    }

    @Override
    public Page<AlertRuleDTO> getAllRules(Pageable pageable) {
        Page<AlertRule> rulePage = ruleRepository.findAll(pageable);
        return rulePage.map(AlertRuleDTO::fromEntity);
    }

    @Override
    public Page<AlertRecordDTO> getAllAlertRecords(Pageable pageable) {
        Page<AlertRecord> recordPage = recordRepository.findAllByOrderByCreatedAtDesc(pageable);
        return recordPage.map(AlertRecordDTO::fromEntity);
    }

    @Override
    public AlertRecordDTO getAlertRecordById(Long recordId) {
        AlertRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("预警记录不存在: " + recordId));
        
        return AlertRecordDTO.fromEntity(record);
    }

    @Override
    public List<AlertRecordDTO> getRecentAlerts(LocalDateTime startTime) {
        List<AlertRecord> records = recordRepository.findRecentAlerts(startTime);
        return records.stream()
                .map(AlertRecordDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AlertRecord> checkForecastAndGenerateAlerts() {
        log.info("开始检查天气预报并生成预警...");
        List<AlertRecord> generatedAlerts = new ArrayList<>();
        
        // 获取所有已启用的预警规则
        List<AlertRule> activeRules = ruleRepository.findByEnabledTrue();
        if (activeRules.isEmpty()) {
            log.info("没有启用的预警规则，跳过检查");
            return generatedAlerts;
        }
        
        // 获取所有地块
        List<Field> fields = fieldRepository.findAll();
        if (fields.isEmpty()) {
            log.warn("没有地块数据，无法生成预警");
            return generatedAlerts;
        }
        
        for (Field field : fields) {
            try {
                // 获取该地块的预报数据（默认7天）
                WeatherRequestDTO request = new WeatherRequestDTO();
                request.setLatitude(field.getLatitude());
                request.setLongitude(field.getLongitude());
                
                List<WeatherForecastDTO> forecastList = weatherService.getWeatherForecast(request);
                
                if (forecastList == null || forecastList.isEmpty()) {
                    log.warn("地块[{}]({}, {})无预报数据，跳过检查", 
                            field.getName(), field.getLatitude(), field.getLongitude());
                    continue;
                }
                
                log.info("获取到地块[{}]的{}条预报数据，开始检查预警规则", 
                        field.getName(), forecastList.size());
                
                // 对每条预报数据应用所有规则
                for (WeatherForecastDTO forecast : forecastList) {
                    for (AlertRule rule : activeRules) {
                        // 只检查与当前字段相关的规则
                        if (rule.getField().getId().equals(field.getId())) {
                            // 检查是否满足预警条件
                            if (matchesAlertCondition(forecast, rule)) {
                                // 创建预警记录
                                AlertRecord alertRecord = createAlertRecord(forecast, rule, field);
                                if (alertRecord != null) {
                                    generatedAlerts.add(alertRecord);
                                    
                                    // 发送通知
                                    sendAlertNotification(alertRecord);
                                }
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                log.error("检查地块[{}]的预警时发生错误", field.getName(), e);
            }
        }
        
        log.info("预警检查完成，生成了{}条预警记录", generatedAlerts.size());
        return generatedAlerts;
    }
    
    /**
     * 检查天气预报数据是否匹配预警规则条件
     */
    private boolean matchesAlertCondition(WeatherForecastDTO forecast, AlertRule rule) {
        try {
            // 获取第一个参数值
            BigDecimal value1 = getValueFromForecast(forecast, rule.getParamName());
            if (value1 == null) {
                return false;
            }
            
            // 第一个条件判断
            boolean firstCondition = evaluateCondition(value1, rule.getOperator(), rule.getThreshold());
            
            // 如果没有第二个条件，直接返回第一个条件的结果
            if (rule.getParamName2() == null || rule.getOperator2() == null || rule.getThreshold2() == null) {
                return firstCondition;
            }
            
            // 获取第二个参数值
            BigDecimal value2 = getValueFromForecast(forecast, rule.getParamName2());
            if (value2 == null) {
                return false;
            }
            
            // 第二个条件判断
            boolean secondCondition = evaluateCondition(value2, rule.getOperator2(), rule.getThreshold2());
            
            // 两个条件都满足才返回true
            return firstCondition && secondCondition;
            
        } catch (Exception e) {
            log.error("评估预警条件时发生错误: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从预报数据中获取指定参数的值
     */
    private BigDecimal getValueFromForecast(WeatherForecastDTO forecast, String paramName) {
        if (paramName == null) {
            return null;
        }
        
        switch (paramName) {
            case "temp":
                return forecast.getTemp();
            case "feelsLike":
                return forecast.getFeelsLike();
            case "tempMin":
                return forecast.getTempMin();
            case "tempMax":
                return forecast.getTempMax();
            case "pressure":
                return forecast.getPressure() != null ? new BigDecimal(forecast.getPressure()) : null;
            case "humidity":
                return forecast.getHumidity() != null ? new BigDecimal(forecast.getHumidity()) : null;
            case "windSpeed":
                return forecast.getWindSpeed();
            case "windDeg":
                return forecast.getWindDeg() != null ? new BigDecimal(forecast.getWindDeg()) : null;
            case "windGust":
                return forecast.getWindGust();
            case "cloudsAll":
                return forecast.getCloudsAll() != null ? new BigDecimal(forecast.getCloudsAll()) : null;
            case "visibility":
                return forecast.getVisibility() != null ? new BigDecimal(forecast.getVisibility()) : null;
            case "pop":
                return forecast.getPop();
            case "rain1h":
                return forecast.getRain1h();
            case "snow1h":
                return forecast.getSnow1h();
            case "dayLength":
                // 计算日长（小时）
                if (forecast.getSunrise() != null && forecast.getSunset() != null) {
                    long dayLengthSeconds = forecast.getSunset() - forecast.getSunrise();
                    BigDecimal dayLengthHours = new BigDecimal(dayLengthSeconds).divide(new BigDecimal(3600), 2, RoundingMode.HALF_UP);
                    return dayLengthHours;
                }
                return null;
            case "vpd":
                // 计算VPD（蒸汽压亏缺）
                if (forecast.getTemp() != null && forecast.getHumidity() != null) {
                    return calculateVPD(forecast.getTemp(), new BigDecimal(forecast.getHumidity()));
                }
                return null;
            default:
                log.warn("未知的参数名称: {}", paramName);
                return null;
        }
    }
    
    /**
     * 计算蒸汽压亏缺（VPD）
     * VPD计算公式: es = 0.6108 * e^((17.27 * T) / (T + 237.3)), ea = RH/100 * es, VPD = es - ea
     */
    private BigDecimal calculateVPD(BigDecimal temp, BigDecimal humidity) {
        // 饱和蒸汽压 es (kPa)
        double es = 0.6108 * Math.exp((17.27 * temp.doubleValue()) / (temp.doubleValue() + 237.3));
        
        // 实际蒸汽压 ea (kPa)
        double ea = (humidity.doubleValue() / 100.0) * es;
        
        // 蒸汽压亏缺 VPD (kPa)
        double vpd = es - ea;
        
        return new BigDecimal(vpd).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 评估条件（值与阈值的比较）
     */
    private boolean evaluateCondition(BigDecimal value, String operator, BigDecimal threshold) {
        if (value == null || operator == null || threshold == null) {
            return false;
        }
        
        switch (operator) {
            case ">":
                return value.compareTo(threshold) > 0;
            case ">=":
                return value.compareTo(threshold) >= 0;
            case "<":
                return value.compareTo(threshold) < 0;
            case "<=":
                return value.compareTo(threshold) <= 0;
            case "==":
                return value.compareTo(threshold) == 0;
            default:
                log.warn("未知的操作符: {}", operator);
                return false;
        }
    }
    
    /**
     * 创建预警记录
     */
    private AlertRecord createAlertRecord(WeatherForecastDTO forecast, AlertRule rule, Field field) {
        // 检查是否已存在相同的预警记录（相同规则和预报时间）
        if (recordRepository.existsByRuleIdAndForecastDt(rule.getId(), forecast.getDt())) {
            log.debug("已存在相同的预警记录，跳过创建: 规则ID={}, 预报时间={}", rule.getId(), forecast.getDt());
            return null;
        }
        
        // 获取参数值
        BigDecimal value1 = getValueFromForecast(forecast, rule.getParamName());
        BigDecimal value2 = null;
        if (rule.getParamName2() != null) {
            value2 = getValueFromForecast(forecast, rule.getParamName2());
        }
        
        // 格式化预报日期
        String forecastDate = Instant.ofEpochSecond(forecast.getDt())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DATE_FORMATTER);
        
        // 创建预警消息
        String message = formatAlertMessage(rule, forecast, value1, value2, forecastDate, field);
        
        // 创建预警记录
        AlertRecord record = new AlertRecord();
        record.setRule(rule);
        record.setForecastDt(forecast.getDt());
        record.setParamValue(value1);
        record.setParamValue2(value2);
        record.setMessage(message);
        record.setForecastDate(forecastDate);
        record.setLatitude(field.getLatitude());
        record.setLongitude(field.getLongitude());
        
        return recordRepository.save(record);
    }
    
    /**
     * 格式化预警消息
     */
    private String formatAlertMessage(AlertRule rule, WeatherForecastDTO forecast, 
                                     BigDecimal value1, BigDecimal value2, 
                                     String forecastDate, Field field) {
        // 使用规则中的消息模板，替换占位符
        String message = rule.getMessage();
        
        // 替换占位符
        message = message.replace("{field}", field.getName())
                         .replace("{date}", forecastDate)
                         .replace("{param}", rule.getParamName())
                         .replace("{value}", value1.toString())
                         .replace("{threshold}", rule.getThreshold().toString());
        
        if (rule.getParamName2() != null && value2 != null) {
            message = message.replace("{param2}", rule.getParamName2())
                             .replace("{value2}", value2.toString())
                             .replace("{threshold2}", rule.getThreshold2().toString());
        }
        
        return message;
    }
    
    /**
     * 发送预警通知
     * 使用NotificationService发送通知，该服务会根据用户的通知设置决定是否发送邮件和系统通知
     */
    private void sendAlertNotification(AlertRecord alertRecord) {
        String title = "气象预警通知";
        // 使用sendNotificationToAllUsers方法，该方法会根据每个用户的通知设置来决定通知方式
        notificationService.sendNotificationToAllUsers(title, alertRecord.getMessage(), NOTIFICATION_TYPE_ALERT);
        log.info("已发送预警通知: {}", alertRecord.getMessage());
    }
} 