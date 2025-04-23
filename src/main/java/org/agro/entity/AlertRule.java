package org.agro.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 气象预警规则实体类
 */
@Entity
@Table(name = "alert_rule")
@Data
public class AlertRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 规则名称
    
    @Column(nullable = false)
    private Integer type; // 规则类型：1-温度, 2-湿度, 3-降水, 4-风害, 5-光照, 6-气压

    @Column(nullable = false)
    private Integer subType; // 规则子类型：比如对于温度类型，1-高温,2-低温

    @Column(nullable = false, length = 100)
    private String paramName; // 参数名称，如tempMax、humidity等

    @Column(nullable = false)
    private String operator; // 运算符，如 >、<、>=、<=、==

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal threshold; // 阈值

    // 用于复合条件的第二参数（可选）
    @Column
    private String paramName2;
    
    @Column
    private String operator2;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal threshold2;
    
    @Column(nullable = false, length = 250)
    private String message; // 预警消息模板

    @Column(nullable = false)
    private Boolean enabled = true; // 是否启用
    
    @ManyToOne
    @JoinColumn(name = "field_id", nullable = false)
    private Field field; // 关联的地块
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) 
    private User user; // 关联的用户

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // 规则类型常量
    public static final int TYPE_TEMPERATURE = 1;
    public static final int TYPE_HUMIDITY = 2;
    public static final int TYPE_PRECIPITATION = 3;
    public static final int TYPE_WIND = 4;
    public static final int TYPE_LIGHT = 5;
    public static final int TYPE_PRESSURE = 6;
    
    // 子类型常量
    // 温度类
    public static final int SUBTYPE_HIGH_TEMPERATURE = 1;
    public static final int SUBTYPE_LOW_TEMPERATURE = 2;
    
    // 湿度类
    public static final int SUBTYPE_LOW_HUMIDITY = 1;
    public static final int SUBTYPE_HIGH_VPD = 2;
    
    // 降水类
    public static final int SUBTYPE_PRECIPITATION_PROBABILITY = 1;
    public static final int SUBTYPE_HEAVY_RAIN = 2;
    public static final int SUBTYPE_DISEASE_RISK = 3;
    
    // 风害类
    public static final int SUBTYPE_STRONG_WIND = 1;
    
    // 光照类
    public static final int SUBTYPE_LOW_LIGHT = 1;
    public static final int SUBTYPE_SHORT_DAYLIGHT = 2;
    
    // 气压类
    public static final int SUBTYPE_PRESSURE_DROP = 1;
    public static final int SUBTYPE_LOW_VISIBILITY = 2;
} 