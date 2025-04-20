package org.agro.repository;

import org.agro.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告存储库接口
 */
@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    /**
     * 查找已发布且未过期的公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 1 AND (a.expireTime IS NULL OR a.expireTime > ?1) ORDER BY a.publishTime DESC")
    List<Announcement> findActiveAnnouncements(LocalDateTime now);
    
    /**
     * 根据类型查找已发布且未过期的公告
     */
    @Query("SELECT a FROM Announcement a WHERE a.status = 1 AND a.type = ?1 AND (a.expireTime IS NULL OR a.expireTime > ?2) ORDER BY a.publishTime DESC")
    List<Announcement> findActiveAnnouncementsByType(Integer type, LocalDateTime now);
    
    /**
     * 分页查询所有公告
     */
    Page<Announcement> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 根据类型分页查询公告
     */
    Page<Announcement> findByTypeOrderByCreatedAtDesc(Integer type, Pageable pageable);
    
    /**
     * 根据状态分页查询公告
     */
    Page<Announcement> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);
} 