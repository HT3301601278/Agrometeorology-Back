package org.agro.service;

import org.agro.entity.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {
    /**
     * 保存配置
     */
    SystemConfig saveConfig(String key, String value, String description);

    /**
     * 获取配置值
     */
    String getConfigValue(String key);

    /**
     * 获取配置对象
     */
    SystemConfig getConfig(String key);

    /**
     * 删除配置
     */
    void deleteConfig(String key);

    /**
     * 批量保存配置
     */
    void batchSaveConfig(Map<String, String> configs);

    /**
     * 分页获取所有配置
     */
    Page<SystemConfig> getAllConfigs(Pageable pageable);

    /**
     * 获取API Key
     */
    String getApiKey();

    /**
     * 保存API Key
     */
    void saveApiKey(String apiKey);

    /**
     * 获取数据拉取频率（分钟）
     */
    int getDataFetchInterval();

    /**
     * 保存数据拉取频率
     */
    void saveDataFetchInterval(int minutes);

    /**
     * 获取邮件配置
     */
    Map<String, String> getEmailConfig();

    /**
     * 保存邮件配置
     */
    void saveEmailConfig(String host, int port, String username, String password, boolean auth, boolean startTls);
} 