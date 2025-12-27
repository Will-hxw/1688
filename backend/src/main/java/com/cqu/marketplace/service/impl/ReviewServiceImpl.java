package com.cqu.marketplace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.review.ReviewCreateRequest;
import com.cqu.marketplace.entity.Order;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.Review;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.ReviewMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.OrderService;
import com.cqu.marketplace.service.ReviewService;
import com.cqu.marketplace.vo.review.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewMapper reviewMapper;
    private final OrderService orderService;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public Long createReview(Long buyerId, ReviewCreateRequest request) {
        // 1. 获取订单
        Order order = orderService.getOrderById(request.getOrderId());
        
        // 2. 权限校验：只有买家可以评价
        if (!order.getBuyerId().equals(buyerId)) {
            throw BusinessException.forbidden("只有买家可以评价");
        }
        
        // 3. 状态校验：只有RECEIVED状态可以评价
        if (order.getStatus() != OrderStatus.RECEIVED) {
            throw BusinessException.conflict("订单状态不允许评价，请先确认收货");
        }
        
        // 4. 幂等校验：检查是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, request.getOrderId());
        if (reviewMapper.selectCount(wrapper) > 0) {
            throw BusinessException.conflict("该订单已评价");
        }
        
        // 5. 评分范围校验
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw BusinessException.badRequest("评分必须在1-5之间");
        }
        
        // 6. 创建评价
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setProductId(order.getProductId());
        review.setBuyerId(buyerId);
        review.setSellerId(order.getSellerId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        review.setDeleted(false);
        
        reviewMapper.insert(review);
        
        // 7. 更新订单状态为REVIEWED
        orderService.updateToReviewed(order.getId());
        
        log.info("评价创建成功: reviewId={}, orderId={}", review.getId(), order.getId());
        
        return review.getId();
    }
    
    @Override
    public PageResult<ReviewVO> getProductReviews(Long productId, Integer page, Integer pageSize) {
        Page<Review> pageObj = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getProductId, productId)
               .eq(Review::getDeleted, false)
               .orderByDesc(Review::getCreatedAt);
        
        Page<Review> result = reviewMapper.selectPage(pageObj, wrapper);
        
        List<ReviewVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public PageResult<ReviewVO> getMyReviews(Long buyerId, Integer page, Integer pageSize) {
        Page<Review> pageObj = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getBuyerId, buyerId)
               .orderByDesc(Review::getCreatedAt);
        
        Page<Review> result = reviewMapper.selectPage(pageObj, wrapper);
        
        List<ReviewVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    /**
     * 转换为VO
     */
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
        
        // 获取商品名称
        Product product = productMapper.selectById(review.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
        }
        
        // 获取买家昵称
        User buyer = userMapper.selectById(review.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        
        return vo;
    }
}
