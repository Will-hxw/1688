package com.cqu.marketplace.controller;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.dto.product.ProductCreateRequest;
import com.cqu.marketplace.dto.product.ProductSearchRequest;
import com.cqu.marketplace.dto.product.ProductUpdateRequest;
import com.cqu.marketplace.security.CurrentUser;
import com.cqu.marketplace.security.UserPrincipal;
import com.cqu.marketplace.service.ProductService;
import com.cqu.marketplace.vo.product.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器
 */
@Tag(name = "商品管理", description = "商品CRUD和搜索接口")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @Operation(summary = "创建商品")
    @PostMapping
    public Result<Long> createProduct(@CurrentUser UserPrincipal user,
                                      @Valid @RequestBody ProductCreateRequest request) {
        Long productId = productService.createProduct(user.getId(), request);
        return Result.success("商品创建成功", productId);
    }
    
    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Long id,
                                      @CurrentUser UserPrincipal user,
                                      @Valid @RequestBody ProductUpdateRequest request) {
        productService.updateProduct(id, user.getId(), request);
        return Result.success("商品更新成功", null);
    }
    
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id,
                                      @CurrentUser UserPrincipal user) {
        productService.deleteProduct(id, user.getId());
        return Result.success("商品删除成功", null);
    }
    
    @Operation(summary = "搜索商品（公开接口）")
    @GetMapping
    public Result<PageResult<ProductVO>> searchProducts(@Valid ProductSearchRequest request) {
        PageResult<ProductVO> result = productService.searchProducts(request);
        return Result.success(result);
    }
    
    @Operation(summary = "获取商品详情（公开接口）")
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable Long id) {
        ProductVO product = productService.getProductDetail(id);
        return Result.success(product);
    }
}
