package org.agro.repository;

import org.agro.entity.Notification;
import org.agro.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 通知记录存储库接口
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * 根据用户查找所有通知（分页）
     */
    Page<Notification> findByUser(User user, Pageable pageable);
    
    /**
     * 根据用户ID查找所有通知（分页）
     */
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 查找用户未读通知
     */
    List<Notification> findByUserAndIsReadFalse(User user);
    
    /**
     * 根据用户ID查找未读通知
     */
    List<Notification> findByUserIdAndIsReadFalse(Long userId);
    
    /**
     * 统计用户未读通知数量
     */
    long countByUserAndIsReadFalse(User user);
    
    /**
     * 根据用户ID统计未读通知数量
     */
    long countByUserIdAndIsReadFalse(Long userId);
} 