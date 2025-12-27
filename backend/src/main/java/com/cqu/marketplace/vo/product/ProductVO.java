package com.cqu.marketplace.vo.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品响应VO
 */
@Data
public class ProductVO {
    
    private Long id;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 卖家昵称 */
    private String sellerNickname;
    
    /** 商品名称 */
    private String name;
    
    /** 商品描述 */
    private String description;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 图片URL */
    private String imageUrl;
    
    /** 分类 */
    private String category;
    
    /** 库存数量 */
    private Integer stock;
    
    /** 状态 */
    private String status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
