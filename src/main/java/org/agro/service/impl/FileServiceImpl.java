package org.agro.service.impl;

import org.agro.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 文件服务实现类
 */
@Service
public class FileServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    
    private final Path fileStorageLocation;
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Value("${file.access.url:http://localhost:8080/api/uploads/}")
    private String fileAccessUrl;
    
    public FileServiceImpl() {
        this.fileStorageLocation = Paths.get("uploads")
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            logger.error("无法创建文件上传目录: {}", ex.getMessage());
        }
    }
    
    @Override
    public String storeAvatar(MultipartFile file) throws IOException {
        // 验证文件
        if (file.isEmpty()) {
            throw new IOException("文件不能为空");
        }
        
        // 获取文件名
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        
        // 检查不合法的文件名
        if (originalFilename.contains("..")) {
            throw new IOException("文件名包含非法路径序列: " + originalFilename);
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("只能上传图片文件");
        }
        
        // 创建文件名
        String extension = "";
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            extension = originalFilename.substring(i);
        }
        String filename = "avatar_" + UUID.randomUUID().toString() + extension;
        
        // 创建avatar子目录
        Path avatarDir = this.fileStorageLocation.resolve("avatar");
        if (!Files.exists(avatarDir)) {
            Files.createDirectories(avatarDir);
        }
        
        // 保存文件
        Path targetLocation = avatarDir.resolve(filename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        logger.info("文件保存成功: {}", targetLocation);
        
        // 返回文件访问URL
        return fileAccessUrl + "avatar/" + filename;
    }
} 