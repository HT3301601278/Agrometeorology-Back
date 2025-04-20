package org.agro.service;

import org.agro.dto.FieldGroupDTO;
import org.agro.entity.FieldGroup;

import java.util.List;

/**
 * 地块组服务接口
 */
public interface FieldGroupService {
    
    /**
     * 创建地块组
     * @param userId 用户ID
     * @param fieldGroupDTO 地块组DTO
     * @return 创建的地块组
     */
    FieldGroup createFieldGroup(Long userId, FieldGroupDTO fieldGroupDTO);
    
    /**
     * 更新地块组
     * @param userId 用户ID
     * @param id 地块组ID
     * @param fieldGroupDTO 地块组DTO
     * @return 更新后的地块组
     */
    FieldGroup updateFieldGroup(Long userId, Long id, FieldGroupDTO fieldGroupDTO);
    
    /**
     * 删除地块组
     * @param userId 用户ID
     * @param id 地块组ID
     */
    void deleteFieldGroup(Long userId, Long id);
    
    /**
     * 获取地块组列表
     * @param userId 用户ID
     * @return 地块组列表
     */
    List<FieldGroup> getFieldGroups(Long userId);
    
    /**
     * 获取地块组详情
     * @param userId 用户ID
     * @param id 地块组ID
     * @return 地块组详情
     */
    FieldGroup getFieldGroup(Long userId, Long id);
} 