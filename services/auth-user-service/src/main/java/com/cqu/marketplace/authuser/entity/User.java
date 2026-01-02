package com.cqu.marketplace.authuser.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.cqu.marketplace.common.enums.Role;
import com.cqu.marketplace.common.enums.UserStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 用户名 */
    private String username;
    
    /** 密码（加密） */
    private String password;
    
    /** 昵称 */
    private String nickname;
    
    /** 头像URL */
    private String avatar;
    
    /** 角色 */
    private Role role;
    
    /** 状态 */
    private UserStatus status;
    
    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
