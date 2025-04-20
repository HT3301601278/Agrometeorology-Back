package org.agro.service.impl;

import org.agro.entity.SystemConfig;
import org.agro.repository.SystemConfigRepository;
import org.agro.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置服务实现类
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {
    private static final Logger logger = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    // 默认API Key配置键
    private static final String API_KEY_CONFIG = "openweathermap.api.key";
    
    // 默认数据拉取频率配置键（分钟）
    private static final String FETCH_INTERVAL_CONFIG = "data.fetch.interval";
    
    // 邮件配置键前缀
    private static final String EMAIL_CONFIG_PREFIX = "mail.";

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Override
    @Transactional
    public SystemConfig saveConfig(String key, String value, String description) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseGet(() -> {
                    SystemConfig newConfig = new SystemConfig();
                    newConfig.setConfigKey(key);
                    return newConfig;
                });
        
        config.setConfigValue(value);
        
        if (description != null && !description.isEmpty()) {
            config.setDescription(description);
        }
        
        return systemConfigRepository.save(config);
    }

    @Override
    public String getConfigValue(String key) {
        return systemConfigRepository.findByConfigKey(key)
                .map(SystemConfig::getConfigValue)
                .orElse(null);
    }

    @Override
    public SystemConfig getConfig(String key) {
        return systemConfigRepository.findByConfigKey(key).orElse(null);
    }

    @Override
    @Transactional
    public void deleteConfig(String key) {
        SystemConfig config = systemConfigRepository.findByConfigKey(key).orElse(null);
        
        if (config != null) {
            systemConfigRepository.delete(config);
            logger.info("配置已删除: {}", key);
        } else {
            logger.warn("尝试删除不存在的配置: {}", key);
        }
    }

    @Override
    @Transactional
    public void batchSaveConfig(Map<String, String> configs) {
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            saveConfig(entry.getKey(), entry.getValue(), null);
        }
    }

    @Override
    public Page<SystemConfig> getAllConfigs(Pageable pageable) {
        return systemConfigRepository.findAll(pageable);
    }

    @Override
    public String getApiKey() {
        return getConfigValue(API_KEY_CONFIG);
    }

    @Override
    @Transactional
    public void saveApiKey(String apiKey) {
        saveConfig(API_KEY_CONFIG, apiKey, "OpenWeatherMap API Key");
    }

    @Override
    public int getDataFetchInterval() {
        String value = getConfigValue(FETCH_INTERVAL_CONFIG);
        
        if (value == null || value.isEmpty()) {
            return 30; // 默认30分钟
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.error("无效的数据拉取频率配置: {}", value);
            return 30; // 默认30分钟
        }
    }

    @Override
    @Transactional
    public void saveDataFetchInterval(int minutes) {
        if (minutes < 1) {
            minutes = 1; // 最小1分钟
        }
        
        saveConfig(FETCH_INTERVAL_CONFIG, String.valueOf(minutes), "数据拉取频率（分钟）");
    }

    @Override
    public Map<String, String> getEmailConfig() {
        Map<String, String> emailConfig = new HashMap<>();
        
        // 获取所有邮件相关配置
        systemConfigRepository.findAll().forEach(config -> {
            if (config.getConfigKey().startsWith(EMAIL_CONFIG_PREFIX)) {
                emailConfig.put(
                    config.getConfigKey().substring(EMAIL_CONFIG_PREFIX.length()),
                    config.getConfigValue()
                );
            }
        });
        
        return emailConfig;
    }

    @Override
    @Transactional
    public void saveEmailConfig(String host, int port, String username, String password, boolean auth, boolean startTls) {
        saveConfig(EMAIL_CONFIG_PREFIX + "host", host, "邮件服务器主机");
        saveConfig(EMAIL_CONFIG_PREFIX + "port", String.valueOf(port), "邮件服务器端口");
        saveConfig(EMAIL_CONFIG_PREFIX + "username", username, "邮件服务器用户名");
        saveConfig(EMAIL_CONFIG_PREFIX + "password", password, "邮件服务器密码");
        saveConfig(EMAIL_CONFIG_PREFIX + "auth", String.valueOf(auth), "是否需要认证");
        saveConfig(EMAIL_CONFIG_PREFIX + "starttls", String.valueOf(startTls), "是否启用STARTTLS");
    }
} 