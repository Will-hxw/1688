package com.cqu.marketplace.service;

import com.cqu.marketplace.common.enums.Role;
import com.cqu.marketplace.common.enums.UserStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.auth.LoginRequest;
import com.cqu.marketplace.dto.auth.RegisterRequest;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.security.JwtTokenProvider;
import com.cqu.marketplace.service.impl.AuthServiceImpl;
import com.cqu.marketplace.vo.auth.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 认证服务单元测试
 * 验证: 需求 1.1-1.5
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("测试用户");
        
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("测试用户");
        testUser.setRole(Role.USER);
        testUser.setStatus(UserStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("注册成功 - 验证需求1.1")
    void register_Success() {
        // 准备
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userMapper.insert(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return 1;
        });
        
        // 执行
        Long userId = authService.register(registerRequest);
        
        // 验证
        assertNotNull(userId);
        assertEquals(1L, userId);
        verify(userMapper).insert(any(User.class));
    }
    
    @Test
    @DisplayName("注册失败 - 用户名已存在 - 验证需求1.2")
    void register_UsernameExists_ThrowsConflict() {
        // 准备
        when(userMapper.selectCount(any())).thenReturn(1L);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.register(registerRequest));
        assertEquals(409, exception.getCode());
        assertEquals("用户名已存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("登录成功 - 验证需求1.3")
    void login_Success() {
        // 准备
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("jwt-token");
        
        // 执行
        LoginVO result = authService.login(loginRequest);
        
        // 验证
        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("USER", result.getRole());
    }
    
    @Test
    @DisplayName("登录失败 - 用户名错误 - 验证需求1.4")
    void login_WrongUsername_ThrowsUnauthorized() {
        // 准备
        when(userMapper.selectOne(any())).thenReturn(null);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(loginRequest));
        assertEquals(401, exception.getCode());
    }
    
    @Test
    @DisplayName("登录失败 - 密码错误 - 验证需求1.4")
    void login_WrongPassword_ThrowsUnauthorized() {
        // 准备
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(loginRequest));
        assertEquals(401, exception.getCode());
    }
    
    @Test
    @DisplayName("登录失败 - 账户已禁用 - 验证需求1.5")
    void login_DisabledUser_ThrowsForbidden() {
        // 准备
        testUser.setStatus(UserStatus.DISABLED);
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.login(loginRequest));
        assertEquals(403, exception.getCode());
        assertEquals("账户已被禁用", exception.getMessage());
    }
}
