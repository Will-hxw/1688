package com.cqu.marketplace.order.vo;

import lombok.Data;

/**
 * 订单信息VO（供内部API使用）
 */
@Data
public class OrderInfo {
    
    private Long id;
    
    /** 买家ID */
    private Long buyerId;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 商品ID */
    private Long productId;
    
    /** 订单状态 */
    private String status;
}
