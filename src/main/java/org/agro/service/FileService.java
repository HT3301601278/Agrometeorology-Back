package org.agro.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件服务接口
 */
public interface FileService {
    /**
     * 存储用户头像
     *
     * @param file 上传的文件
     * @return 文件访问URL
     * @throws IOException 如果文件存储过程中发生错误
     */
    String storeAvatar(MultipartFile file) throws IOException;
} 