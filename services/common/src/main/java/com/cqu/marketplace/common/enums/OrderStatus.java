package com.cqu.marketplace.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
public enum OrderStatus {
    
    CREATED("CREATED", "待处理"),
    SHIPPED("SHIPPED", "已发货"),
    RECEIVED("RECEIVED", "已收货"),
    REVIEWED("REVIEWED", "已评价"),
    CANCELED("CANCELED", "已取消");
    
    @EnumValue
    private final String code;
    private final String desc;
    
    OrderStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    /**
     * 检查是否可以转换到目标状态
     */
    public boolean canTransitionTo(OrderStatus target) {
        return switch (this) {
            case CREATED -> target == SHIPPED || target == CANCELED;
            case SHIPPED -> target == RECEIVED;
            case RECEIVED -> target == REVIEWED;
            case REVIEWED, CANCELED -> false;
        };
    }
}
