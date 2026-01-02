package com.cqu.marketplace.product.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.product.dto.ProductCreateRequest;
import com.cqu.marketplace.product.dto.ProductSearchRequest;
import com.cqu.marketplace.product.dto.ProductUpdateRequest;
import com.cqu.marketplace.product.entity.Product;
import com.cqu.marketplace.product.vo.ProductVO;

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
     * 原子扣减库存（下单时调用）
     * @return true表示扣减成功，false表示库存不足
     */
    boolean decrementStock(Long productId);
    
    /**
     * 原子增加库存（取消订单时调用）
     * @return true表示增加成功
     */
    boolean incrementStock(Long productId);
}
