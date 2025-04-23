package org.agro.service.impl;

import org.agro.service.EmailService;
import org.agro.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        // 设置发件人为配置的邮箱用户名
        String fromEmail = getFromEmail();
        if (fromEmail == null) {
            logger.error("发送简单邮件失败: 未配置发件人邮箱");
            return;
        }
        
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        
        mailSender.send(message);
        logger.info("发送简单邮件成功: {}", to);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            // 获取发件人邮箱
            String fromEmail = getFromEmail();
            if (fromEmail == null) {
                logger.error("发送HTML邮件失败: 未配置发件人邮箱");
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            logger.info("发送HTML邮件成功: {}", to);
        } catch (MessagingException e) {
            logger.error("发送HTML邮件失败: {}", e.getMessage());
        }
    }

    /**
     * 发送带内嵌图片的HTML邮件
     * 
     * @param to 收件人
     * @param subject 主题
     * @param content HTML内容
     * @param imageResourcePath 图片资源路径
     * @param contentId 内容ID
     */
    private void sendHtmlEmailWithInlineImage(String to, String subject, String content, String imageResourcePath, String contentId) {
        try {
            // 获取发件人邮箱
            String fromEmail = getFromEmail();
            if (fromEmail == null) {
                logger.error("发送HTML邮件失败: 未配置发件人邮箱");
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            // 添加内嵌图片
            ClassPathResource imageResource = new ClassPathResource(imageResourcePath);
            if (imageResource.exists()) {
                helper.addInline(contentId, imageResource);
            } else {
                logger.warn("内嵌图片资源不存在: {}", imageResourcePath);
            }
            
            mailSender.send(message);
            logger.info("发送带内嵌图片的HTML邮件成功: {}", to);
        } catch (MessagingException e) {
            logger.error("发送带内嵌图片的HTML邮件失败: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("处理内嵌图片失败: {}", e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetVerificationCode(String to, String verificationCode) {
        String subject = "【农业气象监测系统】密码重置验证码";
        
        // 创建HTML邮件模板
        String content = buildPasswordResetEmailTemplate(verificationCode);
        
        // 使用内嵌图片发送邮件
        sendHtmlEmailWithInlineImage(to, subject, content, "static/images/logo.png", "logoImage");
    }

    /**
     * 构建密码重置邮件HTML模板
     * @param verificationCode 验证码
     * @return HTML内容
     */
    private String buildPasswordResetEmailTemplate(String verificationCode) {
        // 获取当前时间
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        // 验证码有效期10分钟后的时间
        java.time.LocalDateTime expiryTime = now.plusMinutes(10);
        // 格式化显示时间
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expiryTimeString = expiryTime.format(formatter);
        
        String bodyContent = 
                "<p style=\"margin-top: 0; font-size: 16px;\">尊敬的用户：</p>\n" +
                "<p style=\"font-size: 16px;\">您好！您正在进行密码重置操作，请使用以下验证码完成操作：</p>\n" +
                "<div style=\"margin: 30px auto; text-align: center; background-color: #f8f8f8; padding: 15px; border-radius: 5px; border-left: 4px solid #2c7e43;\">\n" +
                "    <span style=\"font-size: 28px; font-weight: bold; letter-spacing: 5px; color: #2c7e43;\">" + verificationCode + "</span>\n" +
                "</div>\n" +
                "<p style=\"font-size: 15px;\">此验证码有效期为<span style=\"color: #e74c3c; font-weight: bold;\">10分钟</span>，将在 <span style=\"color: #e74c3c; font-weight: bold;\">" + expiryTimeString + "</span> 过期。</p>\n" +
                "<p style=\"font-size: 15px;\">请勿将验证码泄露给他人。</p>\n" +
                "<p style=\"font-size: 15px;\">如果您并未请求此验证码，可能是有人误输入了您的邮箱地址。请忽略此邮件，无需进行任何操作。</p>";
        
        return buildBaseEmailTemplate("密码重置验证码", bodyContent);
    }

    @Override
    public void sendNotificationEmail(String to, String title, String content) {
        String subject = "【农业气象监测系统】" + title;
        
        // 使用HTML格式发送通知邮件
        String htmlContent = buildNotificationEmailTemplate(title, content);
        sendHtmlEmailWithInlineImage(to, subject, htmlContent, "static/images/logo.png", "logoImage");
    }
    
    /**
     * 构建通知邮件HTML模板
     * @param title 通知标题
     * @param content 通知内容
     * @return HTML内容
     */
    private String buildNotificationEmailTemplate(String title, String content) {
        String bodyContent = 
                "<p style=\"margin-top: 0; font-size: 16px;\">尊敬的用户：</p>\n" +
                "<h2 style=\"font-size: 18px; color: #2c7e43; margin: 20px 0 15px;\">" + title + "</h2>\n" +
                "<div style=\"margin: 20px 0; padding: 15px; background-color: #f8f8f8; border-radius: 5px; border-left: 4px solid #2c7e43;\">\n" +
                "    <p style=\"margin: 0; font-size: 15px;\">" + content.replace("\n", "<br/>") + "</p>\n" +
                "</div>";
        
        return buildBaseEmailTemplate(title, bodyContent);
    }
    
    /**
     * 构建基础邮件HTML模板
     * 
     * @param title 邮件标题
     * @param bodyContent 邮件正文内容
     * @return 完整的HTML邮件内容
     */
    private String buildBaseEmailTemplate(String title, String bodyContent) {
        return "<!DOCTYPE html>\n" +
               "<html lang=\"zh-CN\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>" + title + "</title>\n" +
               "</head>\n" +
               "<body style=\"margin: 0; padding: 0; font-family: 'Microsoft YaHei', Arial, sans-serif; color: #333; background-color: #f5f5f5;\">\n" +
               "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px; background-color: #ffffff; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);\">\n" +
               "        <div style=\"text-align: center; margin-bottom: 20px;\">\n" +
               "            <div style=\"margin: 0 auto 15px; width: 120px; height: 120px; overflow: hidden; border-radius: 50%; background-color: #f5f5f5;\">\n" +
               "                <img src=\"cid:logoImage\" alt=\"农业气象监测系统\" style=\"width: 100%; height: 100%; object-fit: cover;\" />\n" +
               "            </div>\n" +
               "            <h1 style=\"color: #2c7e43; margin: 0; padding: 10px 0; font-size: 24px; border-bottom: 1px solid #eaeaea;\">农业气象监测系统</h1>\n" +
               "        </div>\n" +
               "        <div style=\"padding: 20px; line-height: 1.6;\">\n" +
               bodyContent + "\n" +
               "        </div>\n" +
               "        <div style=\"margin-top: 30px; padding-top: 20px; border-top: 1px solid #eaeaea; text-align: center; color: #999; font-size: 13px;\">\n" +
               "            <p>此邮件由系统自动发送，请勿直接回复</p>\n" +
               "            <p>© " + java.time.Year.now().getValue() + " 农业气象监测系统 - 保留所有权利</p>\n" +
               "        </div>\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>";
    }
    
    /**
     * 获取发件人邮箱
     */
    private String getFromEmail() {
        try {
            Map<String, String> emailConfig = systemConfigService.getEmailConfig();
            if (emailConfig.containsKey("username")) {
                return emailConfig.get("username");
            } else {
                logger.warn("未找到邮箱用户名配置");
                return null;
            }
        } catch (Exception e) {
            logger.error("获取发件人邮箱失败: {}", e.getMessage());
            return null;
        }
    }
} 