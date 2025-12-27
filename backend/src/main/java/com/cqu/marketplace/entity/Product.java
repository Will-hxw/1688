package com.cqu.marketplace.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cqu.marketplace.common.enums.ProductStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 */
@Data
@TableName("product")
public class Product {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 卖家ID */
    private Long sellerId;
    
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
    
    /** 状态 */
    private ProductStatus status;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
