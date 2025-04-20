package org.agro.controller;

import org.agro.dto.ApiResponse;
import org.agro.entity.Announcement;
import org.agro.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取活动公告（已发布且未过期）
     */
    @GetMapping("/public")
    public ResponseEntity<?> getActiveAnnouncements() {
        List<Announcement> announcements = announcementService.findActiveAnnouncements();
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    /**
     * 根据类型获取活动公告
     */
    @GetMapping("/public/type/{type}")
    public ResponseEntity<?> getActiveAnnouncementsByType(@PathVariable Integer type) {
        List<Announcement> announcements = announcementService.findActiveAnnouncementsByType(type);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<?> getAnnouncementDetail(@PathVariable Long id) {
        Announcement announcement = announcementService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(announcement));
    }

    /**
     * 创建公告（管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAnnouncement(@Valid @RequestBody Map<String, Object> announcementData) {
        String title = (String) announcementData.get("title");
        String content = (String) announcementData.get("content");
        Integer type = (Integer) announcementData.get("type");
        Integer status = (Integer) announcementData.get("status");
        
        // 处理日期时间
        LocalDateTime publishTime = null;
        if (announcementData.containsKey("publishTime") && announcementData.get("publishTime") != null) {
            publishTime = LocalDateTime.parse((String) announcementData.get("publishTime"));
        }
        
        LocalDateTime expireTime = null;
        if (announcementData.containsKey("expireTime") && announcementData.get("expireTime") != null) {
            expireTime = LocalDateTime.parse((String) announcementData.get("expireTime"));
        }
        
        Announcement announcement = announcementService.createAnnouncement(
                title, content, type, status, publishTime, expireTime);
        
        return ResponseEntity.ok(ApiResponse.success("公告创建成功", announcement));
    }

    /**
     * 更新公告（管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAnnouncement(
            @PathVariable Long id,
            @Valid @RequestBody Map<String, Object> announcementData) {
        
        String title = (String) announcementData.get("title");
        String content = (String) announcementData.get("content");
        Integer type = (Integer) announcementData.get("type");
        Integer status = (Integer) announcementData.get("status");
        
        // 处理日期时间
        LocalDateTime publishTime = null;
        if (announcementData.containsKey("publishTime") && announcementData.get("publishTime") != null) {
            publishTime = LocalDateTime.parse((String) announcementData.get("publishTime"));
        }
        
        LocalDateTime expireTime = null;
        if (announcementData.containsKey("expireTime") && announcementData.get("expireTime") != null) {
            expireTime = LocalDateTime.parse((String) announcementData.get("expireTime"));
        }
        
        Announcement announcement = announcementService.updateAnnouncement(
                id, title, content, type, status, publishTime, expireTime);
        
        return ResponseEntity.ok(ApiResponse.success("公告更新成功", announcement));
    }

    /**
     * 发布公告（管理员）
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> publishAnnouncement(@PathVariable Long id) {
        Announcement announcement = announcementService.publishAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("公告已发布", announcement));
    }

    /**
     * 删除公告（管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok(ApiResponse.success("公告已删除", null));
    }

    /**
     * 获取所有公告（分页，管理员）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllAnnouncements(
            @PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Announcement> announcements = announcementService.findAllAnnouncements(pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    /**
     * 根据类型获取公告（分页，管理员）
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAnnouncementsByType(
            @PathVariable Integer type,
            @PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Announcement> announcements = announcementService.findAnnouncementsByType(type, pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }

    /**
     * 根据状态获取公告（分页，管理员）
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAnnouncementsByStatus(
            @PathVariable Integer status,
            @PageableDefault(sort = {"createdAt"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Announcement> announcements = announcementService.findAnnouncementsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(announcements));
    }
} 