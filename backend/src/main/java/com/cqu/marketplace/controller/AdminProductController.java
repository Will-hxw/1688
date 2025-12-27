package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.dto.product.ProductUpdateRequest;
import com.cqu.marketplace.service.AdminProductService;
import com.cqu.marketplace.vo.product.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员商品控制器
 */
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final AdminProductService adminProductService;
    
    /**
     * 获取商品列表（含已删除）
     */
    @GetMapping
    public Result<PageResult<ProductVO>> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<ProductVO> result = adminProductService.listProducts(page, pageSize);
        return Result.success(result);
    }
    
    /**
     * 编辑商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(
            @PathVariable("id") Long productId,
            @RequestBody ProductUpdateRequest request) {
        adminProductService.updateProduct(productId, request);
        return Result.success(null);
    }
    
    /**
     * 删除商品（仅ON_SALE可删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable("id") Long productId) {
        adminProductService.deleteProduct(productId);
        return Result.success(null);
    }
}
