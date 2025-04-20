package org.agro.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 认证请求DTO
 */
@Data
public class AuthRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    private String password;
} 