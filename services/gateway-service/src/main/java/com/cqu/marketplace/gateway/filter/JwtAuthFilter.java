package com.cqu.marketplace.gateway.filter;

import com.cqu.marketplace.gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT认证过滤器
 * 负责验证JWT令牌、转发用户信息、拦截内部API
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * 公开路径白名单（无需认证）
     */
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register"
    );
    
    /**
     * 公开路径前缀（无需认证）
     */
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
        "/api/products/",
        "/api/reviews/product/"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String method = request.getMethod().name();
        
        long startTime = System.currentTimeMillis();
        log.debug("收到请求: {} {}", method, path);
        
        // 1. 拦截内部API访问（返回404，假装不存在）
        if (isInternalPath(path)) {
            log.warn("拒绝外部访问内部API: {}", path);
            return notFound(exchange);
        }
        
        // 2. 公开接口跳过验证
        if (isPublicPath(path, method)) {
            log.debug("公开接口，跳过认证: {}", path);
            return chain.filter(exchange).doFinally(signal -> 
                logRequest(method, path, startTime, exchange.getResponse().getStatusCode()));
        }
        
        // 3. 验证JWT
        String token = extractToken(request);
        if (token == null) {
            log.warn("缺少JWT令牌: {}", path);
            return unauthorized(exchange, "缺少认证令牌");
        }
        
        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("JWT令牌无效: {}", path);
            return unauthorized(exchange, "认证令牌无效或已过期");
        }
        
        // 4. 提取用户信息并转发
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);
        
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", userId.toString())
            .header("X-User-Role", role)
            .build();
        
        log.debug("JWT验证成功，用户ID: {}, 角色: {}", userId, role);
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
            .doFinally(signal -> 
                logRequest(method, path, startTime, exchange.getResponse().getStatusCode()));
    }
    
    @Override
    public int getOrder() {
        // 高优先级，确保最先执行
        return -100;
    }
    
    /**
     * 检查是否为内部API路径
     * 同时检查URL编码形式防止绕过
     */
    private boolean isInternalPath(String path) {
        String decodedPath = path.toLowerCase();
        return decodedPath.contains("/internal/") 
            || decodedPath.contains("/internal%2f")
            || decodedPath.contains("/internal%2F")
            || decodedPath.contains("%2finternal/")
            || decodedPath.contains("%2Finternal/");
    }
    
    /**
     * 检查是否为公开路径
     */
    private boolean isPublicPath(String path, String method) {
        // 精确匹配（仅登录注册）
        if (PUBLIC_PATHS.contains(path)) {
            return true;
        }
        
        // GET 请求的公开路径
        if ("GET".equalsIgnoreCase(method)) {
            // 前缀匹配
            for (String prefix : PUBLIC_PATH_PREFIXES) {
                if (path.startsWith(prefix)) {
                    return true;
                }
            }
            // GET /api/products 商品列表公开
            if (path.equals("/api/products") || path.startsWith("/api/products?")) {
                return true;
            }
            // GET /api/products/{id} 商品详情公开
            if (path.matches("/api/products/\\d+")) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 从请求头提取JWT令牌
     */
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String body = String.format(
            "{\"code\":401,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}",
            message, System.currentTimeMillis()
        );
        
        DataBuffer buffer = response.bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 返回404不存在响应（用于隐藏内部API）
     */
    private Mono<Void> notFound(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String body = String.format(
            "{\"code\":404,\"message\":\"资源不存在\",\"data\":null,\"timestamp\":%d}",
            System.currentTimeMillis()
        );
        
        DataBuffer buffer = response.bufferFactory()
            .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 记录请求日志
     */
    private void logRequest(String method, String path, long startTime, 
                           org.springframework.http.HttpStatusCode status) {
        long duration = System.currentTimeMillis() - startTime;
        log.info("请求完成: {} {} - {} ({}ms)", method, path, 
                status != null ? status.value() : "N/A", duration);
    }
}
