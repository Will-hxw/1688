package com.cqu.marketplace.authuser.controller.internal;

import com.cqu.marketplace.authuser.service.UserService;
import com.cqu.marketplace.authuser.vo.UserVO;
import com.cqu.marketplace.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内部用户 API 控制器
 * 仅供其他微服务调用，Gateway 会拦截外部请求
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {
    
    private final UserService userService;
    
    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }
    
    /**
     * 获取用户状态
     */
    @GetMapping("/{id}/status")
    public Result<String> getUserStatus(@PathVariable Long id) {
        String status = userService.getUserStatus(id);
        return Result.success(status);
    }
}
