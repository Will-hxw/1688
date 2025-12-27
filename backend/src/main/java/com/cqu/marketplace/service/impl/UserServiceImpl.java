package com.cqu.marketplace.service.impl;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.user.UpdateProfileRequest;
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
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 更新昵称
        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            user.setNickname(request.getNickname().trim());
        }
        
        // 更新头像
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar().trim());
        }
        
        userMapper.updateById(user);
        log.info("用户信息已更新: userId={}", userId);
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
        vo.setRole(user.getRole().getCode());
        vo.setStatus(user.getStatus().getCode());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
