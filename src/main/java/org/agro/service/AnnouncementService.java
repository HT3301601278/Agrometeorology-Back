package org.agro.service;

import org.agro.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务接口
 */
public interface AnnouncementService {
    /**
     * 创建公告
     */
    Announcement createAnnouncement(String title, String content, Integer type, Integer status, LocalDateTime publishTime, LocalDateTime expireTime);

    /**
     * 更新公告
     */
    Announcement updateAnnouncement(Long id, String title, String content, Integer type, Integer status, LocalDateTime publishTime, LocalDateTime expireTime);

    /**
     * 发布公告（将状态更改为已发布）
     */
    Announcement publishAnnouncement(Long id);

    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);

    /**
     * 根据ID查找公告
     */
    Announcement findById(Long id);

    /**
     * 分页查询所有公告
     */
    Page<Announcement> findAllAnnouncements(Pageable pageable);

    /**
     * 根据类型分页查询公告
     */
    Page<Announcement> findAnnouncementsByType(Integer type, Pageable pageable);

    /**
     * 根据状态分页查询公告
     */
    Page<Announcement> findAnnouncementsByStatus(Integer status, Pageable pageable);

    /**
     * 查询活动公告（已发布且未过期）
     */
    List<Announcement> findActiveAnnouncements();

    /**
     * 根据类型查询活动公告
     */
    List<Announcement> findActiveAnnouncementsByType(Integer type);
} 