package org.agro.service.impl;

import org.agro.entity.Notification;
import org.agro.entity.NotificationSetting;
import org.agro.entity.User;
import org.agro.repository.NotificationRepository;
import org.agro.repository.NotificationSettingRepository;
import org.agro.repository.UserRepository;
import org.agro.service.EmailService;
import org.agro.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 通知服务实现类
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public Notification createNotification(Long userId, String title, String content, Integer type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void sendNotification(Long userId, String title, String content, Integer type) {
        // 检查用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 获取用户通知设置
        NotificationSetting setting = notificationSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    NotificationSetting defaultSetting = new NotificationSetting();
                    defaultSetting.setUser(user);
                    defaultSetting.setEmailNotify(true);
                    defaultSetting.setSystemNotify(true);
                    return notificationSettingRepository.save(defaultSetting);
                });

        // 如果用户设置了接收系统通知，则创建系统通知
        if (setting.getSystemNotify()) {
            createNotification(userId, title, content, type);
        }

        // 如果用户设置了接收邮件通知且有邮箱，则发送邮件通知
        if (setting.getEmailNotify() && user.getEmail() != null && !user.getEmail().isEmpty()) {
            try {
                emailService.sendNotificationEmail(user.getEmail(), title, content);
            } catch (Exception e) {
                logger.error("发送邮件通知失败: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void sendNotificationToAllUsers(String title, String content, Integer type) {
        // 获取所有角色为USER的用户
        List<User> users = userRepository.findByRole("USER");
        
        for (User user : users) {
            try {
                sendNotification(user.getId(), title, content, type);
            } catch (Exception e) {
                logger.error("发送通知给用户[{}]失败: {}", user.getUsername(), e.getMessage());
            }
        }
    }

    @Override
    public Page<Notification> findUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<Notification> findUserUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Override
    public long countUserUnreadNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            notificationRepository.save(notification);
            return true;
        }
        
        return false;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        
        if (unreadNotifications.isEmpty()) {
            return 0;
        }
        
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        
        notificationRepository.saveAll(unreadNotifications);
        return unreadNotifications.size();
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId) {
        if (notificationRepository.existsById(notificationId)) {
            notificationRepository.deleteById(notificationId);
            return true;
        }
        
        return false;
    }

    @Override
    public Notification findById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
    }
} 