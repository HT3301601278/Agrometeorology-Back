package org.agro.service.impl;

import org.agro.dto.FieldGroupDTO;
import org.agro.entity.FieldGroup;
import org.agro.exception.ResourceNotFoundException;
import org.agro.repository.FieldGroupRepository;
import org.agro.repository.FieldRepository;
import org.agro.service.FieldGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地块组服务实现类
 */
@Service
public class FieldGroupServiceImpl implements FieldGroupService {

    @Autowired
    private FieldGroupRepository fieldGroupRepository;
    
    @Autowired
    private FieldRepository fieldRepository;

    @Override
    public FieldGroup createFieldGroup(Long userId, FieldGroupDTO fieldGroupDTO) {
        FieldGroup fieldGroup = new FieldGroup();
        fieldGroup.setUserId(userId);
        fieldGroup.setName(fieldGroupDTO.getName());
        fieldGroup.setDescription(fieldGroupDTO.getDescription());
        
        return fieldGroupRepository.save(fieldGroup);
    }

    @Override
    public FieldGroup updateFieldGroup(Long userId, Long id, FieldGroupDTO fieldGroupDTO) {
        FieldGroup existingFieldGroup = fieldGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("地块组不存在"));
                
        // 验证所有权
        if (!existingFieldGroup.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("地块组不存在");
        }
        
        existingFieldGroup.setName(fieldGroupDTO.getName());
        existingFieldGroup.setDescription(fieldGroupDTO.getDescription());
        existingFieldGroup.setUpdatedAt(LocalDateTime.now());
        
        return fieldGroupRepository.save(existingFieldGroup);
    }

    @Override
    @Transactional
    public void deleteFieldGroup(Long userId, Long id) {
        FieldGroup fieldGroup = fieldGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("地块组不存在"));
                
        // 验证所有权
        if (!fieldGroup.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("地块组不存在");
        }
        
        // 删除该组下所有地块的组关联（将groupId设为null）
        fieldRepository.findByGroupId(id).forEach(field -> {
            field.setGroupId(null);
            fieldRepository.save(field);
        });
        
        // 删除地块组
        fieldGroupRepository.delete(fieldGroup);
    }

    @Override
    public List<FieldGroup> getFieldGroups(Long userId) {
        return fieldGroupRepository.findByUserId(userId);
    }

    @Override
    public FieldGroup getFieldGroup(Long userId, Long id) {
        FieldGroup fieldGroup = fieldGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("地块组不存在"));
                
        // 验证所有权
        if (!fieldGroup.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("地块组不存在");
        }
        
        return fieldGroup;
    }
} 