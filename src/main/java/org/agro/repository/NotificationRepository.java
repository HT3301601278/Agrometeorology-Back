package org.agro.repository;

import org.agro.entity.Notification;
import org.agro.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    
    /**
     * 删除用户的所有通知
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.user.id = ?1")
    void deleteByUserId(Long userId);
    
    /**
     * 查询所有通知，按创建时间倒序排序
     */
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据标题和内容查询通知
     */
    @Query("SELECT n FROM Notification n WHERE n.title = ?1 AND n.content = ?2")
    List<Notification> findByTitleAndContent(String title, String content);
    
    /**
     * 查询管理员发布的通知标题和内容
     * 找出那些发送给多个用户的相同通知（标题和内容都相同）
     */
    @Query(value = "SELECT title, content FROM notification GROUP BY title, content HAVING COUNT(*) > 1", 
           nativeQuery = true)
    List<Object[]> findDistinctAdminNotificationTitlesAndContents();
    
    /**
     * 根据标题和内容查找最新的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.title = ?1 AND n.content = ?2 ORDER BY n.createdAt DESC")
    List<Notification> findLatestByTitleAndContent(String title, String content, Pageable pageable);
    
    /**
     * 直接查询管理员通知
     * 使用标准JPQL语法
     */
    @Query("SELECT n FROM Notification n WHERE EXISTS " +
           "(SELECT 1 FROM Notification n2 WHERE n2.title = n.title AND n2.content = n.content AND n2.id <> n.id) " +
           "GROUP BY n.title, n.content ORDER BY MAX(n.createdAt) DESC")
    Page<Notification> findAdminNotificationsJpql(Pageable pageable);
    
    /**
     * 直接使用原生SQL查询管理员通知
     * 首先查找重复的通知（多个用户收到了相同的通知）
     * 然后对每组只保留最新的一条
     */
    @Query(value = "SELECT t.* FROM notification t " +
           "INNER JOIN (SELECT MAX(id) as max_id FROM notification GROUP BY title, content HAVING COUNT(*) > 1) AS latest " +
           "ON t.id = latest.max_id ORDER BY t.created_at DESC", 
           nativeQuery = true)
    List<Notification> findAdminNotificationsNative();
} 