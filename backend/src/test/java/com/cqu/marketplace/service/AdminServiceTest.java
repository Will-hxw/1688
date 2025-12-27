package com.cqu.marketplace.service;

import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.common.enums.UserStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.entity.Order;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.Review;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.OrderMapper;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.ReviewMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.impl.AdminOrderServiceImpl;
import com.cqu.marketplace.service.impl.AdminProductServiceImpl;
import com.cqu.marketplace.service.impl.AdminReviewServiceImpl;
import com.cqu.marketplace.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 管理员服务测试
 * 验证: Property 16: 管理员权限隔离
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private OrderMapper orderMapper;
    
    @Mock
    private ReviewMapper reviewMapper;
    
    @InjectMocks
    private AdminUserServiceImpl adminUserService;
    
    @InjectMocks
    private AdminProductServiceImpl adminProductService;
    
    @InjectMocks
    private AdminOrderServiceImpl adminOrderService;
    
    @InjectMocks
    private AdminReviewServiceImpl adminReviewService;
    
    private User testUser;
    private Product testProduct;
    private Order testOrder;
    private Review testReview;
    
    @BeforeEach
    void setUp() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setStatus(UserStatus.ACTIVE);
        
        // 初始化测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSellerId(1L);
        testProduct.setName("测试商品");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStatus(ProductStatus.ON_SALE);
        
        // 初始化测试订单
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setProductId(1L);
        testOrder.setStatus(OrderStatus.CREATED);
        
        // 初始化测试评价
        testReview = new Review();
        testReview.setId(1L);
        testReview.setOrderId(1L);
        testReview.setProductId(1L);
        testReview.setBuyerId(1L);
        testReview.setRating(5);
        testReview.setDeleted(false);
    }
    
    // ========== 用户管理测试 ==========
    
    @Test
    @DisplayName("禁用用户成功 - 验证Property 16")
    void disableUser_Success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any())).thenReturn(1);
        
        adminUserService.disableUser(1L);
        
        verify(userMapper).updateById(argThat(user -> 
            user.getStatus() == UserStatus.DISABLED));
    }
    
    @Test
    @DisplayName("禁用用户失败 - 用户不存在")
    void disableUser_Fail_NotFound() {
        when(userMapper.selectById(1L)).thenReturn(null);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> adminUserService.disableUser(1L));
        assertEquals(404, exception.getCode());
    }
    
    @Test
    @DisplayName("禁用用户失败 - 已被禁用")
    void disableUser_Fail_AlreadyDisabled() {
        testUser.setStatus(UserStatus.DISABLED);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> adminUserService.disableUser(1L));
        assertEquals(409, exception.getCode());
    }
    
    @Test
    @DisplayName("启用用户成功 - 验证Property 16")
    void enableUser_Success() {
        testUser.setStatus(UserStatus.DISABLED);
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any())).thenReturn(1);
        
        adminUserService.enableUser(1L);
        
        verify(userMapper).updateById(argThat(user -> 
            user.getStatus() == UserStatus.ACTIVE));
    }
    
    // ========== 商品管理测试 ==========
    
    @Test
    @DisplayName("管理员删除商品成功 - ON_SALE状态 - 验证Property 16")
    void deleteProduct_Success_OnSale() {
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        when(productMapper.deleteById(1L)).thenReturn(1);
        
        adminProductService.deleteProduct(1L);
        
        verify(productMapper).deleteById(1L);
    }
    
    @Test
    @DisplayName("管理员删除商品失败 - SOLD状态 - 验证Property 16")
    void deleteProduct_Fail_Sold() {
        testProduct.setStatus(ProductStatus.SOLD);
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> adminProductService.deleteProduct(1L));
        assertEquals(409, exception.getCode());
    }
    
    // ========== 订单管理测试 ==========
    
    @Test
    @DisplayName("管理员修改订单状态成功 - CREATED->SHIPPED - 验证Property 16")
    void updateOrderStatus_Success_CreatedToShipped() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);
        
        adminOrderService.updateOrderStatus(1L, OrderStatus.SHIPPED);
        
        verify(orderMapper).updateById(argThat(order -> 
            order.getStatus() == OrderStatus.SHIPPED));
    }
    
    @Test
    @DisplayName("管理员修改订单状态成功 - SHIPPED->RECEIVED - 验证Property 16")
    void updateOrderStatus_Success_ShippedToReceived() {
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any())).thenReturn(1);
        
        adminOrderService.updateOrderStatus(1L, OrderStatus.RECEIVED);
        
        verify(orderMapper).updateById(argThat(order -> 
            order.getStatus() == OrderStatus.RECEIVED));
    }
    
    @Test
    @DisplayName("管理员修改订单状态失败 - 非法转换 CREATED->RECEIVED")
    void updateOrderStatus_Fail_InvalidTransition() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> adminOrderService.updateOrderStatus(1L, OrderStatus.RECEIVED));
        assertEquals(409, exception.getCode());
    }
    
    @Test
    @DisplayName("管理员修改订单状态失败 - 终态不能转换")
    void updateOrderStatus_Fail_FinalState() {
        testOrder.setStatus(OrderStatus.CANCELED);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> adminOrderService.updateOrderStatus(1L, OrderStatus.SHIPPED));
        assertEquals(409, exception.getCode());
    }
    
    // ========== 评价管理测试 ==========
    
    @Test
    @DisplayName("管理员删除评价成功 - 验证Property 16")
    void deleteReview_Success() {
        when(reviewMapper.selectById(1L)).thenReturn(testReview);
        when(reviewMapper.updateById(any())).thenReturn(1);
        
        adminReviewService.deleteReview(1L);
        
        verify(reviewMapper).updateById(argThat(review -> 
            review.getDeleted()));
    }
    
    @Test
    @DisplayName("管理员删除评价失败 - 已删除")
    void deleteReview_Fail_AlreadyDeleted() {
        testReview.setDeleted(true);
        when(reviewMapper.selectById(1L)).thenReturn(testReview);
        
        BusinessException exception = assertThrows(BusinessException.class,
            () -> adminReviewService.deleteReview(1L));
        assertEquals(409, exception.getCode());
    }
}
