package com.cqu.marketplace.review.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价视图对象
 */
@Data
public class ReviewVO {
    
    /** 评价ID */
    private Long id;
    
    /** 订单ID */
    private Long orderId;
    
    /** 商品ID */
    private Long productId;
    
    /** 商品名称（微服务架构下不跨服务查询，可选填充） */
    private String productName;
    
    /** 买家ID */
    private Long buyerId;
    
    /** 买家昵称（微服务架构下不跨服务查询，可选填充） */
    private String buyerNickname;
    
    /** 评分 */
    private Integer rating;
    
    /** 评价内容 */
    private String content;
    
    /** 是否删除 */
    private Boolean deleted;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
