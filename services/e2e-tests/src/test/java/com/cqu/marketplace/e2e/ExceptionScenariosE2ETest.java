package com.cqu.marketplace.e2e;

import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 异常场景端到端测试
 * 
 * 测试场景：
 * 1. 库存不足下单失败 (Requirements 3.7)
 * 2. 重复下单幂等返回 (Requirements 4.1)
 * 3. 非法状态转换拒绝 (Requirements 4.8)
 * 
 * 运行前提：
 * 1. 执行 docker-compose up -d 启动所有服务
 * 2. 等待所有服务健康检查通过
 * 3. 运行测试：mvn test -pl services/e2e-tests -DskipE2ETests=false
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("异常场景 E2E 测试")
class ExceptionScenariosE2ETest extends BaseE2ETest {
    
    // 测试数据
    private static String sellerToken;
    private static String buyerToken;
    private static Long sellerId;
    private static Long buyerId;
    private static Long productId;
    private static Long orderId;
    private static String savedIdempotencyKey;
    
    private static final String PASSWORD = "test123456";
    
    // ==================== 测试准备 ====================
    
    @Test
    @Order(1)
    @DisplayName("准备：创建卖家账户")
    void setup1_createSeller() {
        String username = uniqueUsername() + "_seller";
        
        // 注册
        Map<String, Object> registerReq = new HashMap<>();
        registerReq.put("username", username);
        registerReq.put("password", PASSWORD);
        registerReq.put("nickname", "异常测试卖家");
        
        Response registerResp = given()
                .spec(requestSpec)
                .body(registerReq)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract().response();
        
        sellerId = registerResp.jsonPath().getLong("data");
        
        // 登录
        Map<String, Object> loginReq = new HashMap<>();
        loginReq.put("username", username);
        loginReq.put("password", PASSWORD);
        
        Response loginResp = given()
                .spec(requestSpec)
                .body(loginReq)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().response();
        
        sellerToken = loginResp.jsonPath().getString("data.token");
        
        System.out.println("卖家准备完成，ID: " + sellerId);
    }
    
    @Test
    @Order(2)
    @DisplayName("准备：创建买家账户")
    void setup2_createBuyer() {
        String username = uniqueUsername() + "_buyer";
        
        // 注册
        Map<String, Object> registerReq = new HashMap<>();
        registerReq.put("username", username);
        registerReq.put("password", PASSWORD);
        registerReq.put("nickname", "异常测试买家");
        
        Response registerResp = given()
                .spec(requestSpec)
                .body(registerReq)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract().response();
        
        buyerId = registerResp.jsonPath().getLong("data");
        
        // 登录
        Map<String, Object> loginReq = new HashMap<>();
        loginReq.put("username", username);
        loginReq.put("password", PASSWORD);
        
        Response loginResp = given()
                .spec(requestSpec)
                .body(loginReq)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().response();
        
        buyerToken = loginResp.jsonPath().getString("data.token");
        
        System.out.println("买家准备完成，ID: " + buyerId);
    }
    
