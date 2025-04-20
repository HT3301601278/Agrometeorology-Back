package org.agro.service.impl;

import org.agro.dto.FieldDTO;
import org.agro.entity.Field;
import org.agro.entity.FieldGroup;
import org.agro.exception.ResourceNotFoundException;
import org.agro.repository.FieldGroupRepository;
import org.agro.repository.FieldRepository;
import org.agro.service.FieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地块服务实现类
 */
@Service
public class FieldServiceImpl implements FieldService {

    @Autowired
    private FieldRepository fieldRepository;
    
    @Autowired
    private FieldGroupRepository fieldGroupRepository;

    @Override
    public Field createField(Long userId, FieldDTO fieldDTO) {
        // 如果有地块组ID，验证地块组是否存在
        if (fieldDTO.getGroupId() != null) {
            FieldGroup fieldGroup = fieldGroupRepository.findById(fieldDTO.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("地块组不存在"));
                    
            // 验证所有权
            if (!fieldGroup.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("地块组不存在");
            }
        }
        
        Field field = new Field();
        field.setUserId(userId);
        field.setGroupId(fieldDTO.getGroupId());
        field.setName(fieldDTO.getName());
        field.setArea(fieldDTO.getArea());
        field.setSoilType(fieldDTO.getSoilType());
        field.setCropType(fieldDTO.getCropType());
        field.setPlantingSeason(fieldDTO.getPlantingSeason());
        field.setGrowthStage(fieldDTO.getGrowthStage());
        field.setLatitude(fieldDTO.getLatitude());
        field.setLongitude(fieldDTO.getLongitude());
        
        return fieldRepository.save(field);
    }

    @Override
    public Field updateField(Long userId, Long id, FieldDTO fieldDTO) {
        Field existingField = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("地块不存在"));
                
        // 验证所有权
        if (!existingField.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("地块不存在");
        }
        
        // 如果地块组ID改变，验证新地块组是否存在
        if (fieldDTO.getGroupId() != null && 
                (existingField.getGroupId() == null || !existingField.getGroupId().equals(fieldDTO.getGroupId()))) {
            FieldGroup fieldGroup = fieldGroupRepository.findById(fieldDTO.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("地块组不存在"));
                    
            // 验证所有权
            if (!fieldGroup.getUserId().equals(userId)) {
                throw new ResourceNotFoundException("地块组不存在");
            }
        }
        
        existingField.setGroupId(fieldDTO.getGroupId());
        existingField.setName(fieldDTO.getName());
        existingField.setArea(fieldDTO.getArea());
        existingField.setSoilType(fieldDTO.getSoilType());
        existingField.setCropType(fieldDTO.getCropType());
        existingField.setPlantingSeason(fieldDTO.getPlantingSeason());
        existingField.setGrowthStage(fieldDTO.getGrowthStage());
        existingField.setLatitude(fieldDTO.getLatitude());
        existingField.setLongitude(fieldDTO.getLongitude());
        existingField.setUpdatedAt(LocalDateTime.now());
        
        return fieldRepository.save(existingField);
    }

    @Override
    public void deleteField(Long userId, Long id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("地块不存在"));
                
        // 验证所有权
        if (!field.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("地块不存在");
        }
        
        fieldRepository.delete(field);
    }

    @Override
    public List<Field> getFields(Long userId) {
        return fieldRepository.findByUserId(userId);
    }

    @Override
    public List<Field> getFieldsByGroupId(Long userId, Long groupId) {
        // 验证地块组是否存在
        FieldGroup fieldGroup = fieldGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("地块组不存在"));
                
        // 验证所有权
        if (!fieldGroup.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("地块组不存在");
        }
        
        return fieldRepository.findByUserIdAndGroupId(userId, groupId);
    }

    @Override
    public Field getField(Long userId, Long id) {
        Field field = fieldRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("地块不存在"));
                
        // 验证所有权
        if (!field.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("地块不存在");
        }
        
        return field;
    }
} 