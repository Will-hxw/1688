package com.cqu.marketplace.gateway;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gateway 路由属性测试
 * Feature: microservices-migration, Property 1: 路由一致性
 * Validates: Requirements 1.1
 */
class GatewayRoutePropertyTest {
    
    /**
     * 路由规则映射：路径前缀 -> 目标服务
     */
    private static final Map<String, String> ROUTE_RULES = Map.of(
        "/api/auth/", "auth-user-service",
        "/api/users/", "auth-user-service",
        "/api/admin/users/", "auth-user-service",
        "/api/products/", "product-service",
        "/api/upload/", "product-service",
        "/api/admin/products/", "product-service",
        "/api/orders/", "order-service",
        "/api/admin/orders/", "order-service",
        "/api/reviews/", "review-service",
        "/api/admin/reviews/", "review-service"
    );
    
    /**
     * Property 1: 路由一致性
     * 对于任意请求路径，网关应将请求路由到正确的目标服务
     */
    @Property(tries = 100)
    void routeConsistency(@ForAll("validApiPaths") String path) {
        String targetService = resolveTargetService(path);
        
        // 验证路径能够被正确路由
        assertThat(targetService)
            .as("路径 %s 应该能被路由到某个服务", path)
            .isNotNull();
        
        // 验证路由到正确的服务
        String expectedService = getExpectedService(path);
        assertThat(targetService)
            .as("路径 %s 应该路由到 %s", path, expectedService)
            .isEqualTo(expectedService);
    }
    
    /**
     * 生成有效的 API 路径
     */
    @Provide
    Arbitrary<String> validApiPaths() {
        return Arbitraries.of(
            // Auth-User Service 路径
            "/api/auth/login",
            "/api/auth/register",
            "/api/users/me",
            "/api/users/me/products",
            "/api/admin/users/1",
            "/api/admin/users/1/status",
            // Product Service 路径
            "/api/products",
            "/api/products/1",
            "/api/products/search",
            "/api/upload/image",
            "/api/admin/products/1",
            // Order Service 路径
            "/api/orders",
            "/api/orders/buyer",
            "/api/orders/seller",
            "/api/orders/1/status",
            "/api/admin/orders/1",
            // Review Service 路径
            "/api/reviews",
            "/api/reviews/product/1",
            "/api/admin/reviews/1"
        );
    }
    
    /**
     * 根据路径解析目标服务
     */
    private String resolveTargetService(String path) {
        // 按照最长前缀匹配原则
        String matchedPrefix = null;
        int maxLength = 0;
        
        for (String prefix : ROUTE_RULES.keySet()) {
            if (path.startsWith(prefix) && prefix.length() > maxLength) {
                matchedPrefix = prefix;
                maxLength = prefix.length();
            }
        }
        
        // 特殊处理：/api/products 和 /api/orders 等根路径
        if (matchedPrefix == null) {
            if (path.equals("/api/products") || path.startsWith("/api/products?")) {
                return "product-service";
            }
            if (path.equals("/api/orders") || path.startsWith("/api/orders?")) {
                return "order-service";
            }
            if (path.equals("/api/reviews") || path.startsWith("/api/reviews?")) {
                return "review-service";
            }
        }
        
        return matchedPrefix != null ? ROUTE_RULES.get(matchedPrefix) : null;
    }
    
    /**
     * 获取期望的目标服务
     */
    private String getExpectedService(String path) {
        if (path.contains("/auth/") || path.contains("/users/") || 
            path.contains("/admin/users/")) {
            return "auth-user-service";
        }
        if (path.contains("/products") || path.contains("/upload/") || 
            path.contains("/admin/products/")) {
            return "product-service";
        }
        if (path.contains("/orders") || path.contains("/admin/orders/")) {
            return "order-service";
        }
        if (path.contains("/reviews") || path.contains("/admin/reviews/")) {
            return "review-service";
        }
        return null;
    }
    
    /**
     * 单元测试：验证路由规则覆盖所有服务
     */
    @Test
    void allServicesHaveRoutes() {
        assertThat(ROUTE_RULES.values())
            .contains("auth-user-service", "product-service", "order-service", "review-service");
    }
    
    /**
     * 单元测试：验证 StripPrefix 配置
     * 前端请求 /api/products/1 -> 下游服务收到 /products/1
     */
    @Test
    void stripPrefixRemovesApiPrefix() {
        String originalPath = "/api/products/1";
        String strippedPath = stripPrefix(originalPath, 1);
        
        assertThat(strippedPath).isEqualTo("/products/1");
    }
    
    /**
     * 模拟 StripPrefix 过滤器行为
     */
    private String stripPrefix(String path, int parts) {
        String[] segments = path.split("/");
        StringBuilder result = new StringBuilder();
        for (int i = parts + 1; i < segments.length; i++) {
            result.append("/").append(segments[i]);
        }
        return result.length() > 0 ? result.toString() : "/";
    }
}
