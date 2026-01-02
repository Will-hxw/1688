package com.cqu.marketplace.order;

import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单幂等性属性测试
 * 
 * **Property 8: 订单幂等性**
 * *For any* 相同的 (buyerId, idempotencyKey) 组合，多次创建订单请求应返回相同的订单 ID，
 * 且只创建一条订单记录。
 * 
 * **Validates: Requirements 4.1**
 * 
 * Feature: microservices-migration, Property 8: 订单幂等性
 * 
 * 注意：此测试验证幂等性逻辑的正确性，不依赖 Spring 上下文。
 * 使用内存模拟来验证幂等键的行为符合预期。
 */
class OrderIdempotencyPropertyTest {
    
    /**
     * 模拟订单存储（内存实现）
     * 使用 ConcurrentHashMap 模拟数据库的唯一约束 (buyer_id, idempotency_key)
     */
    private static class InMemoryOrderStore {
        // 模拟数据库唯一约束：(buyerId, idempotencyKey) -> orderId
        private final Map<String, Long> orderIndex = new ConcurrentHashMap<>();
        // 订单ID生成器
        private final AtomicLong orderIdGenerator = new AtomicLong(1);
        
        /**
         * 生成唯一键
         */
        private String generateKey(Long buyerId, String idempotencyKey) {
            return buyerId + ":" + idempotencyKey;
        }
        
        /**
         * 创建订单（幂等操作）
         * 如果相同 (buyerId, idempotencyKey) 已存在，返回已存在的订单ID
         * 否则创建新订单并返回新ID
         */
        public Long createOrder(Long buyerId, String idempotencyKey) {
            String key = generateKey(buyerId, idempotencyKey);
            // 使用 computeIfAbsent 保证原子性
            return orderIndex.computeIfAbsent(key, k -> orderIdGenerator.getAndIncrement());
        }
        
        /**
         * 查询订单
         */
        public Long getOrder(Long buyerId, String idempotencyKey) {
            String key = generateKey(buyerId, idempotencyKey);
            return orderIndex.get(key);
        }
        
        /**
         * 统计指定 (buyerId, idempotencyKey) 的订单数量
         */
        public long countOrders(Long buyerId, String idempotencyKey) {
            String key = generateKey(buyerId, idempotencyKey);
            return orderIndex.containsKey(key) ? 1 : 0;
        }
        
        /**
         * 清空存储
         */
        public void clear() {
            orderIndex.clear();
            orderIdGenerator.set(1);
        }
    }
    
    private final InMemoryOrderStore orderStore = new InMemoryOrderStore();
    
    /**
     * Property 8: 订单幂等性 - 相同幂等键返回相同订单ID
     * 
     * 验证：相同 (buyerId, idempotencyKey) 组合多次调用应返回相同的订单 ID
     * 
     * Feature: microservices-migration, Property 8: 订单幂等性
     * Validates: Requirements 4.1
     */
    @Property(tries = 100)
    void sameIdempotencyKeyShouldReturnSameOrderId(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @StringLength(min = 1, max = 64) String idempotencyKey) {
        
        // 清理之前的测试数据
        orderStore.clear();
        
        // 第一次创建订单
        Long orderId1 = orderStore.createOrder(buyerId, idempotencyKey);
        
        // 第二次创建订单（相同幂等键）
        Long orderId2 = orderStore.createOrder(buyerId, idempotencyKey);
        
        // 第三次创建订单（相同幂等键）
        Long orderId3 = orderStore.createOrder(buyerId, idempotencyKey);
        
        // 验证：所有调用返回相同的订单ID
        assertThat(orderId1)
            .as("第一次和第二次创建应返回相同订单ID")
            .isEqualTo(orderId2);
        assertThat(orderId2)
            .as("第二次和第三次创建应返回相同订单ID")
            .isEqualTo(orderId3);
        
        // 验证：只有一条订单记录
        long count = orderStore.countOrders(buyerId, idempotencyKey);
        assertThat(count)
            .as("数据库中应只有一条订单记录")
            .isEqualTo(1);
    }
    
