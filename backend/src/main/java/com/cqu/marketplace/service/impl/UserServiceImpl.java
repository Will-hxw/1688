package com.cqu.marketplace.service.impl;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.ProductService;
import com.cqu.marketplace.service.UserService;
import com.cqu.marketplace.vo.product.ProductVO;
import com.cqu.marketplace.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final ProductService productService;
    
    @Override
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        return convertToVO(user);
    }
    
    @Override
    public PageResult<ProductVO> getMyProducts(Long userId, Integer page, Integer pageSize) {
        return productService.getMyProducts(userId, page, pageSize);
    }
    
    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
