package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.vo.review.ReviewVO;

/**
 * 管理员评价服务接口
 */
public interface AdminReviewService {
    
    /**
     * 获取评价列表（含已删除）
     */
    PageResult<ReviewVO> listReviews(Integer page, Integer pageSize);
    
    /**
     * 软删除评价
     */
    void deleteReview(Long reviewId);
}
