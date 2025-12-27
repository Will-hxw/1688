package com.cqu.marketplace.service;

import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.service.impl.UploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 上传服务测试
 * 验证: Property 18: 图片上传约束
 */
class UploadServiceTest {
    
    private UploadServiceImpl uploadService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        uploadService = new UploadServiceImpl();
        ReflectionTestUtils.setField(uploadService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(uploadService, "allowedTypes", "image/jpeg,image/png");
        ReflectionTestUtils.setField(uploadService, "maxSize", 2097152L); // 2MB
    }
    
    @Test
    @DisplayName("上传JPG图片成功 - 验证Property 18")
    void uploadImage_Success_Jpg() {
        // 准备
        byte[] content = new byte[1024]; // 1KB
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", content);
        
        // 执行
        String imageUrl = uploadService.uploadImage(file);
        
        // 验证
        assertNotNull(imageUrl);
        assertTrue(imageUrl.startsWith("/uploads/"));
        assertTrue(imageUrl.endsWith(".jpg"));
    }
    
    @Test
    @DisplayName("上传PNG图片成功 - 验证Property 18")
    void uploadImage_Success_Png() {
        // 准备
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.png", "image/png", content);
        
        // 执行
        String imageUrl = uploadService.uploadImage(file);
        
        // 验证
        assertNotNull(imageUrl);
        assertTrue(imageUrl.startsWith("/uploads/"));
        assertTrue(imageUrl.endsWith(".png"));
    }
    
    @Test
    @DisplayName("上传失败 - 文件为空 - 验证Property 18")
    void uploadImage_Fail_EmptyFile() {
        // 准备
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", new byte[0]);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> uploadService.uploadImage(file));
        assertEquals(400, exception.getCode());
    }
    
    @Test
    @DisplayName("上传失败 - 文件过大（超过2MB）- 验证Property 18")
    void uploadImage_Fail_FileTooLarge() {
        // 准备：3MB文件
        byte[] content = new byte[3 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", content);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> uploadService.uploadImage(file));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("2MB"));
    }
    
    @Test
    @DisplayName("上传失败 - 不支持的文件类型（GIF）- 验证Property 18")
    void uploadImage_Fail_UnsupportedType_Gif() {
        // 准备
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.gif", "image/gif", content);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> uploadService.uploadImage(file));
        assertEquals(400, exception.getCode());
        assertTrue(exception.getMessage().contains("jpg/png"));
    }
    
    @Test
    @DisplayName("上传失败 - 不支持的文件类型（PDF）- 验证Property 18")
    void uploadImage_Fail_UnsupportedType_Pdf() {
        // 准备
        byte[] content = new byte[1024];
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.pdf", "application/pdf", content);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> uploadService.uploadImage(file));
        assertEquals(400, exception.getCode());
    }
    
    @Test
    @DisplayName("上传成功 - 边界值2MB - 验证Property 18")
    void uploadImage_Success_ExactlyMaxSize() {
        // 准备：正好2MB
        byte[] content = new byte[2 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", content);
        
        // 执行
        String imageUrl = uploadService.uploadImage(file);
        
        // 验证
        assertNotNull(imageUrl);
        assertTrue(imageUrl.startsWith("/uploads/"));
    }
    
    @Test
    @DisplayName("上传失败 - 文件为null - 验证Property 18")
    void uploadImage_Fail_NullFile() {
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> uploadService.uploadImage(null));
        assertEquals(400, exception.getCode());
    }
}
