package org.agro.repository;

import org.agro.entity.WeatherHistorical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WeatherHistoricalRepository extends JpaRepository<WeatherHistorical, Long> {
    
    /**
     * 根据经纬度及时间范围查询历史天气数据
     */
    @Query("SELECT w FROM WeatherHistorical w WHERE w.latitude = :latitude AND w.longitude = :longitude " +
           "AND w.dt >= :startTime AND w.dt <= :endTime ORDER BY w.dt ASC")
    List<WeatherHistorical> findByCoordinatesInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据经纬度及时间范围查询历史天气数据的数量
     */
    @Query("SELECT COUNT(w) FROM WeatherHistorical w WHERE w.latitude = :latitude AND w.longitude = :longitude " +
           "AND w.dt >= :startTime AND w.dt <= :endTime")
    long countByCoordinatesInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据经纬度和时间戳查询特定的历史天气数据
     */
    Optional<WeatherHistorical> findByLatitudeAndLongitudeAndDt(
            BigDecimal latitude,
            BigDecimal longitude,
            Long dt);
} 