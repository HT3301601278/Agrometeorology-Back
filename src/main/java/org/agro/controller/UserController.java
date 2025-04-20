package org.agro.controller;

import org.agro.dto.ApiResponse;
import org.agro.dto.PasswordChangeRequest;
import org.agro.dto.UserUpdateRequest;
import org.agro.entity.User;
import org.agro.security.UserDetailsImpl;
import org.agro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.findById(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@Valid @RequestBody UserUpdateRequest updateRequest) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updatedUser = userService.updateUser(userDetails.getId(), updateRequest);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));
    }

    /**
     * 更新用户头像
     */
    @PutMapping("/me/avatar")
    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") String avatarUrl) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User updatedUser = userService.updateAvatar(userDetails.getId(), avatarUrl);
        return ResponseEntity.ok(ApiResponse.success("头像更新成功", updatedUser));
    }

    /**
     * 修改密码
     */
    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean result = userService.changePassword(userDetails.getId(), request);
        
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("原密码不正确"));
        }
    }

    /**
     * 获取用户信息（管理员）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户状态（冻结/解冻，管理员）
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        boolean result = userService.toggleUserStatus(id);
        
        if (result) {
            User user = userService.findById(id);
            String status = user.getStatus() ? "已启用" : "已冻结";
            return ResponseEntity.ok(ApiResponse.success("用户状态" + status, user));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("操作失败"));
        }
    }

    /**
     * 删除用户（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        boolean result = userService.deleteUser(id);
        
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("删除失败"));
        }
    }
} 