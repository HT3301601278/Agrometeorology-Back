package org.agro.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地块实体类
 */
@Entity
@Table(name = "field")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "area", precision = 10, scale = 2)
    private BigDecimal area;

    @Column(name = "soil_type", length = 20)
    private String soilType;

    @Column(name = "crop_type", length = 50)
    private String cropType;

    @Column(name = "planting_season", length = 20)
    private String plantingSeason;

    @Column(name = "growth_stage", length = 20)
    private String growthStage;

    @Column(name = "latitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 