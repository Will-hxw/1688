package com.cqu.marketplace.review.client;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.review.vo.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Auth-User Service Feign 客户端
 */
@FeignClient(name = "auth-user-service", url = "${services.auth-user.url}")
public interface UserClient {
    
    /**
     * 获取用户信息
     */
    @GetMapping("/internal/users/{id}")
    Result<UserInfo> getUserInfo(@PathVariable("id") Long userId);
}
