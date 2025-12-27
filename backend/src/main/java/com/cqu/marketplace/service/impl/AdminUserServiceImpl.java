package com.cqu.marketplace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.UserStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.AdminUserService;
import com.cqu.marketplace.vo.user.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {
    
    private final UserMapper userMapper;
    
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
