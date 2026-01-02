package com.cqu.marketplace.review;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.review.client.OrderClient;
import com.cqu.marketplace.review.dto.ReviewCreateRequest;
import com.cqu.marketplace.review.entity.Review;
import com.cqu.marketplace.review.mapper.ReviewMapper;
import com.cqu.marketplace.review.service.ReviewService;
import com.cqu.marketplace.review.vo.OrderInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 评价创建集成测试
 * 使用 @MockBean 模拟 OrderClient
 * 
 * Requirements: 5.1-5.3
 */
@SpringBootTest
@ActiveProfiles("test")
class ReviewCreationIntegrationTest {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private ReviewMapper reviewMapper;
    
    @MockBean
    private OrderClient orderClient;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        reviewMapper.delete(null);
    }
    
    @Test
    @DisplayName("创建评价成功 - 订单状态为 RECEIVED")
    void createReview_Success_WhenOrderStatusIsReceived() {
        // Given: 模拟 Order Service 返回 RECEIVED 状态的订单
        Long orderId = 1L;
        Long buyerId = 100L;
        Long sellerId = 200L;
        Long productId = 300L;
        
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setBuyerId(buyerId);
        orderInfo.setSellerId(sellerId);
        orderInfo.setProductId(productId);
        orderInfo.setStatus(OrderStatus.RECEIVED.getCode());
        
        when(orderClient.getOrderInfo(orderId)).thenReturn(Result.success(orderInfo));
        when(orderClient.markAsReviewed(orderId)).thenReturn(Result.success(null));
        
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(orderId);
        request.setRating(5);
        request.setContent("非常满意！");
        
        // When: 创建评价
        Long reviewId = reviewService.createReview(buyerId, request);
        
        // Then: 评价创建成功
        assertThat(reviewId).isNotNull();
        
        Review review = reviewMapper.selectById(reviewId);
        assertThat(review).isNotNull();
        assertThat(review.getOrderId()).isEqualTo(orderId);
        assertThat(review.getBuyerId()).isEqualTo(buyerId);
        assertThat(review.getSellerId()).isEqualTo(sellerId);
        assertThat(review.getProductId()).isEqualTo(productId);
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("非常满意！");
        assertThat(review.getDeleted()).isFalse();
        
        // 验证调用了 markAsReviewed
        verify(orderClient, times(1)).markAsReviewed(orderId);
    }
    
    @Test
    @DisplayName("创建评价失败 - 订单状态不是 RECEIVED")
    void createReview_Fail_WhenOrderStatusIsNotReceived() {
        // Given: 模拟 Order Service 返回 SHIPPED 状态的订单
        Long orderId = 2L;
        Long buyerId = 100L;
        
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setBuyerId(buyerId);
        orderInfo.setSellerId(200L);
        orderInfo.setProductId(300L);
        orderInfo.setStatus(OrderStatus.SHIPPED.getCode());
        
        when(orderClient.getOrderInfo(orderId)).thenReturn(Result.success(orderInfo));
        
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(orderId);
        request.setRating(4);
        request.setContent("测试");
        
        // When & Then: 创建评价应该失败
        assertThatThrownBy(() -> reviewService.createReview(buyerId, request))
            .hasMessageContaining("订单状态不允许评价");
        
        // 验证没有调用 markAsReviewed
        verify(orderClient, never()).markAsReviewed(anyLong());
    }
    
    @Test
    @DisplayName("创建评价失败 - 非买家尝试评价")
    void createReview_Fail_WhenNotBuyer() {
        // Given: 模拟 Order Service 返回订单，但当前用户不是买家
        Long orderId = 3L;
        Long actualBuyerId = 100L;
        Long wrongUserId = 999L;
        
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setBuyerId(actualBuyerId);
        orderInfo.setSellerId(200L);
        orderInfo.setProductId(300L);
        orderInfo.setStatus(OrderStatus.RECEIVED.getCode());
        
        when(orderClient.getOrderInfo(orderId)).thenReturn(Result.success(orderInfo));
        
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(orderId);
        request.setRating(3);
        request.setContent("测试");
        
        // When & Then: 非买家创建评价应该失败
        assertThatThrownBy(() -> reviewService.createReview(wrongUserId, request))
            .hasMessageContaining("只有买家可以评价");
    }
    
    @Test
    @DisplayName("创建评价失败 - 订单已评价（幂等性）")
    void createReview_Fail_WhenAlreadyReviewed() {
        // Given: 先创建一个评价
        Long orderId = 4L;
        Long buyerId = 100L;
        
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setBuyerId(buyerId);
        orderInfo.setSellerId(200L);
        orderInfo.setProductId(300L);
        orderInfo.setStatus(OrderStatus.RECEIVED.getCode());
        
        when(orderClient.getOrderInfo(orderId)).thenReturn(Result.success(orderInfo));
        when(orderClient.markAsReviewed(orderId)).thenReturn(Result.success(null));
        
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(orderId);
        request.setRating(5);
        request.setContent("第一次评价");
        
        // 第一次创建成功
        reviewService.createReview(buyerId, request);
        
        // When & Then: 第二次创建应该失败
        request.setContent("第二次评价");
        assertThatThrownBy(() -> reviewService.createReview(buyerId, request))
            .hasMessageContaining("该订单已评价");
    }
    
    @Test
    @DisplayName("创建评价成功后调用 markAsReviewed")
    void createReview_CallsMarkAsReviewed_AfterSuccess() {
        // Given
        Long orderId = 5L;
        Long buyerId = 100L;
        
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setBuyerId(buyerId);
        orderInfo.setSellerId(200L);
        orderInfo.setProductId(300L);
        orderInfo.setStatus(OrderStatus.RECEIVED.getCode());
        
        when(orderClient.getOrderInfo(orderId)).thenReturn(Result.success(orderInfo));
        when(orderClient.markAsReviewed(orderId)).thenReturn(Result.success(null));
        
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(orderId);
        request.setRating(4);
        request.setContent("测试 markAsReviewed 调用");
        
        // When
        reviewService.createReview(buyerId, request);
        
        // Then: 验证 markAsReviewed 被调用
        verify(orderClient, times(1)).markAsReviewed(orderId);
    }
    
    @Test
    @DisplayName("markAsReviewed 失败不影响评价创建")
    void createReview_Success_EvenIfMarkAsReviewedFails() {
        // Given: markAsReviewed 返回失败
        Long orderId = 6L;
        Long buyerId = 100L;
        
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setBuyerId(buyerId);
        orderInfo.setSellerId(200L);
        orderInfo.setProductId(300L);
        orderInfo.setStatus(OrderStatus.RECEIVED.getCode());
        
        when(orderClient.getOrderInfo(orderId)).thenReturn(Result.success(orderInfo));
        when(orderClient.markAsReviewed(orderId)).thenReturn(Result.error(409, "状态更新失败"));
        
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(orderId);
        request.setRating(5);
        request.setContent("测试");
        
        // When: 创建评价
        Long reviewId = reviewService.createReview(buyerId, request);
        
        // Then: 评价仍然创建成功
        assertThat(reviewId).isNotNull();
        Review review = reviewMapper.selectById(reviewId);
        assertThat(review).isNotNull();
    }
}
