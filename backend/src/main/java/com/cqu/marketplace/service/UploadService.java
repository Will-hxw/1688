package com.cqu.marketplace.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface UploadService {
    
    /**
     * 上传图片
     * @param file 图片文件
     * @return 图片访问URL
     */
    String uploadImage(MultipartFile file);
}
