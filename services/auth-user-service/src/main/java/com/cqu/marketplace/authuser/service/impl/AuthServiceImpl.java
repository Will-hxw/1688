package com.cqu.marketplace.authuser.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cqu.marketplace.authuser.dto.LoginRequest;
import com.cqu.marketplace.authuser.dto.RegisterRequest;
import com.cqu.marketplace.authuser.entity.User;
import com.cqu.marketplace.authuser.mapper.UserMapper;
import com.cqu.marketplace.authuser.security.JwtTokenProvider;
import com.cqu.marketplace.authuser.service.AuthService;
import com.cqu.marketplace.authuser.vo.LoginVO;
import com.cqu.marketplace.common.enums.Role;
import com.cqu.marketplace.common.enums.UserStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw BusinessException.conflict("用户名已存在");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole(Role.USER);
        user.setStatus(UserStatus.ACTIVE);
        
        userMapper.insert(user);
        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());
        
        return user.getId();
    }
    
    @Override
    public LoginVO login(LoginRequest request) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        // 验证用户存在
        if (user == null) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        
        // 验证用户状态
        if (user.getStatus() == UserStatus.DISABLED) {
            throw BusinessException.forbidden("账户已被禁用");
        }
        
        // 生成JWT令牌
        String token = jwtTokenProvider.generateToken(
            user.getId(), 
            user.getUsername(), 
            user.getRole().getCode()
        );
        
        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());
        
        return new LoginVO(
            token,
            user.getId(),
            user.getUsername(),
            user.getNickname(),
            user.getRole().getCode()
        );
    }
}