    /**
     * Property 8.1: 不同幂等键应创建不同订单
     * 
     * 验证：不同的 idempotencyKey 应该创建不同的订单
     * 
     * Feature: microservices-migration, Property 8.1: 不同幂等键创建不同订单
     * Validates: Requirements 4.1
     */
    @Property(tries = 100)
    void differentIdempotencyKeysShouldCreateDifferentOrders(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @StringLength(min = 1, max = 30) String key1,
            @ForAll @StringLength(min = 1, max = 30) String key2) {
        
        // 确保两个幂等键不同
        Assume.that(!key1.equals(key2));
        
        // 清理之前的测试数据
        orderStore.clear();
        
        String idempotencyKey1 = "key1_" + key1;
        String idempotencyKey2 = "key2_" + key2;
        
        // 创建两个订单
        Long orderId1 = orderStore.createOrder(buyerId, idempotencyKey1);
        Long orderId2 = orderStore.createOrder(buyerId, idempotencyKey2);
        
        // 验证：两个订单ID不同
        assertThat(orderId1)
            .as("不同幂等键应创建不同订单")
            .isNotEqualTo(orderId2);
        
        // 验证：每个幂等键各有一条记录
        assertThat(orderStore.countOrders(buyerId, idempotencyKey1)).isEqualTo(1);
        assertThat(orderStore.countOrders(buyerId, idempotencyKey2)).isEqualTo(1);
    }
    
    /**
     * Property 8.2: 不同买家相同幂等键应创建不同订单
     * 
     * 验证：不同买家使用相同幂等键应该创建不同订单
     * 幂等键的唯一性是基于 (buyerId, idempotencyKey) 组合
     * 
     * Feature: microservices-migration, Property 8.2: 不同买家相同幂等键
     * Validates: Requirements 4.1
     */
    @Property(tries = 100)
    void differentBuyersSameIdempotencyKeyShouldCreateDifferentOrders(
            @ForAll @LongRange(min = 1, max = 500) Long buyerId1,
            @ForAll @LongRange(min = 501, max = 1000) Long buyerId2,
            @ForAll @StringLength(min = 1, max = 64) String idempotencyKey) {
        
        // 确保两个买家ID不同
        Assume.that(!buyerId1.equals(buyerId2));
        
        // 清理之前的测试数据
        orderStore.clear();
        
        // 不同买家使用相同幂等键创建订单
        Long orderId1 = orderStore.createOrder(buyerId1, idempotencyKey);
        Long orderId2 = orderStore.createOrder(buyerId2, idempotencyKey);
        
        // 验证：两个订单ID不同
        assertThat(orderId1)
            .as("不同买家使用相同幂等键应创建不同订单")
            .isNotEqualTo(orderId2);
        
        // 验证：每个买家各有一条记录
        assertThat(orderStore.countOrders(buyerId1, idempotencyKey)).isEqualTo(1);
        assertThat(orderStore.countOrders(buyerId2, idempotencyKey)).isEqualTo(1);
    }
    
    /**
     * Property 8.3: 幂等性查询一致性
     * 
     * 验证：创建订单后，查询应返回相同的订单ID
     * 
     * Feature: microservices-migration, Property 8.3: 幂等性查询一致性
     * Validates: Requirements 4.1
     */
    @Property(tries = 100)
    void orderQueryShouldReturnConsistentResult(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @StringLength(min = 1, max = 64) String idempotencyKey) {
        
        // 清理之前的测试数据
        orderStore.clear();
        
        // 创建订单
        Long createdOrderId = orderStore.createOrder(buyerId, idempotencyKey);
        
        // 查询订单
        Long queriedOrderId = orderStore.getOrder(buyerId, idempotencyKey);
        
        // 验证：查询结果与创建结果一致
        assertThat(queriedOrderId)
            .as("查询结果应与创建结果一致")
            .isEqualTo(createdOrderId);
    }
}
