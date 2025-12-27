package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.dto.review.ReviewCreateRequest;
import com.cqu.marketplace.vo.review.ReviewVO;

/**
 * 评价服务接口
 */
public interface ReviewService {
    
    /**
     * 创建评价
     * 前置条件：订单状态为RECEIVED
     * 幂等性：一个订单只能评价一次
     * 
     * @param buyerId 买家ID
     * @param request 创建请求
     * @return 评价ID
     */
    Long createReview(Long buyerId, ReviewCreateRequest request);
    
    /**
     * 获取商品评价列表
     * 仅返回deleted=false的评价
     * 
     * @param productId 商品ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    PageResult<ReviewVO> getProductReviews(Long productId, Integer page, Integer pageSize);
    
    /**
     * 获取我的评价列表
     * 
     * @param buyerId 买家ID
     * @param page 页码
     * @param pageSize 每页数量
     * @return 评价列表
     */
    PageResult<ReviewVO> getMyReviews(Long buyerId, Integer page, Integer pageSize);
}
