package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.dto.review.ReviewCreateRequest;
import com.cqu.marketplace.security.CurrentUser;
import com.cqu.marketplace.security.UserPrincipal;
import com.cqu.marketplace.service.ReviewService;
import com.cqu.marketplace.vo.review.ReviewVO;
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
            @CurrentUser UserPrincipal user,
            @Valid @RequestBody ReviewCreateRequest request) {
        
        Long reviewId = reviewService.createReview(user.getId(), request);
        return Result.success(reviewId);
    }
    
    /**
     * 获取商品评价列表（匿名可访问）
     */
    @GetMapping("/products/{productId}/reviews")
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
            @CurrentUser UserPrincipal user,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<ReviewVO> result = reviewService.getMyReviews(user.getId(), page, pageSize);
        return Result.success(result);
    }
}
