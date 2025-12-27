package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.vo.product.ProductVO;
import com.cqu.marketplace.vo.user.UserVO;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取当前用户信息
     */
    UserVO getCurrentUser(Long userId);
    
    /**
     * 获取我的商品列表
     */
    PageResult<ProductVO> getMyProducts(Long userId, Integer page, Integer pageSize);
}
