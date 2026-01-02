package com.cqu.marketplace.review.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品信息（从 Product Service 获取）
 */
@Data
public class ProductInfo {
    
    /** 商品ID */
    private Long id;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 商品名称 */
    private String name;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 图片URL */
    private String imageUrl;
    
    /** 库存 */
    private Integer stock;
    
    /** 状态 */
    private String status;
}
