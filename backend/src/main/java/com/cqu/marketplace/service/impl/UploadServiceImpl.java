package com.cqu.marketplace.service.impl;

import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Value("${upload.allowed-types}")
    private String allowedTypes;
    
    @Value("${upload.max-size}")
    private long maxSize;
    
    /** 允许的文件扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    
    @Override
    public String uploadImage(MultipartFile file) {
        // 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }
        
        // 校验文件大小（2MB）
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "文件大小不能超过2MB");
        }
        
        // 校验文件类型
        String contentType = file.getContentType();
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (contentType == null || !allowedTypeList.contains(contentType)) {
            throw new BusinessException(400, "仅支持jpg/png格式的图片");
        }
        
        // 校验文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(400, "仅支持jpg/png格式的图片");
        }
        
        // 生成唯一文件名
        String newFilename = UUID.randomUUID().toString() + "." + extension;
        
        try {
            // 确保上传目录存在
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            log.info("文件上传成功: {}", newFilename);
            
            // 返回访问URL
            return "/uploads/" + newFilename;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(500, "文件上传失败");
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
