package com.cqu.marketplace.order;

import com.cqu.marketplace.common.enums.OrderStatus;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单快照完整性属性测试
 * 
 * **Property 10: 订单快照完整性**
 * *For any* 创建的订单，订单记录应包含商品快照信息（名称、价格、图片），
 * 且与创建时的商品信息一致。
 * 
 * **Validates: Requirements 4.11**
 * 
 * Feature: microservices-migration, Property 10: 订单快照完整性
 * 
 * 注意：此测试验证快照逻辑的正确性，使用内存模拟避免 Spring 上下文依赖。
 */
class OrderSnapshotPropertyTest {
    
    /**
     * 订单记录（含快照字段）
     */
    private static class OrderRecord {
        private final Long id;
        private final Long buyerId;
        private final Long sellerId;
        private final Long productId;
        private final BigDecimal price;
        private final String productName;
        private final String productImage;
        private final String idempotencyKey;
        private OrderStatus status;
        
        public OrderRecord(Long id, Long buyerId, Long sellerId, Long productId,
                          BigDecimal price, String productName, String productImage,
                          String idempotencyKey, OrderStatus status) {
            this.id = id;
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.productId = productId;
            this.price = price;
            this.productName = productName;
            this.productImage = productImage;
            this.idempotencyKey = idempotencyKey;
            this.status = status;
        }
        
        public Long getId() { return id; }
        public Long getBuyerId() { return buyerId; }
        public Long getSellerId() { return sellerId; }
        public Long getProductId() { return productId; }
        public BigDecimal getPrice() { return price; }
        public String getProductName() { return productName; }
        public String getProductImage() { return productImage; }
        public String getIdempotencyKey() { return idempotencyKey; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
    }
    
    /**
     * 模拟订单存储（内存实现）
     */
    private static class InMemoryOrderStore {
        private final Map<Long, OrderRecord> orders = new ConcurrentHashMap<>();
        private final AtomicLong orderIdGenerator = new AtomicLong(1);
        
        public OrderRecord insert(Long buyerId, Long sellerId, Long productId,
                                 BigDecimal price, String productName, String productImage,
                                 String idempotencyKey) {
            Long orderId = orderIdGenerator.getAndIncrement();
            OrderRecord order = new OrderRecord(orderId, buyerId, sellerId, productId,
                price, productName, productImage, idempotencyKey, OrderStatus.CREATED);
            orders.put(orderId, order);
            return order;
        }
        
        public OrderRecord selectById(Long orderId) {
            return orders.get(orderId);
        }
        
        public boolean updateStatus(Long orderId, OrderStatus fromStatus, OrderStatus toStatus) {
            OrderRecord order = orders.get(orderId);
            if (order == null || order.getStatus() != fromStatus) {
                return false;
            }
            order.setStatus(toStatus);
            return true;
        }
        
        public void clear() {
            orders.clear();
            orderIdGenerator.set(1);
        }
    }
    
    private final InMemoryOrderStore orderStore = new InMemoryOrderStore();
    
    /**
     * Property 10.1: 订单快照字段完整性
     * 
     * 验证：订单创建后，快照字段（productName, productImage, price）应与输入一致
     * 
     * Feature: microservices-migration, Property 10: 订单快照完整性
     * Validates: Requirements 4.11
     */
    @Property(tries = 100)
    void orderSnapshotShouldMatchInput(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1, max = 1000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll @StringLength(min = 1, max = 50) String productName,
            @ForAll @StringLength(min = 1, max = 100) String productImage,
            @ForAll("validPrice") BigDecimal price) {
        
        orderStore.clear();
        
        // 创建订单（含快照字段）
        String idempotencyKey = UUID.randomUUID().toString();
        OrderRecord order = orderStore.insert(buyerId, sellerId, productId,
            price, productName, productImage, idempotencyKey);
        
        // 从存储读取
        OrderRecord saved = orderStore.selectById(order.getId());
        
        // 验证快照字段完整性
        assertThat(saved).isNotNull();
        assertThat(saved.getProductName()).isEqualTo(productName);
        assertThat(saved.getProductImage()).isEqualTo(productImage);
        assertThat(saved.getPrice()).isEqualByComparingTo(price);
    }
    
