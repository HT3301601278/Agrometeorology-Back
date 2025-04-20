package org.agro.controller;

import org.agro.dto.FieldGroupDTO;
import org.agro.entity.FieldGroup;
import org.agro.security.UserDetailsImpl;
import org.agro.service.FieldGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 地块组控制器
 */
@RestController
@RequestMapping("/field-groups")
public class FieldGroupController {

    @Autowired
    private FieldGroupService fieldGroupService;
    
    /**
     * 创建地块组
     * @param authentication 当前登录用户
     * @param fieldGroupDTO 地块组DTO
     * @return 创建的地块组
     */
    @PostMapping
    public ResponseEntity<FieldGroup> createFieldGroup(
            Authentication authentication,
            @Valid @RequestBody FieldGroupDTO fieldGroupDTO) {
        Long userId = extractUserId(authentication);
        FieldGroup fieldGroup = fieldGroupService.createFieldGroup(userId, fieldGroupDTO);
        return new ResponseEntity<>(fieldGroup, HttpStatus.CREATED);
    }
    
    /**
     * 更新地块组
     * @param authentication 当前登录用户
     * @param id 地块组ID
     * @param fieldGroupDTO 地块组DTO
     * @return 更新后的地块组
     */
    @PutMapping("/{id}")
    public ResponseEntity<FieldGroup> updateFieldGroup(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FieldGroupDTO fieldGroupDTO) {
        Long userId = extractUserId(authentication);
        FieldGroup fieldGroup = fieldGroupService.updateFieldGroup(userId, id, fieldGroupDTO);
        return ResponseEntity.ok(fieldGroup);
    }
    
    /**
     * 删除地块组
     * @param authentication 当前登录用户
     * @param id 地块组ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFieldGroup(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = extractUserId(authentication);
        fieldGroupService.deleteFieldGroup(userId, id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取地块组列表
     * @param authentication 当前登录用户
     * @return 地块组列表
     */
    @GetMapping
    public ResponseEntity<List<FieldGroup>> getFieldGroups(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<FieldGroup> fieldGroups = fieldGroupService.getFieldGroups(userId);
        return ResponseEntity.ok(fieldGroups);
    }
    
    /**
     * 获取地块组详情
     * @param authentication 当前登录用户
     * @param id 地块组ID
     * @return 地块组详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<FieldGroup> getFieldGroup(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = extractUserId(authentication);
        FieldGroup fieldGroup = fieldGroupService.getFieldGroup(userId, id);
        return ResponseEntity.ok(fieldGroup);
    }
    
    /**
     * 从Authentication中提取用户ID
     * @param authentication 认证信息
     * @return 用户ID
     */
    private Long extractUserId(Authentication authentication) {
        // 从认证对象中获取用户详情
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }
} 