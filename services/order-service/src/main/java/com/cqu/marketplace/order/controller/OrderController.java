package com.cqu.marketplace.order.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.order.dto.OrderCreateRequest;
import com.cqu.marketplace.order.service.OrderService;
import com.cqu.marketplace.order.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 * 从请求头获取用户信息（由Gateway注入）
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 创建订单
     * 必须携带Idempotency-Key请求头
     */
    @PostMapping
    public Result<Long> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderCreateRequest request) {
        
        // 幂等键强制校验
        if (!StringUtils.hasText(idempotencyKey)) {
            throw BusinessException.badRequest("缺少Idempotency-Key请求头");
        }
        
        Long orderId = orderService.createOrder(userId, request, idempotencyKey);
        return Result.success(orderId);
    }
    
    /**
     * 发货（卖家）
     */
    @PostMapping("/{id}/ship")
    public Result<Void> shipOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long orderId) {
        
        orderService.shipOrder(orderId, userId);
        return Result.success(null);
    }
    
    /**
     * 确认收货（买家）
     */
    @PostMapping("/{id}/receive")
    public Result<Void> receiveOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long orderId) {
        
        orderService.receiveOrder(orderId, userId);
        return Result.success(null);
    }
    
    /**
     * 取消订单（买家/卖家）
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable("id") Long orderId) {
        
        orderService.cancelOrder(orderId, userId);
        return Result.success(null);
    }
    
    /**
     * 买家订单列表
     */
    @GetMapping("/buyer")
    public Result<PageResult<OrderVO>> getBuyerOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<OrderVO> result = orderService.getBuyerOrders(userId, status, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 卖家订单列表
     */
    @GetMapping("/seller")
    public Result<PageResult<OrderVO>> getSellerOrders(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<OrderVO> result = orderService.getSellerOrders(userId, status, page, pageSize);
        return Result.success(result);
    }
}
