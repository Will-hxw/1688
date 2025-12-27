package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.dto.order.OrderStatusUpdateRequest;
import com.cqu.marketplace.service.AdminOrderService;
import com.cqu.marketplace.vo.order.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员订单控制器
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final AdminOrderService adminOrderService;
    
    /**
     * 获取订单列表
     */
    @GetMapping
    public Result<PageResult<OrderVO>> listOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<OrderVO> result = adminOrderService.listOrders(page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 修改订单状态（遵循状态机）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateOrderStatus(
            @PathVariable("id") Long orderId,
            @RequestBody OrderStatusUpdateRequest request) {
        adminOrderService.updateOrderStatus(orderId, request.getStatus());
        return Result.success(null);
    }
}
