package org.agro.service;

import org.agro.dto.AuthResponse;
import org.agro.dto.PasswordChangeRequest;
import org.agro.dto.PasswordResetRequest;
import org.agro.dto.UserUpdateRequest;
import org.agro.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 注册用户
     */
    User registerUser(String username, String password, String email, String nickname, String phone);

    /**
     * 用户登录
     */
    AuthResponse loginUser(String username, String password);

    /**
     * 根据ID查找用户
     */
    User findById(Long id);

    /**
     * 根据用户名查找用户
     */
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);

    /**
     * 更新用户信息
     */
    User updateUser(Long userId, UserUpdateRequest updateRequest);

    /**
     * 更新用户头像
     */
    User updateAvatar(Long userId, String avatarUrl);

    /**
     * 修改密码
     */
    boolean changePassword(Long userId, PasswordChangeRequest passwordChangeRequest);

    /**
     * 发送密码重置验证码
     */
    void sendPasswordResetCode(String email);

    /**
     * 重置密码
     */
    boolean resetPassword(PasswordResetRequest passwordResetRequest);

    /**
     * 冻结/解冻用户
     */
    boolean toggleUserStatus(Long userId);

    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);

    /**
     * 分页查询所有用户
     */
    Page<User> findAllUsers(Pageable pageable);
} 