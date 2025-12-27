package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.vo.user.UserVO;

/**
 * 管理员用户服务接口
 */
public interface AdminUserService {
    
    /**
     * 获取用户列表
     */
    PageResult<UserVO> listUsers(Integer page, Integer pageSize);
    
    /**
     * 禁用用户
     */
    void disableUser(Long userId);
    
    /**
     * 启用用户
     */
    void enableUser(Long userId);
}
