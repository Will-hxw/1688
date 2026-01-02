package com.cqu.marketplace.authuser.controller;

import com.cqu.marketplace.authuser.service.UserService;
import com.cqu.marketplace.authuser.vo.UserVO;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员用户控制器
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    
    private final UserService userService;
    
    /**
     * 获取用户列表
     */
    @GetMapping
    public Result<PageResult<UserVO>> listUsers(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        checkAdminRole(role);
        PageResult<UserVO> result = userService.listUsers(page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 禁用用户
     */
    @PutMapping("/{id}/disable")
    public Result<Void> disableUser(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        checkAdminRole(role);
        userService.disableUser(id);
        return Result.success();
    }
    
    /**
     * 启用用户
     */
    @PutMapping("/{id}/enable")
    public Result<Void> enableUser(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        checkAdminRole(role);
        userService.enableUser(id);
        return Result.success();
    }
    
    /**
     * 更新用户状态（兼容原接口）
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @RequestParam String status) {
        checkAdminRole(role);
        if ("DISABLED".equalsIgnoreCase(status)) {
            userService.disableUser(id);
        } else if ("ACTIVE".equalsIgnoreCase(status)) {
            userService.enableUser(id);
        } else {
            throw new BusinessException(400, "无效的状态值");
        }
        return Result.success();
    }
    
    /**
     * 检查管理员权限
     */
    private void checkAdminRole(String role) {
        if (!"ADMIN".equals(role)) {
            throw BusinessException.forbidden("需要管理员权限");
        }
    }
}
