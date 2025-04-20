package org.agro.entity;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 通知设置实体类
 */
@Entity
@Table(name = "notification_setting")
@Data
public class NotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean emailNotify = true;

    @Column(nullable = false)
    private Boolean systemNotify = true;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
} 