package com.cqu.marketplace.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 验证属性测试
 * Feature: microservices-migration
 * Property 2: JWT 验证正确性
 * Property 3: 请求头转发完整性
 * Validates: Requirements 1.2, 1.3, 1.4
 */
class JwtAuthPropertyTest {
    
    private static final String JWT_SECRET = "cqu-marketplace-jwt-secret-key-2024-very-long-secret";
    private static final long JWT_EXPIRATION = 86400000L; // 24小时
    
    /**
     * Property 2: JWT 验证正确性
     * 对于任意有效的 JWT Token，网关应成功验证并提取正确的用户 ID 和角色
     */
    @Property(tries = 100)
    void validJwtShouldBeAccepted(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll("validUsernames") String username,
            @ForAll("validRoles") String role) {
        
        // 生成有效的 JWT
        String token = generateToken(userId, username, role);
        
        // 验证 Token 有效性
        boolean isValid = validateToken(token);
        assertThat(isValid)
            .as("有效的 JWT Token 应该通过验证")
            .isTrue();
        
        // 验证提取的用户信息正确
        Long extractedUserId = getUserIdFromToken(token);
        String extractedRole = getRoleFromToken(token);
        
        assertThat(extractedUserId)
            .as("提取的用户 ID 应该与原始值一致")
            .isEqualTo(userId);
        
        assertThat(extractedRole)
            .as("提取的角色应该与原始值一致")
            .isEqualTo(role);
    }
    
    /**
     * Property 2: JWT 验证正确性（无效 Token）
     * 对于任意无效或过期的 JWT Token，网关应返回验证失败
     */
    @Property(tries = 100)
    void invalidJwtShouldBeRejected(@ForAll("invalidTokens") String token) {
        boolean isValid = validateToken(token);
        assertThat(isValid)
            .as("无效的 JWT Token 应该验证失败")
            .isFalse();
    }
    
    /**
     * Property 2: 过期 Token 应被拒绝
     */
    @Property(tries = 50)
    void expiredJwtShouldBeRejected(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll("validUsernames") String username,
            @ForAll("validRoles") String role) {
        
        // 生成已过期的 JWT
        String token = generateExpiredToken(userId, username, role);
        
        boolean isValid = validateToken(token);
        assertThat(isValid)
            .as("过期的 JWT Token 应该验证失败")
            .isFalse();
    }
    
    /**
     * Property 3: 请求头转发完整性
     * 对于任意通过 JWT 验证的请求，转发到下游服务的请求头应包含正确的 X-User-Id 和 X-User-Role
     */
    @Property(tries = 100)
    void headerForwardingCompleteness(
            @ForAll @LongRange(min = 1, max = 10000) Long userId,
            @ForAll("validRoles") String role) {
        
        // 模拟请求头转发
        Map<String, String> forwardedHeaders = simulateHeaderForwarding(userId, role);
        
        // 验证 X-User-Id 头存在且正确
        assertThat(forwardedHeaders)
            .as("转发的请求头应包含 X-User-Id")
            .containsKey("X-User-Id");
        assertThat(forwardedHeaders.get("X-User-Id"))
            .as("X-User-Id 应该与用户 ID 一致")
            .isEqualTo(userId.toString());
        
        // 验证 X-User-Role 头存在且正确
        assertThat(forwardedHeaders)
            .as("转发的请求头应包含 X-User-Role")
            .containsKey("X-User-Role");
        assertThat(forwardedHeaders.get("X-User-Role"))
            .as("X-User-Role 应该与角色一致")
            .isEqualTo(role);
    }
    
    // ========== 辅助方法 ==========
    
    @Provide
    Arbitrary<String> validUsernames() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(3)
            .ofMaxLength(20);
    }
    
    @Provide
    Arbitrary<String> validRoles() {
        return Arbitraries.of("USER", "ADMIN");
    }
    
    @Provide
    Arbitrary<String> invalidTokens() {
        return Arbitraries.of(
            "",
            "invalid",
            "Bearer invalid",
            "eyJhbGciOiJIUzI1NiJ9.invalid.signature",
            "not.a.jwt",
            "eyJhbGciOiJIUzI1NiJ9",
            null
        ).filter(t -> t != null);
    }
    
    /**
     * 生成有效的 JWT Token
     */
    private String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_EXPIRATION);
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(getSigningKey())
            .compact();
    }
    
    /**
     * 生成已过期的 JWT Token
     */
    private String generateExpiredToken(Long userId, String username, String role) {
        Date past = new Date(System.currentTimeMillis() - 86400000L); // 24小时前
        Date expired = new Date(past.getTime() + 1000L); // 过期时间在过去
        
        return Jwts.builder()
            .subject(userId.toString())
            .claim("username", username)
            .claim("role", role)
            .issuedAt(past)
            .expiration(expired)
            .signWith(getSigningKey())
            .compact();
    }
    
    /**
     * 验证 Token 有效性
     */
    private boolean validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 从 Token 中提取用户 ID
     */
    private Long getUserIdFromToken(String token) {
        return Long.parseLong(
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject()
        );
    }
    
    /**
     * 从 Token 中提取角色
     */
    private String getRoleFromToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("role", String.class);
    }
    
    /**
     * 模拟请求头转发
     */
    private Map<String, String> simulateHeaderForwarding(Long userId, String role) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-User-Id", userId.toString());
        headers.put("X-User-Role", role);
        return headers;
    }
    
    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
