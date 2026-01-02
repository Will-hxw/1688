package com.cqu.marketplace.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cqu.marketplace.common.enums.CanceledBy;
import com.cqu.marketplace.common.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 * 包含商品快照字段（productName, productImage）避免跨服务查询
 */
@Data
@TableName("`order`")
public class Order {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 买家ID */
    private Long buyerId;
    
    /** 卖家ID */
    private Long sellerId;
    
    /** 商品ID */
    private Long productId;
    
    /** 成交价格（下单时锁定） */
    private BigDecimal price;
    
    /** 商品名称快照 */
    private String productName;
    
    /** 商品图片快照 */
    private String productImage;
    
    /** 订单状态 */
    private OrderStatus status;
    
    /** 幂等键 */
    private String idempotencyKey;
    
    /** 取消方 */
    private CanceledBy canceledBy;
    
    /** 取消时间 */
    private LocalDateTime canceledAt;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
