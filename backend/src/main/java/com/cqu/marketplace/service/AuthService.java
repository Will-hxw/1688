package com.cqu.marketplace.service;

import com.cqu.marketplace.dto.auth.LoginRequest;
import com.cqu.marketplace.dto.auth.RegisterRequest;
import com.cqu.marketplace.vo.auth.LoginVO;

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
