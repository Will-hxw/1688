package com.cqu.marketplace.product.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.product.dto.ProductCreateRequest;
import com.cqu.marketplace.product.dto.ProductSearchRequest;
import com.cqu.marketplace.product.dto.ProductUpdateRequest;
import com.cqu.marketplace.product.service.ProductService;
import com.cqu.marketplace.product.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 * 从请求头获取用户信息（由 Gateway 注入 X-User-Id）
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    /**
     * 创建商品
     */
    @PostMapping
    public Result<Long> createProduct(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ProductCreateRequest request) {
        Long productId = productService.createProduct(userId, request);
        return Result.success("商品创建成功", productId);
    }
    
    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ProductUpdateRequest request) {
        productService.updateProduct(id, userId, request);
        return Result.success("商品更新成功", null);
    }
    
    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        productService.deleteProduct(id, userId);
        return Result.success("商品删除成功", null);
    }
    
    /**
     * 搜索商品（公开接口）
     */
    @GetMapping
    public Result<PageResult<ProductVO>> searchProducts(@Valid ProductSearchRequest request) {
        PageResult<ProductVO> result = productService.searchProducts(request);
        return Result.success(result);
    }
    
    /**
     * 获取商品详情（公开接口）
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        ProductVO product = productService.getProductDetail(id);
        return Result.success(product);
    }
}
