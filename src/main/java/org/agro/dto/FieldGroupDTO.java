package org.agro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 地块组数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldGroupDTO {
    
    private Long id;
    
    @NotBlank(message = "地块组名称不能为空")
    @Size(max = 50, message = "地块组名称长度不能超过50个字符")
    private String name;
    
    @Size(max = 255, message = "描述长度不能超过255个字符")
    private String description;
} 