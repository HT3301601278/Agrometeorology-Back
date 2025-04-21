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
            
            // 检查是否有配置信息
            if (emailConfig.isEmpty()) {
                logger.warn("数据库中未找到邮件配置信息，邮件功能可能无法正常工作");
                return mailSender;
            }
            
            // 设置邮件服务器配置
            if (emailConfig.containsKey("host")) {
                mailSender.setHost(emailConfig.get("host"));
            } else {
                logger.warn("未找到邮件服务器主机配置");
            }
            
            if (emailConfig.containsKey("port")) {
                try {
                    mailSender.setPort(Integer.parseInt(emailConfig.get("port")));
                } catch (NumberFormatException e) {
                    logger.warn("邮件服务器端口配置格式错误: {}", emailConfig.get("port"));
                }
            } else {
                logger.warn("未找到邮件服务器端口配置");
            }
            
            if (emailConfig.containsKey("username")) {
                mailSender.setUsername(emailConfig.get("username"));
            } else {
                logger.warn("未找到邮件服务器用户名配置");
            }
            
            if (emailConfig.containsKey("password")) {
                mailSender.setPassword(emailConfig.get("password"));
            } else {
                logger.warn("未找到邮件服务器密码配置");
            }
            
            // 设置邮件属性
            Properties props = mailSender.getJavaMailProperties();
            
            if (emailConfig.containsKey("auth")) {
                props.put("mail.smtp.auth", emailConfig.get("auth"));
            }
            
            // 根据端口设置SSL或TLS
            if (emailConfig.containsKey("port")) {
                String port = emailConfig.get("port");
                if ("465".equals(port)) {
                    // SSL配置
                    props.put("mail.smtp.ssl.enable", "true");
                    props.put("mail.smtp.socketFactory.port", port);
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                } else if (emailConfig.containsKey("starttls")) {
                    // TLS配置
                    props.put("mail.smtp.starttls.enable", emailConfig.get("starttls"));
                }
            }
            
            props.put("mail.smtp.timeout", "25000");
            props.put("mail.smtp.connectiontimeout", "25000");
            props.put("mail.smtp.writetimeout", "25000");
            props.put("mail.debug", "true");
            
            if (mailSender.getHost() != null && mailSender.getUsername() != null) {
                logger.info("邮件配置已从数据库加载: host={}, port={}, username={}", 
                        mailSender.getHost(), mailSender.getPort(), mailSender.getUsername());
            } else {
                logger.warn("邮件配置不完整，某些必要的配置缺失");
            }
        } catch (Exception e) {
            logger.error("从数据库加载邮件配置失败: {}", e.getMessage());
        }
        
        return mailSender;
    }
} 