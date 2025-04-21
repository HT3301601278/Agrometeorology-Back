package org.agro.repository;

import org.agro.entity.WeatherHistorical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherHistoricalRepository extends JpaRepository<WeatherHistorical, Long> {
    
    /**
     * 根据经纬度及时间范围查询历史天气数据
     */
    @Query("SELECT h FROM WeatherHistorical h WHERE " +
            "h.latitude = :latitude AND h.longitude = :longitude AND " +
            "h.dt >= :startTime AND h.dt <= :endTime " +
            "ORDER BY h.dt ASC")
    List<WeatherHistorical> findByCoordinatesInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据经纬度及时间范围查询历史天气数据的数量
     */
    @Query("SELECT COUNT(h) FROM WeatherHistorical h WHERE " +
            "h.latitude = :latitude AND h.longitude = :longitude AND " +
            "h.dt >= :startTime AND h.dt <= :endTime")
    long countByCoordinatesInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据坐标和精确时间戳查询单条历史天气数据
     */
    @Query("SELECT h FROM WeatherHistorical h WHERE " +
            "h.latitude = :latitude AND h.longitude = :longitude AND h.dt = :dt")
    Optional<WeatherHistorical> findByCoordinatesAndDt(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("dt") Long dt);
} 