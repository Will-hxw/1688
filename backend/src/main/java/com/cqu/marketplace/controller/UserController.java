package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.dto.user.UpdateProfileRequest;
import com.cqu.marketplace.security.UserPrincipal;
import com.cqu.marketplace.service.UserService;
import com.cqu.marketplace.vo.product.ProductVO;
import com.cqu.marketplace.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public Result<UserVO> getCurrentUser(@AuthenticationPrincipal UserPrincipal user) {
        UserVO userVO = userService.getCurrentUser(user.getId());
        return Result.success(userVO);
    }
    
    /**
     * 更新当前用户信息
     */
    @PutMapping("/me")
    public Result<Void> updateProfile(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(user.getId(), request);
        return Result.success();
    }
    
    /**
     * 获取我的商品列表
     */
    @GetMapping("/me/products")
    public Result<PageResult<ProductVO>> getMyProducts(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = userService.getMyProducts(user.getId(), page, pageSize);
        return Result.success(result);
    }
}
