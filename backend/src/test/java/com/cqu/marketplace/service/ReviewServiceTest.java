package com.cqu.marketplace.service;

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
import com.cqu.marketplace.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 评价服务测试
 * 验证: Property 13, 14, 15
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    
    @Mock
    private ReviewMapper reviewMapper;
    
    @Mock
    private OrderService orderService;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private ReviewServiceImpl reviewService;
    
    private Order testOrder;
    private Product testProduct;
    private User testBuyer;
    
    @BeforeEach
    void setUp() {
        // 初始化测试订单（RECEIVED状态）
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setProductId(1L);
        testOrder.setPrice(new BigDecimal("99.99"));
        testOrder.setStatus(OrderStatus.RECEIVED);
        
        // 初始化测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        
        // 初始化测试买家
        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setNickname("买家");
    }
    
    @Test
    @DisplayName("创建评价成功 - RECEIVED状态 - 验证Property 13: 评价前置条件")
    void createReview_Success_ReceivedStatus() {
        // 准备
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(1L);
        request.setRating(5);
        request.setContent("很好的商品");
        
        when(orderService.getOrderById(1L)).thenReturn(testOrder);
        when(reviewMapper.selectCount(any())).thenReturn(0L);
        when(reviewMapper.insert(any())).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1L);
            // 验证评价数据
            assertEquals(1L, review.getOrderId());
            assertEquals(1L, review.getProductId());
            assertEquals(1L, review.getBuyerId());
            assertEquals(5, review.getRating());
            assertFalse(review.getDeleted());
            return 1;
        });
        when(orderService.updateToReviewed(1L)).thenReturn(true);
        
        // 执行
        Long reviewId = reviewService.createReview(1L, request);
        
        // 验证
        assertNotNull(reviewId);
        verify(orderService).updateToReviewed(1L);
    }
    
    @Test
    @DisplayName("创建评价失败 - 非RECEIVED状态 - 验证Property 13: 评价前置条件")
    void createReview_Fail_NotReceivedStatus() {
        // 准备：订单状态为SHIPPED
        testOrder.setStatus(OrderStatus.SHIPPED);
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(1L);
        request.setRating(5);
        
        when(orderService.getOrderById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> reviewService.createReview(1L, request));
        assertEquals(409, exception.getCode());
        assertTrue(exception.getMessage().contains("确认收货"));
    }
    
    @Test
    @DisplayName("创建评价失败 - CREATED状态 - 验证Property 13")
    void createReview_Fail_CreatedStatus() {
        // 准备：订单状态为CREATED
        testOrder.setStatus(OrderStatus.CREATED);
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(1L);
        request.setRating(5);
        
        when(orderService.getOrderById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> reviewService.createReview(1L, request));
        assertEquals(409, exception.getCode());
    }
    
    @Test
    @DisplayName("创建评价失败 - 非买家 - 验证Property 13")
    void createReview_Fail_NotBuyer() {
        // 准备
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(1L);
        request.setRating(5);
        
        when(orderService.getOrderById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证：卖家尝试评价
        BusinessException exception = assertThrows(BusinessException.class,
            () -> reviewService.createReview(2L, request));
        assertEquals(403, exception.getCode());
    }
    
    @Test
    @DisplayName("创建评价失败 - 已评价 - 验证Property 14: 评价幂等性")
    void createReview_Fail_AlreadyReviewed() {
        // 准备
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(1L);
        request.setRating(5);
        
        when(orderService.getOrderById(1L)).thenReturn(testOrder);
        // 模拟已存在评价
        when(reviewMapper.selectCount(any())).thenReturn(1L);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> reviewService.createReview(1L, request));
        assertEquals(409, exception.getCode());
        assertTrue(exception.getMessage().contains("已评价"));
    }
    
    @Test
    @DisplayName("评分范围校验 - 评分为1 - 验证Property 15: 评分范围约束")
    void createReview_Success_Rating1() {
        // 准备
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(1L);
        request.setRating(1);
        request.setContent("不太满意");
        
        when(orderService.getOrderById(1L)).thenReturn(testOrder);
        when(reviewMapper.selectCount(any())).thenReturn(0L);
        when(reviewMapper.insert(any())).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1L);
            assertEquals(1, review.getRating());
            return 1;
        });
        when(orderService.updateToReviewed(1L)).thenReturn(true);
        
        // 执行
        Long reviewId = reviewService.createReview(1L, request);
        
        // 验证
        assertNotNull(reviewId);
    }
    
    @Test
    @DisplayName("评分范围校验 - 评分为5 - 验证Property 15: 评分范围约束")
    void createReview_Success_Rating5() {
        // 准备
        ReviewCreateRequest request = new ReviewCreateRequest();
        request.setOrderId(1L);
        request.setRating(5);
        request.setContent("非常满意");
        
        when(orderService.getOrderById(1L)).thenReturn(testOrder);
        when(reviewMapper.selectCount(any())).thenReturn(0L);
        when(reviewMapper.insert(any())).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1L);
            assertEquals(5, review.getRating());
            return 1;
        });
        when(orderService.updateToReviewed(1L)).thenReturn(true);
        
        // 执行
        Long reviewId = reviewService.createReview(1L, request);
        
        // 验证
        assertNotNull(reviewId);
    }
}
