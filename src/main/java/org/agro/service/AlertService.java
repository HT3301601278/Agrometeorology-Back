package org.agro.service;

import org.agro.dto.AlertRecordDTO;
import org.agro.dto.AlertRuleDTO;
import org.agro.entity.AlertRecord;
import org.agro.entity.AlertRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警服务接口
 */
public interface AlertService {
    
    /**
     * 创建预警规则
     */
    AlertRuleDTO createRule(AlertRuleDTO ruleDTO);
    
    /**
     * 更新预警规则
     */
    AlertRuleDTO updateRule(Long ruleId, AlertRuleDTO ruleDTO);
    
    /**
     * 删除预警规则
     */
    void deleteRule(Long ruleId);
    
    /**
     * 启用/禁用预警规则
     */
    AlertRuleDTO toggleRuleStatus(Long ruleId, boolean enabled);
    
    /**
     * 获取预警规则详情
     */
    AlertRuleDTO getRuleById(Long ruleId);
    
    /**
     * 分页获取所有预警规则
     */
    Page<AlertRuleDTO> getAllRules(Pageable pageable);
    
    /**
     * 分页获取所有预警记录
     */
    Page<AlertRecordDTO> getAllAlertRecords(Pageable pageable);
    
    /**
     * 获取预警记录详情
     */
    AlertRecordDTO getAlertRecordById(Long recordId);
    
    /**
     * 检查天气预报数据，生成预警
     * 此方法将根据所有已启用的预警规则检查天气预报数据，生成预警事件
     */
    List<AlertRecord> checkForecastAndGenerateAlerts();
    
    /**
     * 获取最近一段时间内的预警记录
     */
    List<AlertRecordDTO> getRecentAlerts(LocalDateTime startTime);
} 