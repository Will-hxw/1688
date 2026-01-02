package com.cqu.marketplace.authuser.controller;

import com.cqu.marketplace.authuser.dto.LoginRequest;
import com.cqu.marketplace.authuser.dto.RegisterRequest;
import com.cqu.marketplace.authuser.service.AuthService;
import com.cqu.marketplace.authuser.vo.LoginVO;
import com.cqu.marketplace.common.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return Result.success("注册成功", userId);
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = authService.login(request);
        return Result.success("登录成功", loginVO);
    }
}
