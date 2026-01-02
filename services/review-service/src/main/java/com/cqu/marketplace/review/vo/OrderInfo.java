package com.cqu.marketplace.review.vo;

import lombok.Data;

/**
 * 订单信息VO（从Order Service获取）
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
