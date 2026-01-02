package com.cqu.marketplace.e2e;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

/**
 * E2E 测试基类
 * 配置 REST Assured 基础设置
 * 
 * 运行前提：所有微服务已通过 docker-compose up 启动
 */
public abstract class BaseE2ETest {
    
    /** Gateway 服务地址（通过环境变量配置，默认 localhost:8080） */
    protected static final String GATEWAY_URL = System.getenv().getOrDefault(
            "GATEWAY_URL", "http://localhost:8080");
    
    /** 请求规范 */
    protected static RequestSpecification requestSpec;
    
    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = GATEWAY_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL);
        
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }
    
    /**
     * 创建带 JWT Token 的请求规范
     */
    protected RequestSpecification withAuth(String token) {
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }
    
    /**
     * 创建带幂等键的请求规范
     */
    protected RequestSpecification withIdempotencyKey(String token, String idempotencyKey) {
        return new RequestSpecBuilder()
                .addRequestSpecification(requestSpec)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Idempotency-Key", idempotencyKey)
                .build();
    }
    
    /**
     * 生成唯一用户名（避免测试冲突）
     */
    protected String uniqueUsername() {
        return "e2e_user_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
    
    /**
     * 生成唯一幂等键
     */
    protected String uniqueIdempotencyKey() {
        return "idem_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
}
