package com.cqu.marketplace.vo.order;

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
    
    /** 买家昵称 */
    private String buyerNickname;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 卖家昵称 */
    private String sellerNickname;
    
    /** 商品ID */
    private Long productId;
    
    /** 商品名称 */
    private String productName;
    
    /** 商品图片 */
    private String productImage;
    
    /** 成交价格 */
    private BigDecimal price;
    
    /** 订单状态 */
    private String status;
    
    /** 取消方 */
    private String canceledBy;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
