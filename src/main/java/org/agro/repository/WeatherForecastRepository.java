package org.agro.repository;

import org.agro.entity.WeatherForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherForecastRepository extends JpaRepository<WeatherForecast, Long> {
    
    /**
     * 根据经纬度、预报类型及时间范围查询天气预报数据
     */
    @Query("SELECT f FROM WeatherForecast f WHERE " +
            "f.latitude = :latitude AND f.longitude = :longitude AND " +
            "f.forecastType = :forecastType AND " +
            "f.dt >= :startTime AND f.dt <= :endTime " +
            "ORDER BY f.dt ASC")
    List<WeatherForecast> findByCoordinatesAndTypeInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("forecastType") byte forecastType,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据经纬度、预报类型及时间范围查询天气预报数据的数量
     */
    @Query("SELECT COUNT(f) FROM WeatherForecast f WHERE " +
            "f.latitude = :latitude AND f.longitude = :longitude AND " +
            "f.forecastType = :forecastType AND " +
            "f.dt >= :startTime AND f.dt <= :endTime")
    long countByCoordinatesAndTypeInTimeRange(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("forecastType") byte forecastType,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime);
    
    /**
     * 根据坐标、预报类型和精确时间戳查询单条记录
     */
    @Query("SELECT f FROM WeatherForecast f WHERE " +
            "f.latitude = :latitude AND f.longitude = :longitude AND " +
            "f.forecastType = :forecastType AND f.dt = :dt")
    Optional<WeatherForecast> findByCoordinatesAndTypeAndDt(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("forecastType") byte forecastType,
            @Param("dt") Long dt);
} 