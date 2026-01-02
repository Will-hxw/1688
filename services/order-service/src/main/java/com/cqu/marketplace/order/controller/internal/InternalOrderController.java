package com.cqu.marketplace.order.controller.internal;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.order.service.OrderService;
import com.cqu.marketplace.order.vo.OrderInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内部订单API控制器
 * 仅供服务间调用，Gateway会拦截外部访问
 */
@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {
    
    private final OrderService orderService;
    
    /**
     * 获取订单信息
     */
    @GetMapping("/{id}")
    public Result<OrderInfo> getOrderInfo(@PathVariable("id") Long orderId) {
        OrderInfo info = orderService.getOrderInfo(orderId);
        return Result.success(info);
    }
    
    /**
     * 标记订单为已评价
     */
    @PutMapping("/{id}/reviewed")
    public Result<Void> markAsReviewed(@PathVariable("id") Long orderId) {
        boolean success = orderService.updateToReviewed(orderId);
        if (!success) {
            return Result.error(409, "订单状态不允许标记为已评价");
        }
        return Result.success(null);
    }
}
