package com.cqu.marketplace.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户角色枚举
 */
@Getter
public enum Role {
    
    USER("USER", "普通用户"),
    ADMIN("ADMIN", "管理员");
    
    @EnumValue
    private final String code;
    private final String desc;
    
    Role(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
