package org.agro.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预警记录实体类
 */
@Entity
@Table(name = "alert_record")
@Data
public class AlertRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    private AlertRule rule; // 关联的预警规则

    @Column(nullable = false)
    private Long forecastDt; // 预报时间戳

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paramValue; // 触发预警的参数值
    
    @Column(precision = 10, scale = 2)
    private BigDecimal paramValue2; // 第二参数值（如果有）

    @Column(nullable = false, length = 250)
    private String message; // 预警消息内容
    
    @Column(nullable = false, length = 100)
    private String forecastDate; // 预报日期，格式化的字符串

    @Column(nullable = false, precision = 10)
    private BigDecimal latitude; // 纬度

    @Column(nullable = false, precision = 10)
    private BigDecimal longitude; // 经度

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 