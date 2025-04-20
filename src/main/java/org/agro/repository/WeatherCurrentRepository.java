package org.agro.repository;

import org.agro.entity.WeatherCurrent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WeatherCurrentRepository extends JpaRepository<WeatherCurrent, Long> {

    /**
     * 根据经纬度查询最新的实时天气数据
     */
    @Query("SELECT w FROM WeatherCurrent w WHERE w.latitude = :latitude AND w.longitude = :longitude ORDER BY w.dt DESC")
    List<WeatherCurrent> findTopByCoordinatesOrderByDtDesc(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude);

    /**
     * 根据经纬度和时间戳查询特定的天气数据
     */
    Optional<WeatherCurrent> findByLatitudeAndLongitudeAndDt(
            BigDecimal latitude,
            BigDecimal longitude,
            Long dt);
}
