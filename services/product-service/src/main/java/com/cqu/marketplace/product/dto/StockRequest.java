package com.cqu.marketplace.product.dto;

import lombok.Data;

/**
 * 库存操作请求DTO
 */
@Data
public class StockRequest {
    
    /** 订单ID（用于日志追踪） */
    private String orderId;
}
