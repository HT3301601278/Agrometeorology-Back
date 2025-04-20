package org.agro.repository;

import org.agro.entity.NotificationSetting;
import org.agro.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 通知设置存储库接口
 */
@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    /**
     * 根据用户查找通知设置
     */
    Optional<NotificationSetting> findByUser(User user);
    
    /**
     * 根据用户ID查找通知设置
     */
    Optional<NotificationSetting> findByUserId(Long userId);
} 