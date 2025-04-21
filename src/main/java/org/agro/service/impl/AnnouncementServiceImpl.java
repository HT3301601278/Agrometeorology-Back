package org.agro.service.impl;

import org.agro.entity.Announcement;
import org.agro.repository.AnnouncementRepository;
import org.agro.service.AnnouncementService;
import org.agro.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告服务实现类
 */
@Service
public class AnnouncementServiceImpl implements AnnouncementService {
    private static final Logger logger = LoggerFactory.getLogger(AnnouncementServiceImpl.class);

    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Announcement createAnnouncement(String title, String content, Integer type, Integer status, LocalDateTime publishTime, LocalDateTime expireTime) {
        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setType(type);
        announcement.setStatus(status);
        announcement.setPublishTime(publishTime);
        announcement.setExpireTime(expireTime);
        
        announcement = announcementRepository.save(announcement);
        
        return announcement;
    }

    @Override
    @Transactional
    public Announcement updateAnnouncement(Long id, String title, String content, Integer type, Integer status, LocalDateTime publishTime, LocalDateTime expireTime) {
        Announcement announcement = findById(id);
        
        // 更新公告信息
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setType(type);
        announcement.setStatus(status);
        announcement.setPublishTime(publishTime);
        announcement.setExpireTime(expireTime);
        
        return announcementRepository.save(announcement);
    }

    @Override
    @Transactional
    public Announcement publishAnnouncement(Long id) {
        Announcement announcement = findById(id);
        
        // 已经是发布状态
        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            return announcement;
        }
        
        // 更新状态为已发布
        announcement.setStatus(1);
        
        // 如果没有设置发布时间，则设置为当前时间
        if (announcement.getPublishTime() == null) {
            announcement.setPublishTime(LocalDateTime.now());
        }
        
        announcement = announcementRepository.save(announcement);
        
        return announcement;
    }

    @Override
    @Transactional
    public void deleteAnnouncement(Long id) {
        if (announcementRepository.existsById(id)) {
            announcementRepository.deleteById(id);
            logger.info("公告已删除: {}", id);
        } else {
            logger.warn("尝试删除不存在的公告: {}", id);
        }
    }

    @Override
    public Announcement findById(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
    }

    @Override
    public Page<Announcement> findAllAnnouncements(Pageable pageable) {
        return announcementRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Announcement> findAnnouncementsByType(Integer type, Pageable pageable) {
        return announcementRepository.findByTypeOrderByCreatedAtDesc(type, pageable);
    }

    @Override
    public Page<Announcement> findAnnouncementsByStatus(Integer status, Pageable pageable) {
        return announcementRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }

    @Override
    public List<Announcement> findActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now());
    }

    @Override
    public List<Announcement> findActiveAnnouncementsByType(Integer type) {
        return announcementRepository.findActiveAnnouncementsByType(type, LocalDateTime.now());
    }
} 