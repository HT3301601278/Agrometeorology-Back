package org.agro.controller;

import org.agro.dto.AlertRecordDTO;
import org.agro.dto.AlertRuleDTO;
import org.agro.dto.ApiResponse;
import org.agro.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 预警控制器
 */
@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    /**
     * 创建预警规则
     */
    @PostMapping("/rules")
    public ResponseEntity<?> createRule(@RequestBody AlertRuleDTO ruleDTO) {
        AlertRuleDTO createdRule = alertService.createRule(ruleDTO);
        return ResponseEntity.ok(ApiResponse.success("预警规则创建成功", createdRule));
    }

    /**
     * 更新预警规则
     */
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<?> updateRule(@PathVariable Long ruleId, @RequestBody AlertRuleDTO ruleDTO) {
        AlertRuleDTO updatedRule = alertService.updateRule(ruleId, ruleDTO);
        return ResponseEntity.ok(ApiResponse.success("预警规则更新成功", updatedRule));
    }

    /**
     * 删除预警规则
     */
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<?> deleteRule(@PathVariable Long ruleId) {
        alertService.deleteRule(ruleId);
        return ResponseEntity.ok(ApiResponse.success("预警规则删除成功", null));
    }

    /**
     * 启用/禁用预警规则
     */
    @PatchMapping("/rules/{ruleId}/status")
    public ResponseEntity<?> toggleRuleStatus(@PathVariable Long ruleId, @RequestBody Map<String, Boolean> statusMap) {
        Boolean enabled = statusMap.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(400, "缺少启用状态参数"));
        }
        
        AlertRuleDTO updatedRule = alertService.toggleRuleStatus(ruleId, enabled);
        String message = enabled ? "预警规则已启用" : "预警规则已禁用";
        return ResponseEntity.ok(ApiResponse.success(message, updatedRule));
    }

    /**
     * 获取预警规则详情
     */
    @GetMapping("/rules/{ruleId}")
    public ResponseEntity<?> getRuleById(@PathVariable Long ruleId) {
        AlertRuleDTO rule = alertService.getRuleById(ruleId);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    /**
     * 分页获取所有预警规则
     */
    @GetMapping("/rules")
    public ResponseEntity<?> getAllRules(
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AlertRuleDTO> rulePage = alertService.getAllRules(pageable);
        return ResponseEntity.ok(ApiResponse.success(rulePage));
    }

    /**
     * 分页获取所有预警记录
     */
    @GetMapping("/records")
    public ResponseEntity<?> getAllAlertRecords(
            @PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AlertRecordDTO> recordPage = alertService.getAllAlertRecords(pageable);
        return ResponseEntity.ok(ApiResponse.success(recordPage));
    }

    /**
     * 获取预警记录详情
     */
    @GetMapping("/records/{recordId}")
    public ResponseEntity<?> getAlertRecordById(@PathVariable Long recordId) {
        AlertRecordDTO record = alertService.getAlertRecordById(recordId);
        return ResponseEntity.ok(ApiResponse.success(record));
    }
    
    /**
     * 获取最近7天的预警记录
     */
    @GetMapping("/records/recent")
    public ResponseEntity<?> getRecentAlerts() {
        LocalDateTime startTime = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        List<AlertRecordDTO> recentAlerts = alertService.getRecentAlerts(startTime);
        return ResponseEntity.ok(ApiResponse.success(recentAlerts));
    }
    
    /**
     * 手动触发预警检查
     */
    @PostMapping("/check")
    public ResponseEntity<?> manualCheckAlerts() {
        alertService.checkForecastAndGenerateAlerts();
        return ResponseEntity.ok(ApiResponse.success("预警检查已触发"));
    }
} 