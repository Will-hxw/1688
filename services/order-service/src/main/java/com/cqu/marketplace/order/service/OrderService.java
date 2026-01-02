package com.cqu.marketplace.order.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.order.dto.OrderCreateRequest;
import com.cqu.marketplace.order.entity.Order;
import com.cqu.marketplace.order.vo.OrderInfo;
import com.cqu.marketplace.order.vo.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单（幂等）
     * @param buyerId 买家ID
     * @param request 创建请求
     * @param idempotencyKey 幂等键
     * @return 订单ID
     */
    Long createOrder(Long buyerId, OrderCreateRequest request, String idempotencyKey);
    
    /**
     * 发货（卖家）
     * @param orderId 订单ID
     * @param sellerId 卖家ID
     */
    void shipOrder(Long orderId, Long sellerId);
    
    /**
     * 确认收货（买家）
     * @param orderId 订单ID
     * @param buyerId 买家ID
     */
    void receiveOrder(Long orderId, Long buyerId);
    
    /**
     * 取消订单（买家/卖家）
     * @param orderId 订单ID
     * @param userId 用户ID
     */
    void cancelOrder(Long orderId, Long userId);
    
    /**
     * 获取买家订单列表
     */
    PageResult<OrderVO> getBuyerOrders(Long buyerId, String status, Integer page, Integer pageSize);
    
    /**
     * 获取卖家订单列表
     */
    PageResult<OrderVO> getSellerOrders(Long sellerId, String status, Integer page, Integer pageSize);
    
    /**
     * 根据ID获取订单
     */
    Order getOrderById(Long orderId);
    
    /**
     * 获取订单信息（内部API）
     */
    OrderInfo getOrderInfo(Long orderId);
    
    /**
     * 标记订单为已评价
     * @return 是否成功
     */
    boolean updateToReviewed(Long orderId);
}
