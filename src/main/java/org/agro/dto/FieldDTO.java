package org.agro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 地块数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldDTO {
    
    private Long id;
    
    private Long groupId;
    
    @NotBlank(message = "地块名称不能为空")
    @Size(max = 50, message = "地块名称长度不能超过50个字符")
    private String name;
    
    @Digits(integer = 8, fraction = 2, message = "面积格式不正确")
    private BigDecimal area;
    
    @Size(max = 20, message = "土壤类型长度不能超过20个字符")
    private String soilType;
    
    @Size(max = 50, message = "作物类型长度不能超过50个字符")
    private String cropType;
    
    @Size(max = 20, message = "种植季节长度不能超过20个字符")
    private String plantingSeason;
    
    @Size(max = 20, message = "生长阶段长度不能超过20个字符")
    private String growthStage;
    
    @NotNull(message = "纬度不能为空")
    @Digits(integer = 3, fraction = 6, message = "纬度格式不正确")
    private BigDecimal latitude;
    
    @NotNull(message = "经度不能为空")
    @Digits(integer = 3, fraction = 6, message = "经度格式不正确")
    private BigDecimal longitude;
} 