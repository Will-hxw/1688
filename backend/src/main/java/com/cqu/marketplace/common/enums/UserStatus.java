package com.cqu.marketplace.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatus {
    
    ACTIVE("ACTIVE", "启用"),
    DISABLED("DISABLED", "禁用");
    
    @EnumValue
    private final String code;
    private final String desc;
    
    UserStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
