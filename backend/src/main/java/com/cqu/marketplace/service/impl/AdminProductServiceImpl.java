package com.cqu.marketplace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.product.ProductUpdateRequest;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.AdminProductService;
import com.cqu.marketplace.vo.product.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductServiceImpl implements AdminProductService {
    
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    
    @Override
    public PageResult<ProductVO> listProducts(Integer page, Integer pageSize) {
        Page<Product> pageParam = new Page<>(page, pageSize);
        // 管理员可以看到所有商品，包括已删除的
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Product::getCreatedAt);
        
        Page<Product> result = productMapper.selectPage(pageParam, wrapper);
        
        List<ProductVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public void updateProduct(Long productId, ProductUpdateRequest request) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 管理员可以编辑任何状态的商品
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
    }
    
    @Override
    public void deleteProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 仅ON_SALE状态可删除
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(409, "仅在售商品可删除");
        }
        
        // 通过MyBatis-Plus逻辑删除
        productMapper.deleteById(productId);
        
        log.info("管理员删除商品成功: productId={}", productId);
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
        vo.setStatus(product.getStatus().name());
        vo.setCreatedAt(product.getCreatedAt());
        
        // 获取卖家昵称
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        
        return vo;
    }
}
