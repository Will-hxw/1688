package com.cqu.marketplace.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单视图对象
 */
@Data
public class OrderVO {
    
    /** 订单ID */
    private Long id;
    
    /** 买家ID */
    private Long buyerId;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 商品ID */
    private Long productId;
    
    /** 商品名称（快照） */
    private String productName;
    
    /** 商品图片（快照） */
    private String productImage;
    
    /** 成交价格 */
    private BigDecimal price;
    
    /** 订单状态 */
    private String status;
    
    /** 取消方 */
    private String canceledBy;
    
    /** 取消时间 */
    private LocalDateTime canceledAt;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
