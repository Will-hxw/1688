package com.cqu.marketplace.service;

import com.cqu.marketplace.common.enums.CanceledBy;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.order.OrderCreateRequest;
import com.cqu.marketplace.entity.Order;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.OrderMapper;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 订单服务测试
 * 验证: Property 8, 9, 10, 10.1, 11, 12
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderMapper orderMapper;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private ProductService productService;
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private StringRedisTemplate redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;
    
    @InjectMocks
    private OrderServiceImpl orderService;
    
    private Product testProduct;
    private Order testOrder;
    private User testBuyer;
    private User testSeller;
    
    @BeforeEach
    void setUp() {
        // 初始化测试商品
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSellerId(2L);
        testProduct.setName("测试商品");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStatus(ProductStatus.ON_SALE);
        
        // 初始化测试订单
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setBuyerId(1L);
        testOrder.setSellerId(2L);
        testOrder.setProductId(1L);
        testOrder.setPrice(new BigDecimal("99.99"));
        testOrder.setStatus(OrderStatus.CREATED);
        
        // 初始化测试用户
        testBuyer = new User();
        testBuyer.setId(1L);
        testBuyer.setNickname("买家");
        
        testSeller = new User();
        testSeller.setId(2L);
        testSeller.setNickname("卖家");
    }
    
    @Test
    @DisplayName("创建订单成功 - 验证Property 8: 下单原子性")
    void createOrder_Success_AtomicOperation() {
        // 准备
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(1L);
        String idempotencyKey = "test-key-001";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectOne(any())).thenReturn(null);
        when(productService.getProductById(1L)).thenReturn(testProduct);
        when(productService.updateStatusAtomic(eq(1L), eq("ON_SALE"), eq("SOLD"))).thenReturn(true);
        when(orderMapper.insert(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            // 验证订单状态为CREATED
            assertEquals(OrderStatus.CREATED, order.getStatus());
            // 验证价格锁定
            assertEquals(testProduct.getPrice(), order.getPrice());
            return 1;
        });
        
        // 执行
        Long orderId = orderService.createOrder(1L, request, idempotencyKey);
        
        // 验证
        assertNotNull(orderId);
        verify(productService).updateStatusAtomic(1L, "ON_SALE", "SOLD");
        verify(orderMapper).insert(any(Order.class));
    }
    
    @Test
    @DisplayName("创建订单失败 - 商品已售出 - 验证Property 9: 并发下单互斥性")
    void createOrder_Fail_ProductSold() {
        // 准备
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(1L);
        String idempotencyKey = "test-key-002";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectOne(any())).thenReturn(null);
        when(productService.getProductById(1L)).thenReturn(testProduct);
        // 模拟原子更新失败（商品已被其他人购买）
        when(productService.updateStatusAtomic(eq(1L), eq("ON_SALE"), eq("SOLD"))).thenReturn(false);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.createOrder(1L, request, idempotencyKey));
        assertEquals(409, exception.getCode());
        assertTrue(exception.getMessage().contains("已售出"));
    }

    
    @Test
    @DisplayName("幂等键命中Redis - 返回已有订单 - 验证Property 10: 幂等键唯一性")
    void createOrder_IdempotencyHit_Redis() {
        // 准备
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(1L);
        String idempotencyKey = "test-key-003";
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 模拟Redis中已存在幂等键
        when(valueOperations.get(anyString())).thenReturn("100");
        
        // 执行
        Long orderId = orderService.createOrder(1L, request, idempotencyKey);
        
        // 验证：返回已有订单ID，不创建新订单
        assertEquals(100L, orderId);
        verify(orderMapper, never()).insert(any());
        verify(productService, never()).updateStatusAtomic(anyLong(), anyString(), anyString());
    }
    
    @Test
    @DisplayName("幂等键命中DB - 返回已有订单 - 验证Property 10: 幂等键唯一性")
    void createOrder_IdempotencyHit_Database() {
        // 准备
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(1L);
        String idempotencyKey = "test-key-004";
        
        Order existingOrder = new Order();
        existingOrder.setId(200L);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        // 模拟数据库中已存在相同幂等键的订单
        when(orderMapper.selectOne(any())).thenReturn(existingOrder);
        
        // 执行
        Long orderId = orderService.createOrder(1L, request, idempotencyKey);
        
        // 验证：返回已有订单ID
        assertEquals(200L, orderId);
        verify(orderMapper, never()).insert(any());
    }
    
    @Test
    @DisplayName("不能购买自己的商品 - 验证业务规则")
    void createOrder_Fail_BuyOwnProduct() {
        // 准备
        OrderCreateRequest request = new OrderCreateRequest();
        request.setProductId(1L);
        String idempotencyKey = "test-key-005";
        
        // 商品卖家ID与买家ID相同
        testProduct.setSellerId(1L);
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(orderMapper.selectOne(any())).thenReturn(null);
        when(productService.getProductById(1L)).thenReturn(testProduct);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.createOrder(1L, request, idempotencyKey));
        assertEquals(409, exception.getCode());
        assertTrue(exception.getMessage().contains("自己的商品"));
    }
    
    @Test
    @DisplayName("发货成功 - 卖家操作 - 验证Property 11, 12")
    void shipOrder_Success() {
        // 准备
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatusAtomic(1L, "CREATED", "SHIPPED")).thenReturn(1);
        
        // 执行
        assertDoesNotThrow(() -> orderService.shipOrder(1L, 2L));
        
        // 验证
        verify(orderMapper).updateStatusAtomic(1L, "CREATED", "SHIPPED");
    }
    
    @Test
    @DisplayName("发货失败 - 非卖家 - 验证Property 12: 订单操作权限")
    void shipOrder_Fail_NotSeller() {
        // 准备
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证：买家尝试发货
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.shipOrder(1L, 1L));
        assertEquals(403, exception.getCode());
    }
    
    @Test
    @DisplayName("发货失败 - 非CREATED状态 - 验证Property 11: 订单状态转换一致性")
    void shipOrder_Fail_WrongStatus() {
        // 准备
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.shipOrder(1L, 2L));
        assertEquals(409, exception.getCode());
    }
    
    @Test
    @DisplayName("确认收货成功 - 买家操作 - 验证Property 11, 12")
    void receiveOrder_Success() {
        // 准备
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatusAtomic(1L, "SHIPPED", "RECEIVED")).thenReturn(1);
        
        // 执行
        assertDoesNotThrow(() -> orderService.receiveOrder(1L, 1L));
        
        // 验证
        verify(orderMapper).updateStatusAtomic(1L, "SHIPPED", "RECEIVED");
    }
    
    @Test
    @DisplayName("确认收货失败 - 非买家 - 验证Property 12: 订单操作权限")
    void receiveOrder_Fail_NotBuyer() {
        // 准备
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证：卖家尝试确认收货
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.receiveOrder(1L, 2L));
        assertEquals(403, exception.getCode());
    }
    
    @Test
    @DisplayName("确认收货失败 - 非SHIPPED状态 - 验证Property 11")
    void receiveOrder_Fail_WrongStatus() {
        // 准备：订单状态为CREATED
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.receiveOrder(1L, 1L));
        assertEquals(409, exception.getCode());
    }

    
    @Test
    @DisplayName("取消订单成功 - 买家取消 - 验证Property 11, 12")
    void cancelOrder_Success_ByBuyer() {
        // 准备
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatusWithCanceledBy(1L, "CREATED", "CANCELED", "BUYER")).thenReturn(1);
        when(productService.updateStatusAtomic(1L, "SOLD", "ON_SALE")).thenReturn(true);
        
        // 执行
        assertDoesNotThrow(() -> orderService.cancelOrder(1L, 1L));
        
        // 验证：商品状态恢复
        verify(productService).updateStatusAtomic(1L, "SOLD", "ON_SALE");
        verify(orderMapper).updateStatusWithCanceledBy(1L, "CREATED", "CANCELED", "BUYER");
    }
    
    @Test
    @DisplayName("取消订单成功 - 卖家取消 - 验证Property 11, 12")
    void cancelOrder_Success_BySeller() {
        // 准备
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateStatusWithCanceledBy(1L, "CREATED", "CANCELED", "SELLER")).thenReturn(1);
        when(productService.updateStatusAtomic(1L, "SOLD", "ON_SALE")).thenReturn(true);
        
        // 执行
        assertDoesNotThrow(() -> orderService.cancelOrder(1L, 2L));
        
        // 验证
        verify(orderMapper).updateStatusWithCanceledBy(1L, "CREATED", "CANCELED", "SELLER");
    }
    
    @Test
    @DisplayName("取消订单失败 - 非买家非卖家 - 验证Property 12")
    void cancelOrder_Fail_NotParticipant() {
        // 准备
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证：第三方尝试取消
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.cancelOrder(1L, 999L));
        assertEquals(403, exception.getCode());
    }
    
    @Test
    @DisplayName("取消订单失败 - 非CREATED状态 - 验证Property 11")
    void cancelOrder_Fail_WrongStatus() {
        // 准备：订单已发货
        testOrder.setStatus(OrderStatus.SHIPPED);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> orderService.cancelOrder(1L, 1L));
        assertEquals(409, exception.getCode());
    }
    
    @Test
    @DisplayName("更新订单为已评价 - 验证状态转换")
    void updateToReviewed_Success() {
        // 准备
        when(orderMapper.updateStatusAtomic(1L, "RECEIVED", "REVIEWED")).thenReturn(1);
        
        // 执行
        boolean result = orderService.updateToReviewed(1L);
        
        // 验证
        assertTrue(result);
    }
    
    @Test
    @DisplayName("更新订单为已评价失败 - 状态不对")
    void updateToReviewed_Fail() {
        // 准备：状态不是RECEIVED
        when(orderMapper.updateStatusAtomic(1L, "RECEIVED", "REVIEWED")).thenReturn(0);
        
        // 执行
        boolean result = orderService.updateToReviewed(1L);
        
        // 验证
        assertFalse(result);
    }
}
