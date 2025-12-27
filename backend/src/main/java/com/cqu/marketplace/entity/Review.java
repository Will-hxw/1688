package com.cqu.marketplace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价实体
 */
@Data
@TableName("review")
public class Review {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 订单ID（唯一约束，一个订单只能评价一次） */
    private Long orderId;
    
    /** 商品ID */
    private Long productId;
    
    /** 买家ID（评价者） */
    private Long buyerId;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 评分（1-5） */
    private Integer rating;
    
    /** 评价内容 */
    private String content;
    
    /** 是否删除（软删除） */
    private Boolean deleted;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
