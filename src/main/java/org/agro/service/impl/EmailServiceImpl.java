package org.agro.service.impl;

import org.agro.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendSimpleEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        
        mailSender.send(message);
        logger.info("发送简单邮件成功: {}", to);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            logger.info("发送HTML邮件成功: {}", to);
        } catch (MessagingException e) {
            logger.error("发送HTML邮件失败: {}", e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetVerificationCode(String to, String verificationCode) {
        String subject = "密码重置验证码";
        String content = String.format("您好，您的密码重置验证码为：%s，有效期为10分钟，请勿泄露给他人。", verificationCode);
        
        sendSimpleEmail(to, subject, content);
    }

    @Override
    public void sendNotificationEmail(String to, String title, String content) {
        String subject = "【农业气象监测系统】" + title;
        sendSimpleEmail(to, subject, content);
    }
} 