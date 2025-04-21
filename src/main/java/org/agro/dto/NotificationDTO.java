package org.agro.dto;

import lombok.Data;
import org.agro.entity.Notification;

import java.time.LocalDateTime;

/**
 * 通知DTO，用于返回给前端的通知数据
 */
@Data
public class NotificationDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    /**
     * 从Notification实体转换为DTO
     */
    public static NotificationDTO fromEntity(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
} 