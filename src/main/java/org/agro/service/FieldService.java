package org.agro.service;

import org.agro.dto.FieldDTO;
import org.agro.entity.Field;

import java.util.List;

/**
 * 地块服务接口
 */
public interface FieldService {
    
    /**
     * 创建地块
     * @param userId 用户ID
     * @param fieldDTO 地块DTO
     * @return 创建的地块
     */
    Field createField(Long userId, FieldDTO fieldDTO);
    
    /**
     * 更新地块
     * @param userId 用户ID
     * @param id 地块ID
     * @param fieldDTO 地块DTO
     * @return 更新后的地块
     */
    Field updateField(Long userId, Long id, FieldDTO fieldDTO);
    
    /**
     * 删除地块
     * @param userId 用户ID
     * @param id 地块ID
     */
    void deleteField(Long userId, Long id);
    
    /**
     * 获取地块列表
     * @param userId 用户ID
     * @return 地块列表
     */
    List<Field> getFields(Long userId);
    
    /**
     * 根据地块组ID获取地块列表
     * @param userId 用户ID
     * @param groupId 地块组ID
     * @return 地块列表
     */
    List<Field> getFieldsByGroupId(Long userId, Long groupId);
    
    /**
     * 获取地块详情
     * @param userId 用户ID
     * @param id 地块ID
     * @return 地块详情
     */
    Field getField(Long userId, Long id);
} 