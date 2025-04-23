package org.agro.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * 安全工具类
 * 用于获取当前登录用户的信息
 */
public class SecurityUtils {
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * 获取当前登录用户的ID
     * @return 用户ID（Optional包装）
     */
    public static Optional<Long> getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                    authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                return Optional.of(userDetails.getId());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("获取当前用户ID时发生错误: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 获取当前登录用户名
     * @return 用户名（Optional包装）
     */
    public static Optional<String> getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return Optional.of(authentication.getName());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("获取当前用户名时发生错误: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 获取当前用户详情
     * @return 用户详情（Optional包装）
     */
    public static Optional<UserDetailsImpl> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                    authentication.getPrincipal() instanceof UserDetailsImpl) {
                return Optional.of((UserDetailsImpl) authentication.getPrincipal());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("获取当前用户详情时发生错误: {}", e.getMessage());
            return Optional.empty();
        }
    }
}