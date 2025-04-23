package org.agro.controller;

import org.agro.dto.ApiResponse;
import org.agro.entity.SystemConfig;
import org.agro.entity.User;
import org.agro.service.NotificationService;
import org.agro.service.SystemConfigService;
import org.agro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取所有用户（分页）
     */
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> users = userService.findAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 获取所有系统配置（分页）
     */
    @GetMapping("/configs")
    public ResponseEntity<?> getAllConfigs(
            @PageableDefault(sort = {"id"}, direction = Sort.Direction.ASC) Pageable pageable) {
        Page<SystemConfig> configs = systemConfigService.getAllConfigs(pageable);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    /**
     * 保存系统配置
     */
    @PostMapping("/configs")
    public ResponseEntity<?> saveConfig(@RequestBody Map<String, String> configData) {
        String key = configData.get("key");
        String value = configData.get("value");
        String description = configData.get("description");
        
        SystemConfig config = systemConfigService.saveConfig(key, value, description);
        return ResponseEntity.ok(ApiResponse.success("配置保存成功", config));
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/configs/{key}")
    public ResponseEntity<?> deleteConfig(@PathVariable String key) {
        systemConfigService.deleteConfig(key);
        return ResponseEntity.ok(ApiResponse.success("配置删除成功", null));
    }

    /**
     * 获取API Key配置
     */
    @GetMapping("/configs/api-key")
    public ResponseEntity<?> getApiKey() {
        String apiKey = systemConfigService.getApiKey();
        return ResponseEntity.ok(ApiResponse.success(Map.of("apiKey", apiKey)));
    }

    /**
     * 保存API Key配置
     */
    @PostMapping("/configs/api-key")
    public ResponseEntity<?> saveApiKey(@RequestBody Map<String, String> data) {
        String apiKey = data.get("apiKey");
        systemConfigService.saveApiKey(apiKey);
        return ResponseEntity.ok(ApiResponse.success("API Key已保存", null));
    }

    /**
     * 获取数据拉取频率配置
     */
    @GetMapping("/configs/fetch-interval")
    public ResponseEntity<?> getDataFetchInterval() {
        int interval = systemConfigService.getDataFetchInterval();
        return ResponseEntity.ok(ApiResponse.success(Map.of("interval", interval)));
    }

    /**
     * 保存数据拉取频率配置
     */
    @PostMapping("/configs/fetch-interval")
    public ResponseEntity<?> saveDataFetchInterval(@RequestBody Map<String, Integer> data) {
        int interval = data.get("interval");
        systemConfigService.saveDataFetchInterval(interval);
        return ResponseEntity.ok(ApiResponse.success("数据拉取频率已设置", null));
    }

    /**
     * 获取邮件配置
     */
    @GetMapping("/configs/email")
    public ResponseEntity<?> getEmailConfig() {
        Map<String, String> emailConfig = systemConfigService.getEmailConfig();
        return ResponseEntity.ok(ApiResponse.success(emailConfig));
    }

    /**
     * 保存邮件配置
     */
    @PostMapping("/configs/email")
    public ResponseEntity<?> saveEmailConfig(@RequestBody Map<String, Object> data) {
        systemConfigService.saveEmailConfig(
                (String) data.get("host"),
                (Integer) data.get("port"),
                (String) data.get("username"),
                (String) data.get("password"),
                (Boolean) data.get("auth"),
                (Boolean) data.get("startTls")
        );
        return ResponseEntity.ok(ApiResponse.success("邮件配置已保存", null));
    }

    /**
     * 发送系统通知
     */
    @PostMapping("/notifications")
    public ResponseEntity<?> sendSystemNotification(@RequestBody Map<String, String> data) {
        String title = data.get("title");
        String content = data.get("content");
        
        notificationService.sendNotificationToAllUsers(title, content);
        return ResponseEntity.ok(ApiResponse.success("系统通知已发送", null));
    }
}