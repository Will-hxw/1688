package com.cqu.marketplace.e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 完整业务流程端到端测试
 * 
 * 测试流程：注册 → 登录 → 发布商品 → 下单 → 发货 → 收货 → 评价
 * 
 * Requirements: 全部
 * 
 * 运行前提：
 * 1. 执行 docker-compose up -d 启动所有服务
 * 2. 等待所有服务健康检查通过
 * 3. 运行测试：mvn test -pl services/e2e-tests -DskipE2ETests=false
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("完整业务流程 E2E 测试")
class FullBusinessFlowE2ETest extends BaseE2ETest {
    
    // 测试数据（跨测试方法共享）
    private static String sellerToken;
    private static String buyerToken;
    private static Long sellerId;
    private static Long buyerId;
    private static Long productId;
    private static Long orderId;
    
    // 测试用户信息
    private static String sellerUsername;
    private static String buyerUsername;
    private static final String PASSWORD = "test123456";
    
    @Test
    @Order(1)
    @DisplayName("1. 卖家注册")
    void step1_sellerRegister() {
        sellerUsername = uniqueUsername() + "_seller";
        
        Map<String, Object> request = new HashMap<>();
        request.put("username", sellerUsername);
        request.put("password", PASSWORD);
        request.put("nickname", "测试卖家");
        
        Response response = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", notNullValue())
                .extract().response();
        
        sellerId = response.jsonPath().getLong("data");
        assertThat(sellerId).isPositive();
        
        System.out.println("卖家注册成功，ID: " + sellerId);
    }
    
    @Test
    @Order(2)
    @DisplayName("2. 卖家登录")
    void step2_sellerLogin() {
        Map<String, Object> request = new HashMap<>();
        request.put("username", sellerUsername);
        request.put("password", PASSWORD);
        
        Response response = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.token", notNullValue())
                .body("data.userId", equalTo(sellerId.intValue()))
                .extract().response();
        
        sellerToken = response.jsonPath().getString("data.token");
        assertThat(sellerToken).isNotBlank();
        
        System.out.println("卖家登录成功，Token: " + sellerToken.substring(0, 20) + "...");
    }
    
    @Test
    @Order(3)
    @DisplayName("3. 买家注册")
    void step3_buyerRegister() {
        buyerUsername = uniqueUsername() + "_buyer";
        
        Map<String, Object> request = new HashMap<>();
        request.put("username", buyerUsername);
        request.put("password", PASSWORD);
        request.put("nickname", "测试买家");
        
        Response response = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", notNullValue())
                .extract().response();
        
        buyerId = response.jsonPath().getLong("data");
        assertThat(buyerId).isPositive();
        
        System.out.println("买家注册成功，ID: " + buyerId);
    }
    
    @Test
    @Order(4)
    @DisplayName("4. 买家登录")
    void step4_buyerLogin() {
        Map<String, Object> request = new HashMap<>();
        request.put("username", buyerUsername);
        request.put("password", PASSWORD);
        
        Response response = given()
                .spec(requestSpec)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.token", notNullValue())
                .extract().response();
        
        buyerToken = response.jsonPath().getString("data.token");
        assertThat(buyerToken).isNotBlank();
        
        System.out.println("买家登录成功");
    }
    
