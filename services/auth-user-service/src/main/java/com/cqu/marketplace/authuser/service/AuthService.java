package com.cqu.marketplace.authuser.service;

import com.cqu.marketplace.authuser.dto.LoginRequest;
import com.cqu.marketplace.authuser.dto.RegisterRequest;
import com.cqu.marketplace.authuser.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 用户注册
     * @param request 注册请求
     * @return 用户ID
     */
    Long register(RegisterRequest request);
    
    /**
     * 用户登录
     * @param request 登录请求
     * @return 登录响应（含JWT令牌）
     */
    LoginVO login(LoginRequest request);
}
