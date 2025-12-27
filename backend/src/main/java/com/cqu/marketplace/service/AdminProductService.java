package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.dto.product.ProductUpdateRequest;
import com.cqu.marketplace.vo.product.ProductVO;

/**
 * 管理员商品服务接口
 */
public interface AdminProductService {
    
    /**
     * 获取商品列表（含已删除）
     */
    PageResult<ProductVO> listProducts(Integer page, Integer pageSize);
    
    /**
     * 编辑商品
     */
    void updateProduct(Long productId, ProductUpdateRequest request);
    
    /**
     * 删除商品（仅ON_SALE可删除）
     */
    void deleteProduct(Long productId);
}
