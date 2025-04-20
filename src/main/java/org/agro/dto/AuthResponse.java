package org.agro.dto;

import lombok.Data;

/**
 * 认证响应DTO
 */
@Data
public class AuthResponse {
    private String token;
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private String role;
    
    public AuthResponse(String token, Long id, String username, String email, String nickname, String avatar, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
        this.role = role;
    }
} 