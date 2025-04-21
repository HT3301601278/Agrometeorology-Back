package org.agro.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeatherRequestDTO {
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Long fieldId; // 关联的地块ID
    private Long startTime; // 开始时间戳，用于历史数据和预报范围
    private Long endTime; // 结束时间戳，用于历史数据和预报范围
    private Integer count; // 获取数据数量
    private String units = "metric"; // 默认使用公制单位(摄氏度)
    private String lang = "zh_cn"; // 默认使用中文
    private Boolean forceRefresh; // 是否强制刷新数据，绕过缓存
}
