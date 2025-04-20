package org.agro.service;

/**
 * 邮件服务接口
 */
public interface EmailService {
    /**
     * 发送文本邮件
     *
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendSimpleEmail(String to, String subject, String content);

    /**
     * 发送HTML格式邮件
     *
     * @param to 收件人
     * @param subject 主题
     * @param content HTML内容
     */
    void sendHtmlEmail(String to, String subject, String content);

    /**
     * 发送密码重置验证码
     *
     * @param to 收件人
     * @param verificationCode 验证码
     */
    void sendPasswordResetVerificationCode(String to, String verificationCode);

    /**
     * 发送通知邮件
     *
     * @param to 收件人
     * @param title 标题
     * @param content 内容
     */
    void sendNotificationEmail(String to, String title, String content);
} 