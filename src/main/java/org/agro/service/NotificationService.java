package org.agro.service;

import org.agro.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 通知服务接口
 */
public interface NotificationService {
    /**
     * 创建通知
     */
    Notification createNotification(Long userId, String title, String content);

    /**
     * 发送通知
     * 根据用户通知设置，可能会同时发送站内通知和邮件通知
     */
    void sendNotification(Long userId, String title, String content);

    /**
     * 批量发送通知
     */
    void sendNotificationToAllUsers(String title, String content);

    /**
     * 查找用户的所有通知
     */
    Page<Notification> findUserNotifications(Long userId, Pageable pageable);

    /**
     * 查找用户的未读通知
     */
    List<Notification> findUserUnreadNotifications(Long userId);

    /**
     * 统计用户未读通知数量
     */
    long countUserUnreadNotifications(Long userId);

    /**
     * 将通知标记为已读
     */
    boolean markAsRead(Long notificationId);

    /**
     * 将用户所有通知标记为已读
     */
    int markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId);

    /**
     * 根据ID查找通知
     */
    Notification findById(Long notificationId);
} 