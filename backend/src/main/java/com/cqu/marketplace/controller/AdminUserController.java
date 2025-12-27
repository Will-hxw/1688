package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.service.AdminUserService;
import com.cqu.marketplace.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户控制器
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final AdminUserService adminUserService;
    
    /**
     * 获取用户列表
     */
    @GetMapping
    public Result<PageResult<UserVO>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<UserVO> result = adminUserService.listUsers(page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 禁用用户
     */
    @PutMapping("/{id}/disable")
    public Result<Void> disableUser(@PathVariable("id") Long userId) {
        adminUserService.disableUser(userId);
        return Result.success(null);
    }
    
    /**
     * 启用用户
     */
    @PutMapping("/{id}/enable")
    public Result<Void> enableUser(@PathVariable("id") Long userId) {
        adminUserService.enableUser(userId);
        return Result.success(null);
    }
}
