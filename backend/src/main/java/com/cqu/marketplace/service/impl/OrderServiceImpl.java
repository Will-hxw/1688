package com.cqu.marketplace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
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
import com.cqu.marketplace.service.OrderService;
import com.cqu.marketplace.service.ProductService;
import com.cqu.marketplace.vo.order.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 * 核心特性：幂等下单、原子扣减库存、状态机校验
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final ProductService productService;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    
    /** 幂等键Redis前缀 */
    private static final String IDEM_KEY_PREFIX = "idem:";
    /** 幂等键过期时间（分钟） */
    private static final long IDEM_KEY_TTL = 5;
    
    @Override
    @Transactional
    public Long createOrder(Long buyerId, OrderCreateRequest request, String idempotencyKey) {
        // 1. 幂等键校验（Redis）
        String redisKey = IDEM_KEY_PREFIX + buyerId + ":" + idempotencyKey;
        String existingOrderId = redisTemplate.opsForValue().get(redisKey);
        if (existingOrderId != null) {
            log.info("幂等键命中，返回已有订单: orderId={}", existingOrderId);
            return Long.parseLong(existingOrderId);
        }
        
        // 2. 检查是否已存在相同幂等键的订单（DB兜底）
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, buyerId)
               .eq(Order::getIdempotencyKey, idempotencyKey);
        Order existingOrder = orderMapper.selectOne(wrapper);
        if (existingOrder != null) {
            log.info("数据库幂等键命中，返回已有订单: orderId={}", existingOrder.getId());
            // 补充Redis缓存
            redisTemplate.opsForValue().set(redisKey, existingOrder.getId().toString(), 
                IDEM_KEY_TTL, TimeUnit.MINUTES);
            return existingOrder.getId();
        }
        
        // 3. 获取商品信息
        Product product = productService.getProductById(request.getProductId());
        
        // 4. 校验：不能购买自己的商品
        if (product.getSellerId().equals(buyerId)) {
            throw BusinessException.conflict("不能购买自己的商品");
        }
        
        // 5. 原子扣减库存（库存-1，库存为0时自动变为SOLD）
        boolean updated = productService.decrementStock(product.getId());
        if (!updated) {
            throw BusinessException.conflict("商品库存不足或不可购买");
        }

        // 6. 创建订单
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(product.getId());
        order.setPrice(product.getPrice());
        order.setStatus(OrderStatus.CREATED);
        order.setIdempotencyKey(idempotencyKey);
        
        orderMapper.insert(order);
        
        // 7. 设置Redis幂等键
        redisTemplate.opsForValue().set(redisKey, order.getId().toString(), 
            IDEM_KEY_TTL, TimeUnit.MINUTES);
        
        log.info("订单创建成功: orderId={}, buyerId={}, productId={}", 
            order.getId(), buyerId, product.getId());
        
        return order.getId();
    }
    
    @Override
    @Transactional
    public void shipOrder(Long orderId, Long sellerId) {
        Order order = getOrderById(orderId);
        
        // 权限校验：仅卖家可发货
        if (!order.getSellerId().equals(sellerId)) {
            throw BusinessException.forbidden("无权操作此订单");
        }
        
        // 状态校验：仅CREATED可发货
        if (order.getStatus() != OrderStatus.CREATED) {
            throw BusinessException.conflict("订单状态不允许发货");
        }
        
        // 原子更新状态
        int rows = orderMapper.updateStatusAtomic(orderId, 
            OrderStatus.CREATED.getCode(), OrderStatus.SHIPPED.getCode());
        if (rows == 0) {
            throw BusinessException.conflict("订单状态已变更，请刷新重试");
        }
        
        log.info("订单发货成功: orderId={}", orderId);
    }
    
    @Override
    @Transactional
    public void receiveOrder(Long orderId, Long buyerId) {
        Order order = getOrderById(orderId);
        
        // 权限校验：仅买家可确认收货
        if (!order.getBuyerId().equals(buyerId)) {
            throw BusinessException.forbidden("无权操作此订单");
        }
        
        // 状态校验：仅SHIPPED可确认收货
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw BusinessException.conflict("订单状态不允许确认收货");
        }
        
        // 原子更新状态
        int rows = orderMapper.updateStatusAtomic(orderId, 
            OrderStatus.SHIPPED.getCode(), OrderStatus.RECEIVED.getCode());
        if (rows == 0) {
            throw BusinessException.conflict("订单状态已变更，请刷新重试");
        }
        
        log.info("订单确认收货成功: orderId={}", orderId);
    }
    
    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId);
        
        // 权限校验：买家或卖家可取消
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw BusinessException.forbidden("无权操作此订单");
        }
        
        // 状态校验：仅CREATED可取消
        if (order.getStatus() != OrderStatus.CREATED) {
            throw BusinessException.conflict("订单状态不允许取消");
        }
        
        // 确定取消方
        CanceledBy canceledBy = isBuyer ? CanceledBy.BUYER : CanceledBy.SELLER;
        
        // 原子更新状态
        int rows = orderMapper.updateStatusWithCanceledBy(orderId, 
            OrderStatus.CREATED.getCode(), OrderStatus.CANCELED.getCode(), canceledBy.getCode());
        if (rows == 0) {
            throw BusinessException.conflict("订单状态已变更，请刷新重试");
        }
        
        // 恢复商品库存（库存+1，状态恢复为ON_SALE）
        productService.incrementStock(order.getProductId());
        
        log.info("订单取消成功: orderId={}, canceledBy={}", orderId, canceledBy);
    }

    
    @Override
    public PageResult<OrderVO> getBuyerOrders(Long buyerId, String status, Integer page, Integer pageSize) {
        Page<Order> pageObj = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuyerId, buyerId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, OrderStatus.valueOf(status));
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        
        Page<Order> result = orderMapper.selectPage(pageObj, wrapper);
        
        List<OrderVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public PageResult<OrderVO> getSellerOrders(Long sellerId, String status, Integer page, Integer pageSize) {
        Page<Order> pageObj = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getSellerId, sellerId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, OrderStatus.valueOf(status));
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        
        Page<Order> result = orderMapper.selectPage(pageObj, wrapper);
        
        List<OrderVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        return order;
    }
    
    @Override
    @Transactional
    public boolean updateToReviewed(Long orderId) {
        int rows = orderMapper.updateStatusAtomic(orderId, 
            OrderStatus.RECEIVED.getCode(), OrderStatus.REVIEWED.getCode());
        return rows > 0;
    }
    
    /**
     * 转换为VO
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setProductId(order.getProductId());
        vo.setPrice(order.getPrice());
        vo.setStatus(order.getStatus().getCode());
        vo.setCreatedAt(order.getCreatedAt());
        
        if (order.getCanceledBy() != null) {
            vo.setCanceledBy(order.getCanceledBy().getCode());
        }
        
        // 获取买家昵称
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        
        // 获取卖家昵称
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        
        // 获取商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImageUrl());
        }
        
        return vo;
    }
}
