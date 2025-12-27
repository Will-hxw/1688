package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.order.OrderCreateRequest;
import com.cqu.marketplace.security.CurrentUser;
import com.cqu.marketplace.security.UserPrincipal;
import com.cqu.marketplace.service.OrderService;
import com.cqu.marketplace.vo.order.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    /**
     * 创建订单
     * 必须携带Idempotency-Key请求头
     */
    @PostMapping
    public Result<Long> createOrder(
            @CurrentUser UserPrincipal user,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody OrderCreateRequest request) {
        
        // 幂等键强制校验
        if (!StringUtils.hasText(idempotencyKey)) {
            throw BusinessException.badRequest("缺少Idempotency-Key请求头");
        }
        
        Long orderId = orderService.createOrder(user.getId(), request, idempotencyKey);
        return Result.success(orderId);
    }
    
    /**
     * 发货（卖家）
     */
    @PostMapping("/{id}/ship")
    public Result<Void> shipOrder(
            @CurrentUser UserPrincipal user,
            @PathVariable("id") Long orderId) {
        
        orderService.shipOrder(orderId, user.getId());
        return Result.success(null);
    }
    
    /**
     * 确认收货（买家）
     */
    @PostMapping("/{id}/receive")
    public Result<Void> receiveOrder(
            @CurrentUser UserPrincipal user,
            @PathVariable("id") Long orderId) {
        
        orderService.receiveOrder(orderId, user.getId());
        return Result.success(null);
    }
    
    /**
     * 取消订单（买家/卖家）
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelOrder(
            @CurrentUser UserPrincipal user,
            @PathVariable("id") Long orderId) {
        
        orderService.cancelOrder(orderId, user.getId());
        return Result.success(null);
    }
    
    /**
     * 买家订单列表
     */
    @GetMapping("/buyer")
    public Result<PageResult<OrderVO>> getBuyerOrders(
            @CurrentUser UserPrincipal user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<OrderVO> result = orderService.getBuyerOrders(user.getId(), status, page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 卖家订单列表
     */
    @GetMapping("/seller")
    public Result<PageResult<OrderVO>> getSellerOrders(
            @CurrentUser UserPrincipal user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        PageResult<OrderVO> result = orderService.getSellerOrders(user.getId(), status, page, pageSize);
        return Result.success(result);
    }
}
