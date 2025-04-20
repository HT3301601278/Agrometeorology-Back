package org.agro.dto;

import lombok.Data;

import javax.validation.constraints.Email;

/**
 * 用户更新请求DTO
 */
@Data
public class UserUpdateRequest {
    private String nickname;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String avatar;
    
    private String phone;
} 