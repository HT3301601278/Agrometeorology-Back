package org.agro.controller;

import org.agro.dto.ApiResponse;
import org.agro.dto.NotificationDTO;
import org.agro.dto.NotificationSettingDTO;
import org.agro.entity.Notification;
import org.agro.entity.NotificationSetting;
import org.agro.repository.NotificationSettingRepository;
import org.agro.repository.UserRepository;
import org.agro.security.UserDetailsImpl;
import org.agro.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取当前用户通知（分页）
     */
    @GetMapping
    public ResponseEntity<?> getUserNotifications(
            @PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // 获取原始通知数据
        Page<Notification> notificationsPage = notificationService.findUserNotifications(userDetails.getId(), pageable);
        
        // 将实体转换为DTO
        List<NotificationDTO> dtoList = notificationsPage.getContent().stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
        
        // 创建新的Page对象
        Page<NotificationDTO> dtoPage = new PageImpl<>(
                dtoList, 
                notificationsPage.getPageable(), 
                notificationsPage.getTotalElements()
        );
        
        return ResponseEntity.ok(ApiResponse.success(dtoPage));
    }

    /**
     * 获取未读通知
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // 获取原始通知数据
        List<Notification> notifications = notificationService.findUserUnreadNotifications(userDetails.getId());
        
        // 将实体转换为DTO
        List<NotificationDTO> dtoList = notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
        
        long count = dtoList.size();
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count, "notifications", dtoList)));
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
        NotificationSetting setting = notificationSettingRepository.findByUserId(userDetails.getId())
                .orElseGet(() -> {
                    NotificationSetting defaultSetting = new NotificationSetting();
                    return defaultSetting;
                });
                
        NotificationSettingDTO settingDTO = NotificationSettingDTO.fromEntity(setting);
        return ResponseEntity.ok(ApiResponse.success(settingDTO));
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/settings")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody Map<String, Boolean> request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Boolean emailNotify = request.get("emailNotify");
        Boolean systemNotify = request.get("systemNotify");
        
        NotificationSetting setting = notificationSettingRepository.findByUserId(userDetails.getId())
                .orElseGet(() -> {
                    NotificationSetting defaultSetting = new NotificationSetting();
                    defaultSetting.setUser(userRepository.findById(userDetails.getId()).orElseThrow());
                    return defaultSetting;
                });
        
        if (emailNotify != null) {
            setting.setEmailNotify(emailNotify);
        }
        
        if (systemNotify != null) {
            setting.setSystemNotify(systemNotify);
        }
        
        setting = notificationSettingRepository.save(setting);
        NotificationSettingDTO settingDTO = NotificationSettingDTO.fromEntity(setting);
        return ResponseEntity.ok(ApiResponse.success("设置更新成功", settingDTO));
    }
}
