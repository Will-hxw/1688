package com.cqu.marketplace.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单创建请求DTO
 */
@Data
public class OrderCreateRequest {
    
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}
