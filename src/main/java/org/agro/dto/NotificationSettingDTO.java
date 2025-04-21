package org.agro.dto;

import lombok.Data;
import org.agro.entity.NotificationSetting;

import java.time.LocalDateTime;

/**
 * 通知设置DTO，用于返回给前端的通知设置数据
 */
@Data
public class NotificationSettingDTO {
    private Long id;
    private Long userId;
    private Boolean emailNotify;
    private Boolean systemNotify;
    private LocalDateTime updatedAt;
    
    /**
     * 从NotificationSetting实体转换为DTO
     */
    public static NotificationSettingDTO fromEntity(NotificationSetting setting) {
        NotificationSettingDTO dto = new NotificationSettingDTO();
        dto.setId(setting.getId());
        dto.setUserId(setting.getUser() != null ? setting.getUser().getId() : null);
        dto.setEmailNotify(setting.getEmailNotify());
        dto.setSystemNotify(setting.getSystemNotify());
        dto.setUpdatedAt(setting.getUpdatedAt());
        return dto;
    }
} 