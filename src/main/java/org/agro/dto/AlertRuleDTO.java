package org.agro.dto;

import lombok.Data;
import org.agro.entity.AlertRule;
import org.agro.entity.Field;
import org.agro.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警规则DTO
 */
@Data
public class AlertRuleDTO {
    private Long id;
    private String name;
    private Integer type;
    private Integer subType;
    private String paramName;
    private String operator;
    private BigDecimal threshold;
    private String paramName2;
    private String operator2;
    private BigDecimal threshold2;
    private String message;
    private Boolean enabled;
    private Long fieldId;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 将实体转换为DTO
     */
    public static AlertRuleDTO fromEntity(AlertRule rule) {
        AlertRuleDTO dto = new AlertRuleDTO();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setType(rule.getType());
        dto.setSubType(rule.getSubType());
        dto.setParamName(rule.getParamName());
        dto.setOperator(rule.getOperator());
        dto.setThreshold(rule.getThreshold());
        dto.setParamName2(rule.getParamName2());
        dto.setOperator2(rule.getOperator2());
        dto.setThreshold2(rule.getThreshold2());
        dto.setMessage(rule.getMessage());
        dto.setEnabled(rule.getEnabled());
        
        if (rule.getField() != null) {
            dto.setFieldId(rule.getField().getId());
        }
        
        if (rule.getUser() != null) {
            dto.setUserId(rule.getUser().getId());
        }
        
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());
        return dto;
    }
    
    /**
     * 将DTO转换为实体
     * 注意：此处不会设置Field和User对象，需要在Service层中设置
     */
    public AlertRule toEntity() {
        AlertRule rule = new AlertRule();
        rule.setId(this.id);
        rule.setName(this.name);
        rule.setType(this.type);
        rule.setSubType(this.subType);
        rule.setParamName(this.paramName);
        rule.setOperator(this.operator);
        rule.setThreshold(this.threshold);
        rule.setParamName2(this.paramName2);
        rule.setOperator2(this.operator2);
        rule.setThreshold2(this.threshold2);
        rule.setMessage(this.message);
        rule.setEnabled(this.enabled != null ? this.enabled : true);
        return rule;
    }
} 