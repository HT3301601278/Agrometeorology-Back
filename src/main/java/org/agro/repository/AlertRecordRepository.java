package org.agro.repository;

import org.agro.entity.AlertRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long> {
    
    /**
     * 分页查询所有预警记录
     */
    Page<AlertRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 检查某个时间范围内是否已存在相同规则和预报时间的预警记录
     */
    boolean existsByRuleIdAndForecastDt(Long ruleId, Long forecastDt);
    
    /**
     * 获取最近一段时间内的预警记录
     */
    @Query("SELECT a FROM AlertRecord a WHERE a.createdAt >= :startTime ORDER BY a.createdAt DESC")
    List<AlertRecord> findRecentAlerts(@Param("startTime") LocalDateTime startTime);
    
    /**
     * 根据规则类型查询预警记录
     */
    @Query("SELECT a FROM AlertRecord a WHERE a.rule.type = :type ORDER BY a.createdAt DESC")
    Page<AlertRecord> findByRuleType(@Param("type") Integer type, Pageable pageable);
    
    /**
     * 根据经纬度范围查询预警记录
     */
    @Query("SELECT a FROM AlertRecord a WHERE " +
            "a.latitude BETWEEN :minLat AND :maxLat AND " +
            "a.longitude BETWEEN :minLon AND :maxLon " +
            "ORDER BY a.createdAt DESC")
    Page<AlertRecord> findByCoordinatesRange(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon,
            Pageable pageable);
} 