package com.cqu.marketplace.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.review.client.OrderClient;
import com.cqu.marketplace.review.client.ProductClient;
import com.cqu.marketplace.review.client.UserClient;
import com.cqu.marketplace.review.dto.ReviewCreateRequest;
import com.cqu.marketplace.review.entity.Review;
import com.cqu.marketplace.review.mapper.ReviewMapper;
import com.cqu.marketplace.review.service.ReviewService;
import com.cqu.marketplace.review.vo.OrderInfo;
import com.cqu.marketplace.review.vo.ProductInfo;
import com.cqu.marketplace.review.vo.ReviewVO;
import com.cqu.marketplace.review.vo.UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewMapper reviewMapper;
    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final UserClient userClient;
    
    @Override
    @Transactional
    public Long createReview(Long buyerId, ReviewCreateRequest request) {
        // 1. 调用 Order Service 获取订单信息
        Result<OrderInfo> orderResult = orderClient.getOrderInfo(request.getOrderId());
        if (orderResult.getCode() != 200 || orderResult.getData() == null) {
            throw BusinessException.notFound("订单不存在");
        }
        OrderInfo order = orderResult.getData();
        
        // 2. 权限校验：只有买家可以评价
        if (!order.getBuyerId().equals(buyerId)) {
            throw BusinessException.forbidden("只有买家可以评价");
        }
        
        // 3. 状态校验：只有RECEIVED状态可以评价
        if (!OrderStatus.RECEIVED.getCode().equals(order.getStatus())) {
            throw BusinessException.conflict("订单状态不允许评价，请先确认收货");
        }
        
        // 4. 幂等校验：检查是否已评价
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, request.getOrderId());
        if (reviewMapper.selectCount(wrapper) > 0) {
            throw BusinessException.conflict("该订单已评价");
        }
        
        // 5. 评分范围校验（DTO已校验，这里双重保险）
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
        
        // 7. 调用 Order Service 更新订单状态为 REVIEWED
        try {
            Result<Void> markResult = orderClient.markAsReviewed(order.getId());
            if (markResult.getCode() != 200) {
                log.warn("标记订单为已评价失败: orderId={}, message={}", order.getId(), markResult.getMessage());
                // 不回滚评价创建，因为评价已经成功
            }
        } catch (Exception e) {
            log.error("调用Order Service标记已评价失败: orderId={}", order.getId(), e);
            // 不回滚评价创建，因为评价已经成功
        }
        
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
    
    @Override
    public PageResult<ReviewVO> listAllReviews(Integer page, Integer pageSize) {
        Page<Review> pageObj = new Page<>(page, pageSize);
        
        // 管理员可以看到所有评价，包括已删除的
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Review::getCreatedAt);
        
        Page<Review> result = reviewMapper.selectPage(pageObj, wrapper);
        
        List<ReviewVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        // 批量填充商品名称和买家昵称
        enrichReviewsWithDetails(voList);
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public void deleteReview(Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw BusinessException.notFound("评价不存在");
        }
        
        if (review.getDeleted()) {
            throw BusinessException.conflict("评价已被删除");
        }
        
        review.setDeleted(true);
        reviewMapper.updateById(review);
        
        log.info("管理员删除评价成功: reviewId={}", reviewId);
    }
    
    /**
     * 转换为VO
     * 注意：微服务架构下不跨服务查询商品名称和买家昵称
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
        
        // 微服务架构下不跨服务查询，productName 和 buyerNickname 保持为 null
        // 如需要可通过前端聚合或 BFF 层处理
        
        return vo;
    }
    
    /**
     * 批量填充评价的商品名称和买家昵称
     * 用于管理后台展示
     */
    private void enrichReviewsWithDetails(List<ReviewVO> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        
        // 收集所有需要查询的商品ID和买家ID
        Set<Long> productIds = reviews.stream()
            .map(ReviewVO::getProductId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        
        Set<Long> buyerIds = reviews.stream()
            .map(ReviewVO::getBuyerId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        
        // 批量查询商品信息
        Map<Long, String> productNameMap = productIds.stream()
            .collect(Collectors.toMap(
                id -> id,
                id -> {
                    try {
                        Result<ProductInfo> result = productClient.getProductInfo(id);
                        if (result != null && result.getCode() == 200 && result.getData() != null) {
                            return result.getData().getName();
                        }
                    } catch (Exception e) {
                        log.warn("获取商品信息失败: productId={}", id, e);
                    }
                    return "商品已删除";
                }
            ));
        
        // 批量查询用户信息
        Map<Long, String> buyerNicknameMap = buyerIds.stream()
            .collect(Collectors.toMap(
                id -> id,
                id -> {
                    try {
                        Result<UserInfo> result = userClient.getUserInfo(id);
                        if (result != null && result.getCode() == 200 && result.getData() != null) {
                            return result.getData().getNickname();
                        }
                    } catch (Exception e) {
                        log.warn("获取用户信息失败: userId={}", id, e);
                    }
                    return "用户已注销";
                }
            ));
        
        // 填充到VO
        for (ReviewVO vo : reviews) {
            if (vo.getProductId() != null) {
                vo.setProductName(productNameMap.get(vo.getProductId()));
            }
            if (vo.getBuyerId() != null) {
                vo.setBuyerNickname(buyerNicknameMap.get(vo.getBuyerId()));
            }
        }
    }
}
