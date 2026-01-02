package com.cqu.marketplace.review.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.review.dto.ReviewCreateRequest;
import com.cqu.marketplace.review.service.ReviewService;
import com.cqu.marketplace.review.vo.ReviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 评价控制器
 */
@RestController
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 创建评价
     */
    @PostMapping("/reviews")
    public Result<Long> createReview(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        
        Long reviewId = reviewService.createReview(userId, request);
        return Result.success(reviewId);
    }
    
    /**
     * 获取商品评价列表（匿名可访问）
     */
    @GetMapping("/reviews/product/{productId}")
    public Result<PageResult<ReviewVO>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<ReviewVO> result = reviewService.getProductReviews(productId, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 获取我的评价列表
     */
    @GetMapping("/reviews/my")
    public Result<PageResult<ReviewVO>> getMyReviews(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<ReviewVO> result = reviewService.getMyReviews(userId, page, pageSize);
        return Result.success(result);
    }
}
