package com.cqu.marketplace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.product.ProductCreateRequest;
import com.cqu.marketplace.dto.product.ProductSearchRequest;
import com.cqu.marketplace.dto.product.ProductUpdateRequest;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.ProductService;
import com.cqu.marketplace.vo.product.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    
    @Override
    @Transactional
    public Long createProduct(Long sellerId, ProductCreateRequest request) {
        Product product = new Product();
        product.setSellerId(sellerId);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());
        product.setStock(request.getStock());
        product.setStatus(ProductStatus.ON_SALE);
        
        productMapper.insert(product);
        log.info("商品创建成功: productId={}, sellerId={}", product.getId(), sellerId);
        
        return product.getId();
    }
    
    @Override
    @Transactional
    public void updateProduct(Long productId, Long userId, ProductUpdateRequest request) {
        Product product = getProductById(productId);
        
        // 权限校验：只能编辑自己的商品
        if (!product.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("无权编辑他人商品");
        }
        
        // 状态校验：只能编辑ON_SALE状态的商品
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw BusinessException.conflict("商品状态不允许编辑");
        }
        
        // 更新字段
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (StringUtils.hasText(request.getImageUrl())) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        
        productMapper.updateById(product);
        log.info("商品更新成功: productId={}", productId);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long productId, Long userId) {
        Product product = getProductById(productId);
        
        // 权限校验：只能删除自己的商品
        if (!product.getSellerId().equals(userId)) {
            throw BusinessException.forbidden("无权删除他人商品");
        }
        
        // 状态校验：只能删除ON_SALE状态的商品
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw BusinessException.conflict("商品状态不允许删除");
        }
        
        product.setStatus(ProductStatus.DELETED);
        productMapper.updateById(product);
        log.info("商品删除成功: productId={}", productId);
    }
    
    @Override
    public PageResult<ProductVO> searchProducts(ProductSearchRequest request) {
        Page<Product> page = new Page<>(request.getPage(), request.getPageSize());
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 只查询ON_SALE状态且有库存的商品
        wrapper.eq(Product::getStatus, ProductStatus.ON_SALE)
               .gt(Product::getStock, 0);
        
        // 关键词搜索
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                .like(Product::getName, request.getKeyword())
                .or()
                .like(Product::getDescription, request.getKeyword())
            );
        }
        
        // 分类筛选
        if (StringUtils.hasText(request.getCategory())) {
            wrapper.eq(Product::getCategory, request.getCategory());
        }
        
        // 价格区间
        if (request.getMinPrice() != null) {
            wrapper.ge(Product::getPrice, request.getMinPrice());
        }
        if (request.getMaxPrice() != null) {
            wrapper.le(Product::getPrice, request.getMaxPrice());
        }
        
        // 排序
        boolean isAsc = "asc".equalsIgnoreCase(request.getSortOrder());
        if ("price".equals(request.getSortBy())) {
            wrapper.orderBy(true, isAsc, Product::getPrice);
        } else {
            wrapper.orderBy(true, isAsc, Product::getCreatedAt);
        }
        
        Page<Product> result = productMapper.selectPage(page, wrapper);
        
        List<ProductVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(request.getPage(), request.getPageSize(), result.getTotal(), voList);
    }
    
    @Override
    public ProductVO getProductDetail(Long productId) {
        Product product = getProductById(productId);
        return convertToVO(product);
    }
    
    @Override
    public Product getProductById(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw BusinessException.notFound("商品不存在");
        }
        return product;
    }
    
    @Override
    public PageResult<ProductVO> getMyProducts(Long userId, Integer page, Integer pageSize) {
        Page<Product> pageObj = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, userId)
               .ne(Product::getStatus, ProductStatus.DELETED)
               .orderByDesc(Product::getCreatedAt);
        
        Page<Product> result = productMapper.selectPage(pageObj, wrapper);
        
        List<ProductVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public boolean updateStatusAtomic(Long productId, String fromStatus, String toStatus) {
        int rows = productMapper.updateStatusAtomic(productId, fromStatus, toStatus);
        return rows > 0;
    }
    
    @Override
    public boolean decrementStock(Long productId) {
        int rows = productMapper.decrementStock(productId);
        return rows > 0;
    }
    
    @Override
    public boolean incrementStock(Long productId) {
        int rows = productMapper.incrementStock(productId);
        return rows > 0;
    }
    
    /**
     * 转换为VO
     */
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
        
        // 获取卖家昵称
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        
        return vo;
    }
}
