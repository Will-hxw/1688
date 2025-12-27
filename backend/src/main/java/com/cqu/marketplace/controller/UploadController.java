package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.service.UploadService;
import com.cqu.marketplace.vo.upload.UploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private final UploadService uploadService;
    
    /**
     * 上传图片
     */
    @PostMapping
    public Result<UploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = uploadService.uploadImage(file);
        UploadVO vo = new UploadVO();
        vo.setImageUrl(imageUrl);
        return Result.success(vo);
    }
}