    @Test
    @Order(5)
    @DisplayName("5. 卖家发布商品")
    void step5_createProduct() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "E2E测试商品_" + System.currentTimeMillis());
        request.put("description", "这是一个端到端测试商品");
        request.put("price", 99.99);
        request.put("imageUrl", "/uploads/test.jpg");
        request.put("category", "测试分类");
        request.put("stock", 10);
        
        Response response = given()
                .spec(withAuth(sellerToken))
                .body(request)
                .when()
                .post("/api/products")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", notNullValue())
                .extract().response();
        
        productId = response.jsonPath().getLong("data");
        assertThat(productId).isPositive();
        
        System.out.println("商品发布成功，ID: " + productId);
    }
    
    @Test
    @Order(6)
    @DisplayName("6. 验证商品详情")
    void step6_verifyProductDetail() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.id", equalTo(productId.intValue()))
                .body("data.sellerId", equalTo(sellerId.intValue()))
                .body("data.stock", equalTo(10))
                .body("data.status", equalTo("ON_SALE"));
        
        System.out.println("商品详情验证通过");
    }
    
    @Test
    @Order(7)
    @DisplayName("7. 买家下单")
    void step7_createOrder() {
        String idempotencyKey = uniqueIdempotencyKey();
        
        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        
        Response response = given()
                .spec(withIdempotencyKey(buyerToken, idempotencyKey))
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", notNullValue())
                .extract().response();
        
        orderId = response.jsonPath().getLong("data");
        assertThat(orderId).isPositive();
        
        System.out.println("订单创建成功，ID: " + orderId);
    }
    
    @Test
    @Order(8)
    @DisplayName("8. 验证库存扣减")
    void step8_verifyStockDecreased() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("data.stock", equalTo(9));  // 库存从10减到9
        
        System.out.println("库存扣减验证通过，当前库存: 9");
    }
    
    @Test
    @Order(9)
    @DisplayName("9. 验证买家订单列表")
    void step9_verifyBuyerOrders() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .get("/api/orders/buyer")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.list", hasSize(greaterThanOrEqualTo(1)))
                .body("data.list[0].id", equalTo(orderId.intValue()))
                .body("data.list[0].status", equalTo("CREATED"));
        
        System.out.println("买家订单列表验证通过");
    }
    
    @Test
    @Order(10)
    @DisplayName("10. 卖家发货")
    void step10_shipOrder() {
        given()
                .spec(withAuth(sellerToken))
                .when()
                .post("/api/orders/" + orderId + "/ship")
                .then()
                .statusCode(200)
                .body("code", equalTo(200));
        
        System.out.println("卖家发货成功");
    }
    
    @Test
    @Order(11)
    @DisplayName("11. 验证订单状态为 SHIPPED")
    void step11_verifyOrderShipped() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .get("/api/orders/buyer")
                .then()
                .statusCode(200)
                .body("data.list[0].status", equalTo("SHIPPED"));
        
        System.out.println("订单状态验证通过: SHIPPED");
    }
    
    @Test
    @Order(12)
    @DisplayName("12. 买家确认收货")
    void step12_receiveOrder() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .post("/api/orders/" + orderId + "/receive")
                .then()
                .statusCode(200)
                .body("code", equalTo(200));
        
        System.out.println("买家确认收货成功");
    }
    
    @Test
    @Order(13)
    @DisplayName("13. 验证订单状态为 RECEIVED")
    void step13_verifyOrderReceived() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .get("/api/orders/buyer")
                .then()
                .statusCode(200)
                .body("data.list[0].status", equalTo("RECEIVED"));
        
        System.out.println("订单状态验证通过: RECEIVED");
    }
    
    @Test
    @Order(14)
    @DisplayName("14. 买家提交评价")
    void step14_createReview() {
        Map<String, Object> request = new HashMap<>();
        request.put("orderId", orderId);
        request.put("rating", 5);
        request.put("content", "非常满意，E2E测试评价！");
        
        given()
                .spec(withAuth(buyerToken))
                .body(request)
                .when()
                .post("/api/reviews")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data", notNullValue());
        
        System.out.println("评价提交成功");
    }
    
    @Test
    @Order(15)
    @DisplayName("15. 验证订单状态为 REVIEWED")
    void step15_verifyOrderReviewed() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .get("/api/orders/buyer")
                .then()
                .statusCode(200)
                .body("data.list[0].status", equalTo("REVIEWED"));
        
        System.out.println("订单状态验证通过: REVIEWED");
    }
    
    @Test
    @Order(16)
    @DisplayName("16. 验证商品评价列表")
    void step16_verifyProductReviews() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/reviews/product/" + productId)
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .body("data.list", hasSize(greaterThanOrEqualTo(1)))
                .body("data.list[0].rating", equalTo(5))
                .body("data.list[0].content", containsString("E2E测试评价"));
        
        System.out.println("商品评价列表验证通过");
    }
    
    @Test
    @Order(17)
    @DisplayName("17. 完整流程验证总结")
    void step17_summary() {
        System.out.println("\n========== E2E 测试完成 ==========");
        System.out.println("卖家ID: " + sellerId);
        System.out.println("买家ID: " + buyerId);
        System.out.println("商品ID: " + productId);
        System.out.println("订单ID: " + orderId);
        System.out.println("流程: 注册 → 登录 → 发布商品 → 下单 → 发货 → 收货 → 评价");
        System.out.println("所有跨服务调用验证通过！");
        System.out.println("==================================\n");
        
        assertThat(sellerId).isPositive();
        assertThat(buyerId).isPositive();
        assertThat(productId).isPositive();
        assertThat(orderId).isPositive();
    }
}
