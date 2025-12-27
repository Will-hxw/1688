package com.cqu.marketplace.vo.user;

import com.cqu.marketplace.common.enums.Role;
import com.cqu.marketplace.common.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO
 */
@Data
public class UserVO {
    
    /** 用户ID */
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 昵称 */
    private String nickname;
    
    /** 头像URL */
    private String avatar;
    
    /** 角色 */
    private Role role;
    
    /** 状态 */
    private UserStatus status;
    
    /** 创建时间 */
    private LocalDateTime createdAt;
}
