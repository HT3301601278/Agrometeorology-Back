package org.agro.config;

import org.agro.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Map;
import java.util.Properties;

/**
 * 邮件配置类
 * 从数据库加载邮件配置
 */
@Configuration
public class MailConfig {
    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        try {
            // 从数据库加载邮件配置
            Map<String, String> emailConfig = systemConfigService.getEmailConfig();
            
            // 设置邮件服务器配置
            mailSender.setHost(emailConfig.getOrDefault("host", "smtp.qq.com"));
            mailSender.setPort(Integer.parseInt(emailConfig.getOrDefault("port", "465")));
            mailSender.setUsername(emailConfig.getOrDefault("username", "482396642@qq.com"));
            mailSender.setPassword(emailConfig.getOrDefault("password", ""));
            
            // 设置邮件属性
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", emailConfig.getOrDefault("auth", "true"));
            
            // 根据端口设置SSL或TLS
            if ("465".equals(emailConfig.getOrDefault("port", "465"))) {
                // SSL配置
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.port", emailConfig.getOrDefault("port", "465"));
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } else {
                // TLS配置
                props.put("mail.smtp.starttls.enable", emailConfig.getOrDefault("starttls", "false"));
            }
            
            props.put("mail.smtp.timeout", "25000");
            props.put("mail.smtp.connectiontimeout", "25000");
            props.put("mail.smtp.writetimeout", "25000");
            props.put("mail.debug", "true");
            
            logger.info("邮件配置已从数据库加载: host={}, port={}, username={}", 
                    mailSender.getHost(), mailSender.getPort(), mailSender.getUsername());
        } catch (Exception e) {
            logger.error("从数据库加载邮件配置失败，使用默认配置: {}", e.getMessage());
        }
        
        return mailSender;
    }
} 