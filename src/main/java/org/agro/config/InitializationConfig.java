package org.agro.config;

import org.agro.entity.NotificationSetting;
import org.agro.entity.User;
import org.agro.repository.NotificationSettingRepository;
import org.agro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化配置类
 * 用于初始化系统必要的默认数据，如管理员账户
 */
@Configuration
public class InitializationConfig {
    private static final Logger logger = LoggerFactory.getLogger(InitializationConfig.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationSettingRepository notificationSettingRepository;
    
    /**
     * 应用启动时初始化管理员账户
     */
    @Bean
    public CommandLineRunner initAdmin() {
        return args -> {
            // 检查是否已存在管理员账户
            if (userRepository.countByRole("ADMIN") == 0) {
                // 创建默认管理员账户
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin123"); // 生产环境应使用加密的密码
                admin.setEmail("admin@agro.org");
                admin.setNickname("系统管理员");
                admin.setRole("ADMIN");
                admin.setStatus(true);
                
                // 保存管理员用户
                admin = userRepository.save(admin);
                
                // 创建管理员通知设置
                NotificationSetting notificationSetting = new NotificationSetting();
                notificationSetting.setUser(admin);
                notificationSetting.setEmailNotify(true);
                notificationSetting.setSystemNotify(true);
                notificationSettingRepository.save(notificationSetting);
                
                logger.info("已初始化默认管理员账户: admin/admin123");
            } else {
                logger.info("已存在管理员账户，跳过初始化");
            }
        };
    }
} 