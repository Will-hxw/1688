package com.cqu.marketplace.review.client;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.review.vo.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Order Service Feign 客户端
 * 使用占位符支持环境变量覆盖
 */
@FeignClient(name = "order-service", url = "${services.order.url}")
public interface OrderClient {
    
    /**
     * 获取订单信息
     */
    @GetMapping("/internal/orders/{id}")
    Result<OrderInfo> getOrderInfo(@PathVariable("id") Long orderId);
    
    /**
     * 标记订单为已评价
     */
    @PutMapping("/internal/orders/{id}/reviewed")
    Result<Void> markAsReviewed(@PathVariable("id") Long orderId);
}
