package com.cqu.marketplace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.Review;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.ReviewMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.AdminReviewService;
import com.cqu.marketplace.vo.review.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReviewServiceImpl implements AdminReviewService {
    
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    
    @Override
    public PageResult<ReviewVO> listReviews(Integer page, Integer pageSize) {
        Page<Review> pageParam = new Page<>(page, pageSize);
        // 管理员可以看到所有评价，包括已删除的
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Review::getCreatedAt);
        
        Page<Review> result = reviewMapper.selectPage(pageParam, wrapper);
        
        List<ReviewVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(404, "评价不存在");
        }
        
        if (review.getDeleted()) {
            throw new BusinessException(409, "评价已被删除");
        }
        
        review.setDeleted(true);
        reviewMapper.updateById(review);
        
        log.info("管理员删除评价成功: reviewId={}", reviewId);
    }
    
    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setProductId(review.getProductId());
        vo.setBuyerId(review.getBuyerId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setDeleted(review.getDeleted());
        vo.setCreatedAt(review.getCreatedAt());
        
        // 获取买家信息
        User buyer = userMapper.selectById(review.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        
        // 获取商品信息
        Product product = productMapper.selectById(review.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
        }
        
        return vo;
    }
}
