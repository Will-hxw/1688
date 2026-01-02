package com.cqu.marketplace.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.product.dto.ProductUpdateRequest;
import com.cqu.marketplace.product.entity.Product;
import com.cqu.marketplace.product.mapper.ProductMapper;
import com.cqu.marketplace.product.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员商品控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    
    private final ProductMapper productMapper;
    
    /**
     * 获取商品列表（含已删除）
     */
    @GetMapping
    public Result<PageResult<ProductVO>> listProducts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Product> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreatedAt);
        
        Page<Product> result = productMapper.selectPage(pageParam, wrapper);
        
        List<ProductVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return Result.success(PageResult.of(page, pageSize, result.getTotal(), voList));
    }
    
    /**
     * 编辑商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(
            @PathVariable("id") Long productId,
            @RequestBody ProductUpdateRequest request) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        
        productMapper.updateById(product);
        log.info("管理员更新商品成功: productId={}", productId);
        return Result.success(null);
    }
    
    /**
     * 删除商品（仅ON_SALE可删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable("id") Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
        
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw BusinessException.conflict("仅在售商品可删除");
        }
        
        product.setStatus(ProductStatus.DELETED);
        productMapper.updateById(product);
        log.info("管理员删除商品成功: productId={}", productId);
        return Result.success(null);
    }
    
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setSellerId(product.getSellerId());
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setImageUrl(product.getImageUrl());
        vo.setCategory(product.getCategory());
        vo.setStock(product.getStock());
        vo.setStatus(product.getStatus().getCode());
        vo.setCreatedAt(product.getCreatedAt());
        return vo;
    }
}
