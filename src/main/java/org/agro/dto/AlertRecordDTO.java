package org.agro.dto;

import lombok.Data;
import org.agro.entity.AlertRecord;
import org.agro.entity.AlertRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警记录DTO
 */
@Data
public class AlertRecordDTO {
    private Long id;
    private Long ruleId;
    private String ruleName;
    private Integer ruleType;
    private Integer ruleSubType;
    private Long forecastDt;
    private BigDecimal paramValue;
    private BigDecimal paramValue2;
    private String message;
    private String forecastDate;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDateTime createdAt;
    
    /**
     * 将实体转换为DTO
     */
    public static AlertRecordDTO fromEntity(AlertRecord record) {
        AlertRecordDTO dto = new AlertRecordDTO();
        dto.setId(record.getId());
        
        AlertRule rule = record.getRule();
        if (rule != null) {
            dto.setRuleId(rule.getId());
            dto.setRuleName(rule.getName());
            dto.setRuleType(rule.getType());
            dto.setRuleSubType(rule.getSubType());
        }
        
        dto.setForecastDt(record.getForecastDt());
        dto.setParamValue(record.getParamValue());
        dto.setParamValue2(record.getParamValue2());
        dto.setMessage(record.getMessage());
        dto.setForecastDate(record.getForecastDate());
        dto.setLatitude(record.getLatitude());
        dto.setLongitude(record.getLongitude());
        dto.setCreatedAt(record.getCreatedAt());
        
        return dto;
    }
} 