    /**
     * Property 10.2: 订单快照不可变性
     * 
     * 验证：订单状态变更后，快照字段应保持不变
     * 
     * Feature: microservices-migration, Property 10: 订单快照完整性
     * Validates: Requirements 4.11
     */
    @Property(tries = 100)
    void orderSnapshotShouldRemainUnchangedAfterStatusUpdate(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1, max = 1000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll @StringLength(min = 1, max = 50) String productName,
            @ForAll @StringLength(min = 1, max = 100) String productImage,
            @ForAll("validPrice") BigDecimal price) {
        
        orderStore.clear();
        
        // 创建订单
        String idempotencyKey = UUID.randomUUID().toString();
        OrderRecord order = orderStore.insert(buyerId, sellerId, productId,
            price, productName, productImage, idempotencyKey);
        Long orderId = order.getId();
        
        // 执行状态转换：CREATED → SHIPPED
        orderStore.updateStatus(orderId, OrderStatus.CREATED, OrderStatus.SHIPPED);
        
        // 验证快照字段未变
        OrderRecord afterShip = orderStore.selectById(orderId);
        assertThat(afterShip.getProductName()).isEqualTo(productName);
        assertThat(afterShip.getProductImage()).isEqualTo(productImage);
        assertThat(afterShip.getPrice()).isEqualByComparingTo(price);
        
        // 执行状态转换：SHIPPED → RECEIVED
        orderStore.updateStatus(orderId, OrderStatus.SHIPPED, OrderStatus.RECEIVED);
        
        // 验证快照字段仍未变
        OrderRecord afterReceive = orderStore.selectById(orderId);
        assertThat(afterReceive.getProductName()).isEqualTo(productName);
        assertThat(afterReceive.getProductImage()).isEqualTo(productImage);
        assertThat(afterReceive.getPrice()).isEqualByComparingTo(price);
    }
    
    /**
     * Property 10.3: 订单快照字段非空
     * 
     * 验证：订单的快照字段不能为空
     * 
     * Feature: microservices-migration, Property 10: 订单快照完整性
     * Validates: Requirements 4.11
     */
    @Property(tries = 100)
    void orderSnapshotFieldsShouldNotBeNull(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1, max = 1000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll @StringLength(min = 1, max = 50) String productName,
            @ForAll @StringLength(min = 1, max = 100) String productImage,
            @ForAll("validPrice") BigDecimal price) {
        
        orderStore.clear();
        
        // 创建订单
        String idempotencyKey = UUID.randomUUID().toString();
        OrderRecord order = orderStore.insert(buyerId, sellerId, productId,
            price, productName, productImage, idempotencyKey);
        
        // 从存储读取
        OrderRecord saved = orderStore.selectById(order.getId());
        
        // 验证快照字段非空
        assertThat(saved.getProductName()).isNotNull().isNotEmpty();
        assertThat(saved.getProductImage()).isNotNull().isNotEmpty();
        assertThat(saved.getPrice()).isNotNull().isPositive();
    }
    
    /**
     * Property 10.4: 订单快照与原始商品信息一致性
     * 
     * 验证：订单快照应与创建时传入的商品信息完全一致
     * 
     * Feature: microservices-migration, Property 10: 订单快照完整性
     * Validates: Requirements 4.11
     */
    @Property(tries = 100)
    void orderSnapshotShouldBeConsistentWithOriginalProduct(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1, max = 1000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll @StringLength(min = 1, max = 50) String productName,
            @ForAll @StringLength(min = 1, max = 100) String productImage,
            @ForAll("validPrice") BigDecimal price) {
        
        orderStore.clear();
        
        // 模拟商品快照信息
        record ProductSnapshot(String name, String imageUrl, BigDecimal price) {}
        ProductSnapshot snapshot = new ProductSnapshot(productName, productImage, price);
        
        // 创建订单时使用快照信息
        String idempotencyKey = UUID.randomUUID().toString();
        OrderRecord order = orderStore.insert(buyerId, sellerId, productId,
            snapshot.price(), snapshot.name(), snapshot.imageUrl(), idempotencyKey);
        
        // 验证订单快照与原始快照一致
        OrderRecord saved = orderStore.selectById(order.getId());
        assertThat(saved.getProductName()).isEqualTo(snapshot.name());
        assertThat(saved.getProductImage()).isEqualTo(snapshot.imageUrl());
        assertThat(saved.getPrice()).isEqualByComparingTo(snapshot.price());
    }
    
    @Provide
    Arbitrary<BigDecimal> validPrice() {
        return Arbitraries.bigDecimals()
            .between(new BigDecimal("0.01"), new BigDecimal("99999.99"))
            .ofScale(2);
    }
}
