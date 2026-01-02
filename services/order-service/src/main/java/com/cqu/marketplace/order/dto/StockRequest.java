package com.cqu.marketplace.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存操作请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRequest {
    
    /** 订单ID（用于日志追踪） */
    private Long orderId;
}
