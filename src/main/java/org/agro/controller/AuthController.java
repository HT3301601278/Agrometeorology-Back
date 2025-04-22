package org.agro.controller;

import org.agro.dto.*;
import org.agro.entity.User;
import org.agro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        User user = userService.registerUser(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail(),
                registerRequest.getNickname(),
                registerRequest.getPhone()
        );
        return ResponseEntity.ok(ApiResponse.success("注册成功", user));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody AuthRequest loginRequest) {
        AuthResponse authResponse = userService.loginUser(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        return ResponseEntity.ok(ApiResponse.success("登录成功", authResponse));
    }

    /**
     * 发送密码重置验证码
     */
    @PostMapping("/password/reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestParam String email) {
        userService.sendPasswordResetCode(email);
        return ResponseEntity.ok(ApiResponse.success("验证码已发送，请检查您的邮箱", null));
    }

    /**
     * 忘记密码 - 验证身份并发送验证码
     */
    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        boolean result = userService.sendForgotPasswordCode(forgotPasswordRequest);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("验证码已发送，请检查您的邮箱", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("用户名或邮箱不正确"));
        }
    }

    /**
     * 重置密码
     */
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest resetRequest) {
        boolean result = userService.resetPassword(resetRequest);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("密码重置成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.fail("密码重置失败，请检查验证码是否正确"));
        }
    }
}