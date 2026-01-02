package com.cqu.marketplace.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品快照VO（从 Product Service 获取）
 */
@Data
public class ProductSnapshot {
    
    private Long id;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 商品名称 */
    private String name;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 图片URL */
    private String imageUrl;
    
    /** 库存数量 */
    private Integer stock;
    
    /** 状态 */
    private String status;
}
