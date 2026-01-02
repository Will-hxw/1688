package com.cqu.marketplace.authuser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.authuser.dto.UpdateProfileRequest;
import com.cqu.marketplace.authuser.entity.User;
import com.cqu.marketplace.authuser.mapper.UserMapper;
import com.cqu.marketplace.authuser.service.UserService;
import com.cqu.marketplace.authuser.vo.UserVO;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.UserStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    
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
    public PageResult<UserVO> listUsers(Integer page, Integer pageSize) {
        Page<User> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreatedAt);
        
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        
        List<UserVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public void disableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException(409, "用户已被禁用");
        }
        
        user.setStatus(UserStatus.DISABLED);
        userMapper.updateById(user);
        
        log.info("用户已禁用: userId={}", userId);
    }
    
    @Override
    public void enableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new BusinessException(409, "用户已是正常状态");
        }
        
        user.setStatus(UserStatus.ACTIVE);
        userMapper.updateById(user);
        
        log.info("用户已启用: userId={}", userId);
    }
    
    @Override
    public UserVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return convertToVO(user);
    }
    
    @Override
    public String getUserStatus(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user.getStatus().getCode();
    }
    
    /**
     * 转换为 VO
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
