package com.cqu.marketplace.product.vo;

import lombok.Data;

/**
 * 用户信息 VO（从 Auth-User Service 获取）
 */
@Data
public class UserInfo {
    
    /** 用户ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 昵称 */
    private String nickname;
    
    /** 角色 */
    private String role;
    
    /** 状态 */
    private String status;
}
