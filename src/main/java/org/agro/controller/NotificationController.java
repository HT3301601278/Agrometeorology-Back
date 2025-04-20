package org.agro.controller;

import org.agro.dto.ApiResponse;
import org.agro.entity.Notification;
import org.agro.entity.NotificationSetting;
import org.agro.security.UserDetailsImpl;
import org.agro.service.NotificationService;
import org.agro.repository.NotificationSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    /**
     * 获取当前用户通知（分页）
     */
    @GetMapping
    public ResponseEntity<?> getUserNotifications(
            @PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Notification> notifications = notificationService.findUserNotifications(userDetails.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * 获取未读通知
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Notification> notifications = notificationService.findUserUnreadNotifications(userDetails.getId());
        long count = notifications.size();
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count, "notifications", notifications)));
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadNotificationCount() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long count = notificationService.countUserUnreadNotifications(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * 将通知标记为已读
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        boolean result = notificationService.markAsRead(id);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("标记已读成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("操作失败"));
        }
    }

    /**
     * 将所有通知标记为已读
     */
    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int count = notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(String.format("已将 %d 条通知标记为已读", count), null));
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        boolean result = notificationService.deleteNotification(id);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("删除失败"));
        }
    }

    /**
     * 获取通知设置
     */
    @GetMapping("/settings")
    public ResponseEntity<?> getNotificationSettings() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        NotificationSetting settings = notificationSettingRepository.findByUserId(userDetails.getId())
                .orElseGet(() -> {
                    NotificationSetting defaultSettings = new NotificationSetting();
                    return defaultSettings;
                });
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/settings")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody Map<String, Boolean> settings) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        NotificationSetting notificationSetting = notificationSettingRepository.findByUserId(userDetails.getId())
                .orElseGet(() -> {
                    NotificationSetting newSetting = new NotificationSetting();
                    return newSetting;
                });

        if (settings.containsKey("emailNotify")) {
            notificationSetting.setEmailNotify(settings.get("emailNotify"));
        }
        if (settings.containsKey("systemNotify")) {
            notificationSetting.setSystemNotify(settings.get("systemNotify"));
        }

        notificationSettingRepository.save(notificationSetting);
        return ResponseEntity.ok(ApiResponse.success("设置已更新", notificationSetting));
    }
} 