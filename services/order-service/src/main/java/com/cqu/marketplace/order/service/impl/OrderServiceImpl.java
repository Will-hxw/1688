package com.cqu.marketplace.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.enums.CanceledBy;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.order.client.ProductClient;
import com.cqu.marketplace.order.dto.OrderCreateRequest;
import com.cqu.marketplace.order.dto.StockRequest;
import com.cqu.marketplace.order.entity.Order;
import com.cqu.marketplace.order.mapper.OrderMapper;
import com.cqu.marketplace.order.service.OrderService;
import com.cqu.marketplace.order.vo.OrderInfo;
import com.cqu.marketplace.order.vo.OrderVO;
import com.cqu.marketplace.order.vo.ProductSnapshot;
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
 * 核心特性：幂等下单、原子扣减库存、状态机校验、商品快照
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final OrderMapper orderMapper;
    private final ProductClient productClient;
    private final StringRedisTemplate redisTemplate;
    
    /** 幂等键Redis前缀 */
    private static final String IDEM_KEY_PREFIX = "idem:";
    /** 幂等键初始TTL（秒）- 处理中状态 */
    private static final long IDEM_PENDING_TTL = 30;
    /** 幂等键最终TTL（秒）- 成功状态 */
    private static final long IDEM_SUCCESS_TTL = 300;
    /** 处理中标记 */
    private static final String PENDING_VALUE = "PENDING";
    /** 订单ID前缀 */
    private static final String ORDER_PREFIX = "order:";
    
    @Override
    @Transactional
    public Long createOrder(Long buyerId, OrderCreateRequest request, String idempotencyKey) {
        String redisKey = IDEM_KEY_PREFIX + buyerId + ":" + idempotencyKey;
        
        // 1. 尝试设置幂等键（SETNX）
        Boolean setSuccess = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, PENDING_VALUE, IDEM_PENDING_TTL, TimeUnit.SECONDS);
        
        if (Boolean.FALSE.equals(setSuccess)) {
            // 幂等键已存在，检查状态
            String existingValue = redisTemplate.opsForValue().get(redisKey);
            if (existingValue != null && existingValue.startsWith(ORDER_PREFIX)) {
                // 已成功创建订单，返回订单ID
                Long orderId = Long.parseLong(existingValue.substring(ORDER_PREFIX.length()));
                log.info("幂等键命中（Redis），返回已有订单: orderId={}", orderId);
                return orderId;
            }
            if (PENDING_VALUE.equals(existingValue)) {
                // 正在处理中
                throw BusinessException.conflict("请求正在处理中，请稍后重试");
            }
        }
        
        try {
            // 2. 检查数据库是否已存在相同幂等键的订单（DB兜底）
            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Order::getBuyerId, buyerId)
                   .eq(Order::getIdempotencyKey, idempotencyKey);
            Order existingOrder = orderMapper.selectOne(wrapper);
            if (existingOrder != null) {
                log.info("幂等键命中（DB），返回已有订单: orderId={}", existingOrder.getId());
                // 补充Redis缓存
                redisTemplate.opsForValue().set(redisKey, ORDER_PREFIX + existingOrder.getId(), 
                    IDEM_SUCCESS_TTL, TimeUnit.SECONDS);
                return existingOrder.getId();
            }
            
            // 3. 获取商品快照
            Result<ProductSnapshot> snapshotResult = productClient.getProductSnapshot(request.getProductId());
            if (snapshotResult.getCode() != 200 || snapshotResult.getData() == null) {
                throw BusinessException.notFound("商品不存在");
            }
            ProductSnapshot snapshot = snapshotResult.getData();
            
            // 4. 校验商品状态
            if (!"ON_SALE".equals(snapshot.getStatus())) {
                throw BusinessException.conflict("商品不可购买");
            }
            
            // 5. 校验：不能购买自己的商品
            if (snapshot.getSellerId().equals(buyerId)) {
                throw BusinessException.conflict("不能购买自己的商品");
            }
            
            // 6. 扣减库存
            Result<Void> stockResult = productClient.decreaseStock(
                    request.getProductId(), new StockRequest(null));
            if (stockResult.getCode() != 200) {
                throw BusinessException.conflict("商品库存不足或不可购买");
            }
            
            // 7. 创建订单（含快照字段）
            Order order = new Order();
            order.setBuyerId(buyerId);
            order.setSellerId(snapshot.getSellerId());
            order.setProductId(request.getProductId());
            order.setPrice(snapshot.getPrice());
            order.setProductName(snapshot.getName());
            order.setProductImage(snapshot.getImageUrl());
            order.setStatus(OrderStatus.CREATED);
            order.setIdempotencyKey(idempotencyKey);
            
            orderMapper.insert(order);
            
            // 8. 更新Redis幂等键为成功状态
            redisTemplate.opsForValue().set(redisKey, ORDER_PREFIX + order.getId(), 
                IDEM_SUCCESS_TTL, TimeUnit.SECONDS);
            
            log.info("订单创建成功: orderId={}, buyerId={}, productId={}", 
                order.getId(), buyerId, request.getProductId());
            
            return order.getId();
            
        } catch (Exception e) {
            // 处理失败，删除幂等键允许重试
            redisTemplate.delete(redisKey);
            throw e;
        }
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
        
        // 回滚库存
        try {
            productClient.increaseStock(order.getProductId(), new StockRequest(orderId));
            log.info("库存回滚成功: orderId={}, productId={}", orderId, order.getProductId());
        } catch (Exception e) {
            log.error("库存回滚失败: orderId={}, productId={}", orderId, order.getProductId(), e);
            // 库存回滚失败不影响订单取消，记录日志后续处理
        }
        
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
    public OrderInfo getOrderInfo(Long orderId) {
        Order order = getOrderById(orderId);
        OrderInfo info = new OrderInfo();
        info.setId(order.getId());
        info.setBuyerId(order.getBuyerId());
        info.setSellerId(order.getSellerId());
        info.setProductId(order.getProductId());
        info.setStatus(order.getStatus().getCode());
        return info;
    }
    
    @Override
    @Transactional
    public boolean updateToReviewed(Long orderId) {
        int rows = orderMapper.updateStatusAtomic(orderId, 
            OrderStatus.RECEIVED.getCode(), OrderStatus.REVIEWED.getCode());
        return rows > 0;
    }
    
    /**
     * 转换为VO（使用快照字段，无需跨服务查询）
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setProductId(order.getProductId());
        vo.setProductName(order.getProductName());
        vo.setProductImage(order.getProductImage());
        vo.setPrice(order.getPrice());
        vo.setStatus(order.getStatus().getCode());
        vo.setCreatedAt(order.getCreatedAt());
        
        if (order.getCanceledBy() != null) {
            vo.setCanceledBy(order.getCanceledBy().getCode());
            vo.setCanceledAt(order.getCanceledAt());
        }
        
        return vo;
    }
}
