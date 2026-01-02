package com.cqu.marketplace.product.client;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.product.vo.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Auth-User Service Feign 客户端
 * 用于获取用户信息（如卖家昵称）
 */
@FeignClient(name = "auth-user-service", url = "${services.auth-user.url}")
public interface UserClient {
    
    /**
     * 获取用户信息
     */
    @GetMapping("/internal/users/{id}")
    Result<UserInfo> getUserById(@PathVariable("id") Long userId);
}
