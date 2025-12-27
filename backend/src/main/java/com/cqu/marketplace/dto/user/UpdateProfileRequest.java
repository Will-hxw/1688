package com.cqu.marketplace.dto.user;

import lombok.Data;

/**
 * 更新个人信息请求
 */
@Data
public class UpdateProfileRequest {
    
    /** 昵称 */
    private String nickname;
    
    /** 头像URL */
    private String avatar;
}
