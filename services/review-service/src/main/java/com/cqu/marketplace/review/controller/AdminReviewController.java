package com.cqu.marketplace.review.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.review.service.ReviewService;
import com.cqu.marketplace.review.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员评价控制器
 */
@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * 获取评价列表（含已删除）
     */
    @GetMapping
    public Result<PageResult<ReviewVO>> listReviews(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ReviewVO> result = reviewService.listAllReviews(page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 软删除评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable("id") Long reviewId) {
        reviewService.deleteReview(reviewId);
        return Result.success(null);
    }
}
