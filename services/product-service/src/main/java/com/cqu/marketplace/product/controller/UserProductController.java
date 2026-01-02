package com.cqu.marketplace.product.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.product.service.ProductService;
import com.cqu.marketplace.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户商品控制器
 * 处理 /users/me/products 路径
 */
@RestController
@RequestMapping("/users/me/products")
@RequiredArgsConstructor
public class UserProductController {
    
    private final ProductService productService;
    
    /**
     * 获取我的商品列表
     */
    @GetMapping
    public Result<PageResult<ProductVO>> getMyProducts(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = productService.getMyProducts(userId, page, pageSize);
        return Result.success(result);
    }
}
