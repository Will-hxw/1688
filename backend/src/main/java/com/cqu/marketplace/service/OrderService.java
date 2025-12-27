package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.dto.order.OrderCreateRequest;
import com.cqu.marketplace.entity.Order;
import com.cqu.marketplace.vo.order.OrderVO;

/**
 * 订单服务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     * 幂等操作：相同buyerId+idempotencyKey返回相同订单
     * 
     * @param buyerId 买家ID
     * @param request 创建请求
     * @param idempotencyKey 幂等键
     * @return 订单ID
     */
    Long createOrder(Long buyerId, OrderCreateRequest request, String idempotencyKey);
    
    /**
     * 发货
     * 仅卖家可操作，状态CREATED→SHIPPED
     * 
     * @param orderId 订单ID
     * @param sellerId 卖家ID
     */
    void shipOrder(Long orderId, Long sellerId);
    
    /**
     * 确认收货
     * 仅买家可操作，状态SHIPPED→RECEIVED
     * 
     * @param orderId 订单ID
     * @param buyerId 买家ID
     */
    void receiveOrder(Long orderId, Long buyerId);
    
    /**
     * 取消订单
     * 买家或卖家可操作，仅CREATED状态可取消
     * 取消后商品恢复ON_SALE
     * 
     * @param orderId 订单ID
     * @param userId 操作用户ID
     */
    void cancelOrder(Long orderId, Long userId);
    
    /**
     * 获取买家订单列表
     * 
     * @param buyerId 买家ID
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    PageResult<OrderVO> getBuyerOrders(Long buyerId, String status, Integer page, Integer pageSize);
    
    /**
     * 获取卖家订单列表
     * 
     * @param sellerId 卖家ID
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 订单列表
     */
    PageResult<OrderVO> getSellerOrders(Long sellerId, String status, Integer page, Integer pageSize);
    
    /**
     * 根据ID获取订单
     * 
     * @param orderId 订单ID
     * @return 订单实体
     */
    Order getOrderById(Long orderId);
    
    /**
     * 更新订单状态为已评价
     * 
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean updateToReviewed(Long orderId);
}
