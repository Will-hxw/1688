package com.cqu.marketplace.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 订单取消者枚举
 */
@Getter
public enum CanceledBy {
    
    BUYER("BUYER", "买家"),
    SELLER("SELLER", "卖家");
    
    @EnumValue
    private final String code;
    private final String desc;
    
    CanceledBy(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
