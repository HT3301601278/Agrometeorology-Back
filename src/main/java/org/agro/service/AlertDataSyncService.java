package org.agro.service;

import lombok.extern.slf4j.Slf4j;
import org.agro.entity.AlertRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 预警数据同步服务
 * 负责定时检查天气预报数据，生成预警事件
 */
@Slf4j
@Service
public class AlertDataSyncService {

    private final AlertService alertService;

    @Autowired
    public AlertDataSyncService(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * 每天凌晨2点30分执行预警检查
     * 在天气预报数据同步后进行
     */
    @Scheduled(cron = "0 30 2 * * ?")
    public void syncAlertData() {
        log.info("开始执行预警检查任务...");
        
        try {
            List<AlertRecord> generatedAlerts = alertService.checkForecastAndGenerateAlerts();
            log.info("预警检查任务完成，生成了{}条预警记录", generatedAlerts.size());
        } catch (Exception e) {
            log.error("执行预警检查任务时发生错误", e);
        }
    }
} 