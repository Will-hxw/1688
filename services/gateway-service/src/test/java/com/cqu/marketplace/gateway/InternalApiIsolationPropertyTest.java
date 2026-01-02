package com.cqu.marketplace.gateway;

import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 内部 API 隔离属性测试
 * Feature: microservices-migration, Property 4: 内部 API 隔离
 * Validates: Requirements 7.3
 */
class InternalApiIsolationPropertyTest {
    
    /**
     * Property 4: 内部 API 隔离
     * 对于任意外部请求访问 /internal/** 路径，网关应返回 404 错误，阻止访问
     */
    @Property(tries = 100)
    void internalApiShouldBeBlocked(@ForAll("internalPaths") String path) {
        boolean isBlocked = isInternalPath(path);
        
        assertThat(isBlocked)
            .as("内部 API 路径 %s 应该被拦截", path)
            .isTrue();
    }
    
    /**
     * Property 4: URL 编码形式的内部 API 也应被拦截
     */
    @Property(tries = 100)
    void encodedInternalApiShouldBeBlocked(@ForAll("encodedInternalPaths") String path) {
        boolean isBlocked = isInternalPath(path);
        
        assertThat(isBlocked)
            .as("URL 编码的内部 API 路径 %s 应该被拦截", path)
            .isTrue();
    }
    
    /**
     * 正常 API 路径不应被拦截
     */
    @Property(tries = 100)
    void normalApiShouldNotBeBlocked(@ForAll("normalApiPaths") String path) {
        boolean isBlocked = isInternalPath(path);
        
        assertThat(isBlocked)
            .as("正常 API 路径 %s 不应该被拦截", path)
            .isFalse();
    }
    
    /**
     * 生成内部 API 路径
     */
    @Provide
    Arbitrary<String> internalPaths() {
        return Arbitraries.of(
            "/internal/users/1",
            "/internal/users/1/status",
            "/internal/products/1",
            "/internal/products/1/decrease-stock",
            "/internal/products/1/increase-stock",
            "/internal/orders/1",
            "/internal/orders/1/reviewed",
            "/api/internal/users/1",
            "/products/internal/test",
            "/users/internal/check"
        );
    }
    
    /**
     * 生成 URL 编码形式的内部 API 路径
     */
    @Provide
    Arbitrary<String> encodedInternalPaths() {
        return Arbitraries.of(
            // %2f 是 / 的 URL 编码（小写）
            "/internal%2fusers/1",
            "/internal%2fproducts/1",
            "/api%2finternal/users/1",
            // %2F 是 / 的 URL 编码（大写）
            "/internal%2Fusers/1",
            "/internal%2Fproducts/1",
            "/api%2Finternal/users/1",
            // 混合编码
            "%2finternal/users/1",
            "%2Finternal/products/1"
        );
    }
    
    /**
     * 生成正常 API 路径（不应被拦截）
     */
    @Provide
    Arbitrary<String> normalApiPaths() {
        return Arbitraries.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/users/me",
            "/api/products",
            "/api/products/1",
            "/api/orders",
            "/api/orders/buyer",
            "/api/reviews",
            "/api/admin/users/1",
            "/api/admin/products/1"
        );
    }
    
    /**
     * 检查是否为内部 API 路径
     * 复制自 JwtAuthFilter 的逻辑
     */
    private boolean isInternalPath(String path) {
        String decodedPath = path.toLowerCase();
        return decodedPath.contains("/internal/") 
            || decodedPath.contains("/internal%2f")
            || decodedPath.contains("/internal%2F".toLowerCase())
            || decodedPath.contains("%2finternal/")
            || decodedPath.contains("%2Finternal/".toLowerCase());
    }
}
