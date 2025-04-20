package org.agro.repository;

import org.agro.entity.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {
    
    /**
     * 根据经纬度、预报类型及时间范围查询天气预报数据
     */
    @Query("SELECT w FROM WeatherForecast w WHERE w.latitude = :latitude AND w.longitude = :longitude " +
           "AND w.forecastType = :forecastType AND w.dt >= :startTime AND w.dt <= :endTime ORDER BY w.dt ASC")
    List<WeatherForecast> findByCoordinatesAndTypeInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("forecastType") Byte forecastType,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据经纬度、预报类型及时间范围查询天气预报数据的数量
     */
    @Query("SELECT COUNT(w) FROM WeatherForecast w WHERE w.latitude = :latitude AND w.longitude = :longitude " +
           "AND w.forecastType = :forecastType AND w.dt >= :startTime AND w.dt <= :endTime")
    long countByCoordinatesAndTypeInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("forecastType") Byte forecastType,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据经纬度、预报类型和时间戳查询特定的天气预报数据
     */
    Optional<WeatherForecast> findByLatitudeAndLongitudeAndForecastTypeAndDt(
            BigDecimal latitude,
            BigDecimal longitude,
            Byte forecastType,
            Long dt);
} 