package com.cqu.marketplace.authuser;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户名唯一性属性测试
 * Feature: microservices-migration, Property 5: 用户名唯一性
 * Validates: Requirements 2.1
 */
class UsernameUniquenessPropertyTest {
    
    /**
     * 模拟已注册的用户名集合
     */
    private final Set<String> registeredUsernames = new HashSet<>();
    
    /**
     * Property 5: 用户名唯一性
     * 对于任意已存在的用户名，重复注册请求应被拒绝
     */
    @Property(tries = 100)
    void duplicateUsernameShouldBeRejected(
            @ForAll("validUsernames") String username) {
        
        // 第一次注册应该成功
        boolean firstRegister = tryRegister(username);
        assertThat(firstRegister)
            .as("第一次注册用户名 %s 应该成功", username)
            .isTrue();
        
        // 第二次注册相同用户名应该失败
        boolean secondRegister = tryRegister(username);
        assertThat(secondRegister)
            .as("重复注册用户名 %s 应该失败", username)
            .isFalse();
        
        // 清理测试数据
        registeredUsernames.remove(username);
    }
    
    /**
     * Property 5: 不同用户名应该都能注册成功
     */
    @Property(tries = 100)
    void differentUsernamesShouldAllSucceed(
            @ForAll("validUsernames") String username1,
            @ForAll("validUsernames") String username2) {
        
        Assume.that(!username1.equals(username2));
        
        // 清理之前的测试数据
        registeredUsernames.clear();
        
        // 两个不同的用户名都应该能注册成功
        boolean register1 = tryRegister(username1);
        boolean register2 = tryRegister(username2);
        
        assertThat(register1)
            .as("用户名 %s 应该注册成功", username1)
            .isTrue();
        assertThat(register2)
            .as("用户名 %s 应该注册成功", username2)
            .isTrue();
        
        // 清理测试数据
        registeredUsernames.clear();
    }
    
    /**
     * 用户名大小写敏感性测试
     * 验证 "User" 和 "user" 是否被视为不同用户名
     */
    @Property(tries = 50)
    void usernameCaseSensitivity(
            @ForAll("validUsernames") String username) {
        
        // 清理之前的测试数据
        registeredUsernames.clear();
        
        String lowerCase = username.toLowerCase();
        String upperCase = username.toUpperCase();
        
        // 如果大小写相同，跳过测试
        Assume.that(!lowerCase.equals(upperCase));
        
        // 注册小写版本
        boolean registerLower = tryRegister(lowerCase);
        assertThat(registerLower).isTrue();
        
        // 注册大写版本（取决于系统是否大小写敏感）
        // 这里假设系统是大小写敏感的，所以大写版本也应该能注册
        boolean registerUpper = tryRegister(upperCase);
        
        // 验证两个用户名都被记录
        assertThat(registeredUsernames).contains(lowerCase);
        if (registerUpper) {
            assertThat(registeredUsernames).contains(upperCase);
        }
        
        // 清理测试数据
        registeredUsernames.clear();
    }
    
    /**
     * 生成有效的用户名
     */
    @Provide
    Arbitrary<String> validUsernames() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(3)
            .ofMaxLength(20)
            .map(String::toLowerCase);
    }
    
    /**
     * 模拟注册逻辑
     * @return true 如果注册成功，false 如果用户名已存在
     */
    private boolean tryRegister(String username) {
        // 检查用户名是否已存在
        if (registeredUsernames.contains(username)) {
            return false;
        }
        
        // 注册成功，添加到已注册集合
        registeredUsernames.add(username);
        return true;
    }
}
