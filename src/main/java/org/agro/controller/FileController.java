package org.agro.controller;

import org.agro.dto.ApiResponse;
import org.agro.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 文件控制器
 */
@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = fileService.storeAvatar(file);
            return ResponseEntity.ok(ApiResponse.success("头像上传成功", fileUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("头像上传失败: " + e.getMessage()));
        }
    }
}
