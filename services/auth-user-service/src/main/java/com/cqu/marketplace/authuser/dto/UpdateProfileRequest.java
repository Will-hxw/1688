package com.cqu.marketplace.authuser.dto;

import lombok.Data;

/**
 * 更新个人信息请求 DTO
 */
@Data
public class UpdateProfileRequest {
    
    /** 昵称 */
    private String nickname;
    
    /** 头像URL */
    private String avatar;
}
