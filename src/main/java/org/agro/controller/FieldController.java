package org.agro.controller;

import org.agro.dto.FieldDTO;
import org.agro.entity.Field;
import org.agro.security.UserDetailsImpl;
import org.agro.service.FieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 地块控制器
 */
@RestController
@RequestMapping("/fields")
public class FieldController {

    @Autowired
    private FieldService fieldService;
    
    /**
     * 创建地块
     * @param authentication 当前登录用户
     * @param fieldDTO 地块DTO
     * @return 创建的地块
     */
    @PostMapping
    public ResponseEntity<Field> createField(
            Authentication authentication,
            @Valid @RequestBody FieldDTO fieldDTO) {
        Long userId = extractUserId(authentication);
        Field field = fieldService.createField(userId, fieldDTO);
        return new ResponseEntity<>(field, HttpStatus.CREATED);
    }
    
    /**
     * 更新地块
     * @param authentication 当前登录用户
     * @param id 地块ID
     * @param fieldDTO 地块DTO
     * @return 更新后的地块
     */
    @PutMapping("/{id}")
    public ResponseEntity<Field> updateField(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FieldDTO fieldDTO) {
        Long userId = extractUserId(authentication);
        Field field = fieldService.updateField(userId, id, fieldDTO);
        return ResponseEntity.ok(field);
    }
    
    /**
     * 删除地块
     * @param authentication 当前登录用户
     * @param id 地块ID
     * @return 无内容响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = extractUserId(authentication);
        fieldService.deleteField(userId, id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 获取地块列表
     * @param authentication 当前登录用户
     * @return 地块列表
     */
    @GetMapping
    public ResponseEntity<List<Field>> getFields(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<Field> fields = fieldService.getFields(userId);
        return ResponseEntity.ok(fields);
    }
    
    /**
     * 根据地块组ID获取地块列表
     * @param authentication 当前登录用户
     * @param groupId 地块组ID
     * @return 地块列表
     */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Field>> getFieldsByGroupId(
            Authentication authentication,
            @PathVariable Long groupId) {
        Long userId = extractUserId(authentication);
        List<Field> fields = fieldService.getFieldsByGroupId(userId, groupId);
        return ResponseEntity.ok(fields);
    }
    
    /**
     * 获取地块详情
     * @param authentication 当前登录用户
     * @param id 地块ID
     * @return 地块详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Field> getField(
            Authentication authentication,
            @PathVariable Long id) {
        Long userId = extractUserId(authentication);
        Field field = fieldService.getField(userId, id);
        return ResponseEntity.ok(field);
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