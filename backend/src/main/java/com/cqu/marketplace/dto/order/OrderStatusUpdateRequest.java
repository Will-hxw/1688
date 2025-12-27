package com.cqu.marketplace.dto.order;

import com.cqu.marketplace.common.enums.OrderStatus;
import lombok.Data;

/**
 * 订单状态更新请求
 */
@Data
public class OrderStatusUpdateRequest {
    
    /** 新状态 */
    private OrderStatus status;
}
