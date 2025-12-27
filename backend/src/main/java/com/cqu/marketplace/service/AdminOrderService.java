package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.vo.order.OrderVO;

/**
 * 管理员订单服务接口
 */
public interface AdminOrderService {
    
    /**
     * 获取订单列表
     */
    PageResult<OrderVO> listOrders(Integer page, Integer pageSize);
    
    /**
     * 修改订单状态（遵循状态机）
     */
    void updateOrderStatus(Long orderId, OrderStatus newStatus);
}
