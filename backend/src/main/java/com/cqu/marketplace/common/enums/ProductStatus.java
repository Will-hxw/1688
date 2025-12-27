package com.cqu.marketplace.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 商品状态枚举
 */
@Getter
public enum ProductStatus {
    
    ON_SALE("ON_SALE", "上架中"),
    SOLD("SOLD", "已被锁定"),
    DELETED("DELETED", "已删除");
    
    @EnumValue
    private final String code;
    private final String desc;
    
    ProductStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