    @Test
    @Order(3)
    @DisplayName("准备：创建库存为1的商品")
    void setup3_createProductWithStock1() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "库存测试商品_" + System.currentTimeMillis());
        request.put("description", "库存只有1件的测试商品");
        request.put("price", 50.00);
        request.put("imageUrl", "/uploads/test.jpg");
        request.put("category", "测试");
        request.put("stock", 1);  // 只有1件库存
        
        Response response = given()
                .spec(withAuth(sellerToken))
                .body(request)
                .when()
                .post("/api/products")
                .then()
                .statusCode(200)
                .extract().response();
        
        productId = response.jsonPath().getLong("data");
        
        System.out.println("商品准备完成，ID: " + productId + "，库存: 1");
    }
    
    // ==================== 场景1：库存不足下单失败 ====================
    
    @Test
    @Order(10)
    @DisplayName("场景1.1：第一次下单成功（消耗库存）")
    void scenario1_1_firstOrderSuccess() {
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
                .extract().response();
        
        orderId = response.jsonPath().getLong("data");
        
        System.out.println("第一次下单成功，订单ID: " + orderId);
    }
    
    @Test
    @Order(11)
    @DisplayName("场景1.2：验证库存已耗尽")
    void scenario1_2_verifyStockExhausted() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("data.stock", equalTo(0));
        
        System.out.println("库存验证通过，当前库存: 0");
    }
    
    @Test
    @Order(12)
    @DisplayName("场景1.3：库存不足下单失败 (Requirements 3.7)")
    void scenario1_3_orderFailDueToInsufficientStock() {
        String idempotencyKey = uniqueIdempotencyKey();
        
        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        
        // 库存不足应返回 409 Conflict
        given()
                .spec(withIdempotencyKey(buyerToken, idempotencyKey))
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(409)
                .body("code", equalTo(409))
                .body("message", containsStringIgnoringCase("库存"));
        
        System.out.println("库存不足下单失败验证通过");
    }
    
    // ==================== 场景2：重复下单幂等返回 ====================
    
    @Test
    @Order(20)
    @DisplayName("准备：创建新商品用于幂等测试")
    void scenario2_0_createNewProduct() {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "幂等测试商品_" + System.currentTimeMillis());
        request.put("description", "用于幂等性测试的商品");
        request.put("price", 100.00);
        request.put("imageUrl", "/uploads/test.jpg");
        request.put("category", "测试");
        request.put("stock", 10);
        
        Response response = given()
                .spec(withAuth(sellerToken))
                .body(request)
                .when()
                .post("/api/products")
                .then()
                .statusCode(200)
                .extract().response();
        
        productId = response.jsonPath().getLong("data");
        
        System.out.println("幂等测试商品创建完成，ID: " + productId);
    }
    
    @Test
    @Order(21)
    @DisplayName("场景2.1：首次下单成功")
    void scenario2_1_firstOrderWithIdempotencyKey() {
        savedIdempotencyKey = uniqueIdempotencyKey();
        
        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        
        Response response = given()
                .spec(withIdempotencyKey(buyerToken, savedIdempotencyKey))
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .extract().response();
        
        orderId = response.jsonPath().getLong("data");
        
        System.out.println("首次下单成功，订单ID: " + orderId + "，幂等键: " + savedIdempotencyKey);
    }
    
    @Test
    @Order(22)
    @DisplayName("场景2.2：重复下单返回相同订单ID (Requirements 4.1)")
    void scenario2_2_duplicateOrderReturnsExistingId() {
        Map<String, Object> request = new HashMap<>();
        request.put("productId", productId);
        
        // 使用相同的幂等键再次下单
        Response response = given()
                .spec(withIdempotencyKey(buyerToken, savedIdempotencyKey))
                .body(request)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("code", equalTo(200))
                .extract().response();
        
        Long duplicateOrderId = response.jsonPath().getLong("data");
        
        // 应返回相同的订单ID
        assertThat(duplicateOrderId).isEqualTo(orderId);
        
        System.out.println("幂等性验证通过，重复下单返回相同订单ID: " + duplicateOrderId);
    }
    
    @Test
    @Order(23)
    @DisplayName("场景2.3：验证库存只扣减一次")
    void scenario2_3_verifyStockDeductedOnce() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("data.stock", equalTo(9));  // 只扣减了1次
        
        System.out.println("库存验证通过，只扣减一次，当前库存: 9");
    }
    
    // ==================== 场景3：非法状态转换拒绝 ====================
    
    @Test
    @Order(30)
    @DisplayName("准备：创建新商品和订单用于状态机测试")
    void scenario3_0_createOrderForStateMachineTest() {
        // 创建商品
        Map<String, Object> productReq = new HashMap<>();
        productReq.put("name", "状态机测试商品_" + System.currentTimeMillis());
        productReq.put("description", "用于状态机测试的商品");
        productReq.put("price", 200.00);
        productReq.put("imageUrl", "/uploads/test.jpg");
        productReq.put("category", "测试");
        productReq.put("stock", 5);
        
        Response productResp = given()
                .spec(withAuth(sellerToken))
                .body(productReq)
                .when()
                .post("/api/products")
                .then()
                .statusCode(200)
                .extract().response();
        
        productId = productResp.jsonPath().getLong("data");
        
        // 创建订单
        Map<String, Object> orderReq = new HashMap<>();
        orderReq.put("productId", productId);
        
        Response orderResp = given()
                .spec(withIdempotencyKey(buyerToken, uniqueIdempotencyKey()))
                .body(orderReq)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .extract().response();
        
        orderId = orderResp.jsonPath().getLong("data");
        
        System.out.println("状态机测试准备完成，商品ID: " + productId + "，订单ID: " + orderId);
    }
    
    @Test
    @Order(31)
    @DisplayName("场景3.1：CREATED 状态不能直接确认收货 (Requirements 4.8)")
    void scenario3_1_cannotReceiveFromCreated() {
        // 订单当前状态是 CREATED，不能直接确认收货
        given()
                .spec(withAuth(buyerToken))
                .when()
                .post("/api/orders/" + orderId + "/receive")
                .then()
                .statusCode(409)
                .body("code", equalTo(409))
                .body("message", containsStringIgnoringCase("状态"));
        
        System.out.println("非法状态转换拒绝验证通过：CREATED → RECEIVED 被拒绝");
    }
    
    @Test
    @Order(32)
    @DisplayName("场景3.2：卖家发货")
    void scenario3_2_shipOrder() {
        given()
                .spec(withAuth(sellerToken))
                .when()
                .post("/api/orders/" + orderId + "/ship")
                .then()
                .statusCode(200);
        
        System.out.println("卖家发货成功");
    }
    
    @Test
    @Order(33)
    @DisplayName("场景3.3：SHIPPED 状态不能取消订单 (Requirements 4.8)")
    void scenario3_3_cannotCancelFromShipped() {
        // 订单当前状态是 SHIPPED，不能取消
        given()
                .spec(withAuth(buyerToken))
                .when()
                .post("/api/orders/" + orderId + "/cancel")
                .then()
                .statusCode(409)
                .body("code", equalTo(409))
                .body("message", containsStringIgnoringCase("状态"));
        
        System.out.println("非法状态转换拒绝验证通过：SHIPPED → CANCELED 被拒绝");
    }
    
    @Test
    @Order(34)
    @DisplayName("场景3.4：SHIPPED 状态不能重复发货 (Requirements 4.8)")
    void scenario3_4_cannotShipAgain() {
        // 订单已经是 SHIPPED 状态，不能再次发货
        given()
                .spec(withAuth(sellerToken))
                .when()
                .post("/api/orders/" + orderId + "/ship")
                .then()
                .statusCode(409)
                .body("code", equalTo(409))
                .body("message", containsStringIgnoringCase("状态"));
        
        System.out.println("非法状态转换拒绝验证通过：SHIPPED → SHIPPED 被拒绝");
    }
    
    @Test
    @Order(35)
    @DisplayName("场景3.5：买家确认收货")
    void scenario3_5_receiveOrder() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .post("/api/orders/" + orderId + "/receive")
                .then()
                .statusCode(200);
        
        System.out.println("买家确认收货成功");
    }
    
    @Test
    @Order(36)
    @DisplayName("场景3.6：RECEIVED 状态不能取消订单 (Requirements 4.8)")
    void scenario3_6_cannotCancelFromReceived() {
        // 订单当前状态是 RECEIVED，不能取消
        given()
                .spec(withAuth(buyerToken))
                .when()
                .post("/api/orders/" + orderId + "/cancel")
                .then()
                .statusCode(409)
                .body("code", equalTo(409))
                .body("message", containsStringIgnoringCase("状态"));
        
        System.out.println("非法状态转换拒绝验证通过：RECEIVED → CANCELED 被拒绝");
    }
    
    // ==================== 场景4：取消订单库存回滚 ====================
    
    @Test
    @Order(40)
    @DisplayName("准备：创建新订单用于取消测试")
    void scenario4_0_createOrderForCancelTest() {
        // 创建商品
        Map<String, Object> productReq = new HashMap<>();
        productReq.put("name", "取消测试商品_" + System.currentTimeMillis());
        productReq.put("description", "用于取消订单测试的商品");
        productReq.put("price", 150.00);
        productReq.put("imageUrl", "/uploads/test.jpg");
        productReq.put("category", "测试");
        productReq.put("stock", 3);
        
        Response productResp = given()
                .spec(withAuth(sellerToken))
                .body(productReq)
                .when()
                .post("/api/products")
                .then()
                .statusCode(200)
                .extract().response();
        
        productId = productResp.jsonPath().getLong("data");
        
        // 创建订单
        Map<String, Object> orderReq = new HashMap<>();
        orderReq.put("productId", productId);
        
        Response orderResp = given()
                .spec(withIdempotencyKey(buyerToken, uniqueIdempotencyKey()))
                .body(orderReq)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .extract().response();
        
        orderId = orderResp.jsonPath().getLong("data");
        
        System.out.println("取消测试准备完成，商品ID: " + productId + "，订单ID: " + orderId);
    }
    
    @Test
    @Order(41)
    @DisplayName("场景4.1：验证下单后库存扣减")
    void scenario4_1_verifyStockAfterOrder() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("data.stock", equalTo(2));  // 3 - 1 = 2
        
        System.out.println("下单后库存验证通过，当前库存: 2");
    }
    
    @Test
    @Order(42)
    @DisplayName("场景4.2：取消订单")
    void scenario4_2_cancelOrder() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .post("/api/orders/" + orderId + "/cancel")
                .then()
                .statusCode(200)
                .body("code", equalTo(200));
        
        System.out.println("订单取消成功");
    }
    
    @Test
    @Order(43)
    @DisplayName("场景4.3：验证取消后库存回滚")
    void scenario4_3_verifyStockRollback() {
        given()
                .spec(requestSpec)
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(200)
                .body("data.stock", equalTo(3));  // 库存回滚到 3
        
        System.out.println("取消后库存回滚验证通过，当前库存: 3");
    }
    
    @Test
    @Order(44)
    @DisplayName("场景4.4：验证订单状态为 CANCELED")
    void scenario4_4_verifyOrderCanceled() {
        given()
                .spec(withAuth(buyerToken))
                .when()
                .get("/api/orders/buyer")
                .then()
                .statusCode(200)
                .body("data.list.find { it.id == " + orderId + " }.status", equalTo("CANCELED"));
        
        System.out.println("订单状态验证通过: CANCELED");
    }
    
    @Test
    @Order(99)
    @DisplayName("异常场景测试总结")
    void summary() {
        System.out.println("\n========== 异常场景 E2E 测试完成 ==========");
        System.out.println("场景1: 库存不足下单失败 ✓");
        System.out.println("场景2: 重复下单幂等返回 ✓");
        System.out.println("场景3: 非法状态转换拒绝 ✓");
        System.out.println("场景4: 取消订单库存回滚 ✓");
        System.out.println("==========================================\n");
    }
}
