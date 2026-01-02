package com.cqu.marketplace.authuser.service;

import com.cqu.marketplace.authuser.dto.UpdateProfileRequest;
import com.cqu.marketplace.authuser.vo.UserVO;
import com.cqu.marketplace.common.PageResult;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);
    
    /**
     * 更新个人信息
     */
    void updateProfile(Long userId, UpdateProfileRequest request);
    
    /**
     * 获取用户列表（管理员）
     */
    PageResult<UserVO> listUsers(Integer page, Integer pageSize);
    
    /**
     * 禁用用户（管理员）
     */
    void disableUser(Long userId);
    
    /**
     * 启用用户（管理员）
     */
    void enableUser(Long userId);
    
    /**
     * 根据ID获取用户（内部API）
     */
    UserVO getUserById(Long userId);
    
    /**
     * 获取用户状态（内部API）
     */
    String getUserStatus(Long userId);
}
