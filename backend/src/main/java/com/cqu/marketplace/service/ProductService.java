package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.dto.product.ProductCreateRequest;
import com.cqu.marketplace.dto.product.ProductSearchRequest;
import com.cqu.marketplace.dto.product.ProductUpdateRequest;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.vo.product.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {
    
    /**
     * 创建商品
     */
    Long createProduct(Long sellerId, ProductCreateRequest request);
    
    /**
     * 更新商品
     */
    void updateProduct(Long productId, Long userId, ProductUpdateRequest request);
    
    /**
     * 删除商品（软删除）
     */
    void deleteProduct(Long productId, Long userId);
    
    /**
     * 搜索商品（仅ON_SALE状态）
     */
    PageResult<ProductVO> searchProducts(ProductSearchRequest request);
    
    /**
     * 获取商品详情
     */
    ProductVO getProductDetail(Long productId);
    
    /**
     * 获取商品实体（内部使用）
     */
    Product getProductById(Long productId);
    
    /**
     * 获取用户的商品列表
     */
    PageResult<ProductVO> getMyProducts(Long userId, Integer page, Integer pageSize);
    
    /**
     * 原子更新商品状态
     */
    boolean updateStatusAtomic(Long productId, String fromStatus, String toStatus);
}
