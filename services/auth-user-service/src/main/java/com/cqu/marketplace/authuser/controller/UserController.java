package com.cqu.marketplace.authuser.controller;

import com.cqu.marketplace.authuser.dto.UpdateProfileRequest;
import com.cqu.marketplace.authuser.service.UserService;
import com.cqu.marketplace.authuser.vo.UserVO;
import com.cqu.marketplace.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        UserVO userVO = userService.getCurrentUser(userId);
        return Result.success(userVO);
    }
    
    /**
     * 更新当前用户信息
     */
    @PutMapping("/me")
    public Result<Void> updateProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(userId, request);
        return Result.success();
    }
}
