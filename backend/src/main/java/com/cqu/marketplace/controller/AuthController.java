package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.dto.auth.LoginRequest;
import com.cqu.marketplace.dto.auth.RegisterRequest;
import com.cqu.marketplace.service.AuthService;
import com.cqu.marketplace.vo.auth.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理", description = "用户注册、登录接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return Result.success("注册成功", userId);
    }
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        LoginVO loginVO = authService.login(request);
        return Result.success("登录成功", loginVO);
    }
}
