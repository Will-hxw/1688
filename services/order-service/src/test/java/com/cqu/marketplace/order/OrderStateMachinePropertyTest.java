package com.cqu.marketplace.order;

import com.cqu.marketplace.common.enums.CanceledBy;
import com.cqu.marketplace.common.enums.OrderStatus;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 订单状态机属性测试
 * 
 * **Property 9: 订单状态机合法性**
 * *For any* 订单状态转换，只有合法的状态转换路径才能成功：
 * - CREATED → SHIPPED（卖家发货）
 * - SHIPPED → RECEIVED（买家确认）
 * - RECEIVED → REVIEWED（评价后）
 * - CREATED → CANCELED（取消，触发库存回滚）
 * 
 * **Property 9.1: 取消订单库存回滚**
 * *For any* 订单从 CREATED 状态取消，系统应调用 increase-stock 回滚库存，
 * 且回滚后库存数量应等于扣减前的值。
 * 
 * **Validates: Requirements 4.5-4.8**
 * 
 * Feature: microservices-migration, Property 9: 订单状态机合法性
 */
class OrderStateMachinePropertyTest {
    
    /**
     * 模拟订单存储（内存实现）
     */
    private static class InMemoryOrderStore {
        private final Map<Long, OrderRecord> orders = new ConcurrentHashMap<>();
        private final AtomicLong orderIdGenerator = new AtomicLong(1);
        
        public OrderRecord createOrder(Long buyerId, Long sellerId, Long productId) {
            Long orderId = orderIdGenerator.getAndIncrement();
            OrderRecord order = new OrderRecord(orderId, buyerId, sellerId, productId, OrderStatus.CREATED);
            orders.put(orderId, order);
            return order;
        }
        
        public OrderRecord getOrder(Long orderId) {
            return orders.get(orderId);
        }
        
        public void clear() {
            orders.clear();
            orderIdGenerator.set(1);
        }
    }
    
    /**
     * 模拟库存存储（内存实现）
     */
    private static class InMemoryStockStore {
        private final Map<Long, Integer> stocks = new ConcurrentHashMap<>();
        private final List<StockOperation> operations = Collections.synchronizedList(new ArrayList<>());
        
        public void setStock(Long productId, int stock) {
            stocks.put(productId, stock);
        }
        
        public int getStock(Long productId) {
            return stocks.getOrDefault(productId, 0);
        }
        
        public boolean decreaseStock(Long productId) {
            return stocks.compute(productId, (k, v) -> {
                if (v == null || v <= 0) return v;
                return v - 1;
            }) != null && stocks.get(productId) >= 0;
        }
        
        public void increaseStock(Long productId, Long orderId) {
            stocks.compute(productId, (k, v) -> (v == null ? 0 : v) + 1);
            operations.add(new StockOperation(productId, orderId, "INCREASE"));
        }
        
        public List<StockOperation> getOperations() {
            return new ArrayList<>(operations);
        }
        
        public void clear() {
            stocks.clear();
            operations.clear();
        }
    }
    
    /**
     * 订单记录
     */
    private static class OrderRecord {
        private final Long id;
        private final Long buyerId;
        private final Long sellerId;
        private final Long productId;
        private OrderStatus status;
        private CanceledBy canceledBy;
        
        public OrderRecord(Long id, Long buyerId, Long sellerId, Long productId, OrderStatus status) {
            this.id = id;
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.productId = productId;
            this.status = status;
        }
        
        public Long getId() { return id; }
        public Long getBuyerId() { return buyerId; }
        public Long getSellerId() { return sellerId; }
        public Long getProductId() { return productId; }
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        public CanceledBy getCanceledBy() { return canceledBy; }
        public void setCanceledBy(CanceledBy canceledBy) { this.canceledBy = canceledBy; }
    }
    
    /**
     * 库存操作记录
     */
    private record StockOperation(Long productId, Long orderId, String type) {}
    
    /**
     * 模拟订单服务（状态机逻辑）
     */
    private static class OrderStateMachine {
        private final InMemoryOrderStore orderStore;
        private final InMemoryStockStore stockStore;
        
        public OrderStateMachine(InMemoryOrderStore orderStore, InMemoryStockStore stockStore) {
            this.orderStore = orderStore;
            this.stockStore = stockStore;
        }
        
        /**
         * 发货：CREATED → SHIPPED
         */
        public boolean ship(Long orderId, Long sellerId) {
            OrderRecord order = orderStore.getOrder(orderId);
            if (order == null) return false;
            if (!order.getSellerId().equals(sellerId)) return false;
            if (order.getStatus() != OrderStatus.CREATED) return false;
            
            order.setStatus(OrderStatus.SHIPPED);
            return true;
        }
        
        /**
         * 确认收货：SHIPPED → RECEIVED
         */
        public boolean receive(Long orderId, Long buyerId) {
            OrderRecord order = orderStore.getOrder(orderId);
            if (order == null) return false;
            if (!order.getBuyerId().equals(buyerId)) return false;
            if (order.getStatus() != OrderStatus.SHIPPED) return false;
            
            order.setStatus(OrderStatus.RECEIVED);
            return true;
        }
        
        /**
         * 标记已评价：RECEIVED → REVIEWED
         */
        public boolean markReviewed(Long orderId) {
            OrderRecord order = orderStore.getOrder(orderId);
            if (order == null) return false;
            if (order.getStatus() != OrderStatus.RECEIVED) return false;
            
            order.setStatus(OrderStatus.REVIEWED);
            return true;
        }
        
        /**
         * 取消订单：CREATED → CANCELED（触发库存回滚）
         */
        public boolean cancel(Long orderId, Long userId) {
            OrderRecord order = orderStore.getOrder(orderId);
            if (order == null) return false;
            
            boolean isBuyer = order.getBuyerId().equals(userId);
            boolean isSeller = order.getSellerId().equals(userId);
            if (!isBuyer && !isSeller) return false;
            
            if (order.getStatus() != OrderStatus.CREATED) return false;
            
            order.setStatus(OrderStatus.CANCELED);
            order.setCanceledBy(isBuyer ? CanceledBy.BUYER : CanceledBy.SELLER);
            
            // 回滚库存
            stockStore.increaseStock(order.getProductId(), orderId);
            
            return true;
        }
        
        /**
         * 尝试任意状态转换（用于测试非法转换）
         */
        public boolean tryTransition(Long orderId, OrderStatus targetStatus) {
            OrderRecord order = orderStore.getOrder(orderId);
            if (order == null) return false;
            
            OrderStatus currentStatus = order.getStatus();
            if (!currentStatus.canTransitionTo(targetStatus)) {
                return false;
            }
            
            order.setStatus(targetStatus);
            return true;
        }
    }
    
    private final InMemoryOrderStore orderStore = new InMemoryOrderStore();
    private final InMemoryStockStore stockStore = new InMemoryStockStore();
    private final OrderStateMachine stateMachine = new OrderStateMachine(orderStore, stockStore);

    
    /**
     * Property 9.1: 合法状态转换 - CREATED → SHIPPED
     * 
     * 验证：卖家可以将 CREATED 状态的订单发货为 SHIPPED
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.5
     */
    @Property(tries = 100)
    void createdToShippedShouldSucceed(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        
        // 卖家发货
        boolean success = stateMachine.ship(order.getId(), sellerId);
        
        // 验证：状态转换成功
        assertThat(success).isTrue();
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
    
    /**
     * Property 9.2: 合法状态转换 - SHIPPED → RECEIVED
     * 
     * 验证：买家可以将 SHIPPED 状态的订单确认收货为 RECEIVED
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.6
     */
    @Property(tries = 100)
    void shippedToReceivedShouldSucceed(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单并发货
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        stateMachine.ship(order.getId(), sellerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        
        // 买家确认收货
        boolean success = stateMachine.receive(order.getId(), buyerId);
        
        // 验证：状态转换成功
        assertThat(success).isTrue();
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }
    
    /**
     * Property 9.3: 合法状态转换 - RECEIVED → REVIEWED
     * 
     * 验证：RECEIVED 状态的订单可以标记为 REVIEWED
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.5-4.8
     */
    @Property(tries = 100)
    void receivedToReviewedShouldSucceed(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单并完成发货、收货流程
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        stateMachine.ship(order.getId(), sellerId);
        stateMachine.receive(order.getId(), buyerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        
        // 标记已评价
        boolean success = stateMachine.markReviewed(order.getId());
        
        // 验证：状态转换成功
        assertThat(success).isTrue();
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.REVIEWED);
    }
    
    /**
     * Property 9.4: 合法状态转换 - CREATED → CANCELED（买家取消）
     * 
     * 验证：买家可以取消 CREATED 状态的订单
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.7
     */
    @Property(tries = 100)
    void createdToCanceledByBuyerShouldSucceed(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        stockStore.setStock(productId, 0); // 模拟已扣减库存
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        
        // 买家取消
        boolean success = stateMachine.cancel(order.getId(), buyerId);
        
        // 验证：状态转换成功
        assertThat(success).isTrue();
        OrderRecord canceled = orderStore.getOrder(order.getId());
        assertThat(canceled.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(canceled.getCanceledBy()).isEqualTo(CanceledBy.BUYER);
    }
    
    /**
     * Property 9.5: 合法状态转换 - CREATED → CANCELED（卖家取消）
     * 
     * 验证：卖家可以取消 CREATED 状态的订单
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.7
     */
    @Property(tries = 100)
    void createdToCanceledBySellerShouldSucceed(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        stockStore.setStock(productId, 0);
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        
        // 卖家取消
        boolean success = stateMachine.cancel(order.getId(), sellerId);
        
        // 验证：状态转换成功
        assertThat(success).isTrue();
        OrderRecord canceled = orderStore.getOrder(order.getId());
        assertThat(canceled.getStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(canceled.getCanceledBy()).isEqualTo(CanceledBy.SELLER);
    }

    
    /**
     * Property 9.6: 非法状态转换 - SHIPPED 不能取消
     * 
     * 验证：SHIPPED 状态的订单不能被取消
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.8
     */
    @Property(tries = 100)
    void shippedCannotBeCanceled(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单并发货
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        stateMachine.ship(order.getId(), sellerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        
        // 尝试取消（买家）
        boolean buyerCancel = stateMachine.cancel(order.getId(), buyerId);
        assertThat(buyerCancel).isFalse();
        
        // 尝试取消（卖家）
        boolean sellerCancel = stateMachine.cancel(order.getId(), sellerId);
        assertThat(sellerCancel).isFalse();
        
        // 验证：状态未变
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
    
    /**
     * Property 9.7: 非法状态转换 - RECEIVED 不能取消
     * 
     * 验证：RECEIVED 状态的订单不能被取消
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.8
     */
    @Property(tries = 100)
    void receivedCannotBeCanceled(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单并完成发货、收货
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        stateMachine.ship(order.getId(), sellerId);
        stateMachine.receive(order.getId(), buyerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.RECEIVED);
        
        // 尝试取消
        boolean cancelResult = stateMachine.cancel(order.getId(), buyerId);
        
        // 验证：取消失败，状态未变
        assertThat(cancelResult).isFalse();
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.RECEIVED);
    }
    
    /**
     * Property 9.8: 非法状态转换 - REVIEWED 不能取消
     * 
     * 验证：REVIEWED 状态的订单不能被取消
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.8
     */
    @Property(tries = 100)
    void reviewedCannotBeCanceled(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单并完成全流程
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        stateMachine.ship(order.getId(), sellerId);
        stateMachine.receive(order.getId(), buyerId);
        stateMachine.markReviewed(order.getId());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.REVIEWED);
        
        // 尝试取消
        boolean cancelResult = stateMachine.cancel(order.getId(), buyerId);
        
        // 验证：取消失败，状态未变
        assertThat(cancelResult).isFalse();
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.REVIEWED);
    }
    
    /**
     * Property 9.9: 非法状态转换 - CANCELED 不能再转换
     * 
     * 验证：CANCELED 状态的订单不能转换到任何其他状态
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.8
     */
    @Property(tries = 100)
    void canceledCannotTransitionToAnyState(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll("targetStatus") OrderStatus targetStatus) {
        
        orderStore.clear();
        stockStore.clear();
        stockStore.setStock(productId, 0);
        
        // 创建订单并取消
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        stateMachine.cancel(order.getId(), buyerId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELED);
        
        // 尝试转换到任意状态
        boolean transitionResult = stateMachine.tryTransition(order.getId(), targetStatus);
        
        // 验证：转换失败，状态未变
        assertThat(transitionResult).isFalse();
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.CANCELED);
    }
    
    /**
     * Property 9.10: 非法状态转换 - 跳跃转换不允许
     * 
     * 验证：不能跳过中间状态直接转换（如 CREATED → RECEIVED）
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.8
     */
    @Property(tries = 100)
    void skipTransitionShouldFail(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        
        // 尝试跳跃转换：CREATED → RECEIVED（跳过 SHIPPED）
        boolean skipToReceived = stateMachine.tryTransition(order.getId(), OrderStatus.RECEIVED);
        assertThat(skipToReceived).isFalse();
        
        // 尝试跳跃转换：CREATED → REVIEWED（跳过 SHIPPED 和 RECEIVED）
        boolean skipToReviewed = stateMachine.tryTransition(order.getId(), OrderStatus.REVIEWED);
        assertThat(skipToReviewed).isFalse();
        
        // 验证：状态未变
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    
    /**
     * Property 9.11: 权限校验 - 非卖家不能发货
     * 
     * 验证：只有卖家可以发货，其他用户发货应失败
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.5
     */
    @Property(tries = 100)
    void onlySellerCanShip(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 2001, max = 3000) Long otherUserId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        
        // 买家尝试发货
        boolean buyerShip = stateMachine.ship(order.getId(), buyerId);
        assertThat(buyerShip).isFalse();
        
        // 其他用户尝试发货
        boolean otherShip = stateMachine.ship(order.getId(), otherUserId);
        assertThat(otherShip).isFalse();
        
        // 验证：状态未变
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.CREATED);
        
        // 卖家发货应成功
        boolean sellerShip = stateMachine.ship(order.getId(), sellerId);
        assertThat(sellerShip).isTrue();
    }
    
    /**
     * Property 9.12: 权限校验 - 非买家不能确认收货
     * 
     * 验证：只有买家可以确认收货，其他用户确认收货应失败
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.6
     */
    @Property(tries = 100)
    void onlyBuyerCanReceive(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 2001, max = 3000) Long otherUserId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 创建订单并发货
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        stateMachine.ship(order.getId(), sellerId);
        
        // 卖家尝试确认收货
        boolean sellerReceive = stateMachine.receive(order.getId(), sellerId);
        assertThat(sellerReceive).isFalse();
        
        // 其他用户尝试确认收货
        boolean otherReceive = stateMachine.receive(order.getId(), otherUserId);
        assertThat(otherReceive).isFalse();
        
        // 验证：状态未变
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.SHIPPED);
        
        // 买家确认收货应成功
        boolean buyerReceive = stateMachine.receive(order.getId(), buyerId);
        assertThat(buyerReceive).isTrue();
    }
    
    /**
     * Property 9.13: 权限校验 - 只有买家或卖家可以取消
     * 
     * 验证：只有买家或卖家可以取消订单，其他用户取消应失败
     * 
     * Feature: microservices-migration, Property 9: 订单状态机合法性
     * Validates: Requirements 4.7
     */
    @Property(tries = 100)
    void onlyBuyerOrSellerCanCancel(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 2001, max = 3000) Long otherUserId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        stockStore.setStock(productId, 0);
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        
        // 其他用户尝试取消
        boolean otherCancel = stateMachine.cancel(order.getId(), otherUserId);
        assertThat(otherCancel).isFalse();
        
        // 验证：状态未变
        assertThat(orderStore.getOrder(order.getId()).getStatus()).isEqualTo(OrderStatus.CREATED);
        
        // 买家取消应成功
        boolean buyerCancel = stateMachine.cancel(order.getId(), buyerId);
        assertThat(buyerCancel).isTrue();
    }
    
    // ==================== Property 9.1: 取消订单库存回滚 ====================
    
    /**
     * Property 9.1.1: 取消订单触发库存回滚
     * 
     * 验证：取消订单时应调用 increase-stock 回滚库存
     * 
     * Feature: microservices-migration, Property 9.1: 取消订单库存回滚
     * Validates: Requirements 4.7, 3.8
     */
    @Property(tries = 100)
    void cancelOrderShouldTriggerStockRollback(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll @LongRange(min = 0, max = 100) int initialStock) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 设置初始库存（模拟下单后已扣减）
        stockStore.setStock(productId, initialStock);
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        
        // 取消订单
        boolean cancelSuccess = stateMachine.cancel(order.getId(), buyerId);
        assertThat(cancelSuccess).isTrue();
        
        // 验证：库存回滚操作被调用
        List<StockOperation> operations = stockStore.getOperations();
        assertThat(operations).hasSize(1);
        assertThat(operations.get(0).productId()).isEqualTo(productId);
        assertThat(operations.get(0).orderId()).isEqualTo(order.getId());
        assertThat(operations.get(0).type()).isEqualTo("INCREASE");
    }
    
    /**
     * Property 9.1.2: 取消订单后库存数量正确
     * 
     * 验证：取消订单后库存数量应等于扣减前的值（+1）
     * 
     * Feature: microservices-migration, Property 9.1: 取消订单库存回滚
     * Validates: Requirements 4.7, 3.8
     */
    @Property(tries = 100)
    void cancelOrderShouldRestoreStock(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId,
            @ForAll @LongRange(min = 0, max = 100) int stockAfterDecrease) {
        
        orderStore.clear();
        stockStore.clear();
        
        // 设置库存（模拟下单后已扣减的状态）
        stockStore.setStock(productId, stockAfterDecrease);
        int expectedStockAfterRollback = stockAfterDecrease + 1;
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        
        // 取消订单
        stateMachine.cancel(order.getId(), buyerId);
        
        // 验证：库存恢复
        int actualStock = stockStore.getStock(productId);
        assertThat(actualStock).isEqualTo(expectedStockAfterRollback);
    }
    
    /**
     * Property 9.1.3: 非取消状态转换不触发库存回滚
     * 
     * 验证：发货、收货、评价等状态转换不应触发库存回滚
     * 
     * Feature: microservices-migration, Property 9.1: 取消订单库存回滚
     * Validates: Requirements 4.5-4.6
     */
    @Property(tries = 100)
    void nonCancelTransitionShouldNotTriggerStockRollback(
            @ForAll @LongRange(min = 1, max = 1000) Long buyerId,
            @ForAll @LongRange(min = 1001, max = 2000) Long sellerId,
            @ForAll @LongRange(min = 1, max = 1000) Long productId) {
        
        orderStore.clear();
        stockStore.clear();
        stockStore.setStock(productId, 10);
        
        // 创建订单
        OrderRecord order = orderStore.createOrder(buyerId, sellerId, productId);
        
        // 执行正常流程：发货 → 收货 → 评价
        stateMachine.ship(order.getId(), sellerId);
        stateMachine.receive(order.getId(), buyerId);
        stateMachine.markReviewed(order.getId());
        
        // 验证：没有库存回滚操作
        List<StockOperation> operations = stockStore.getOperations();
        assertThat(operations).isEmpty();
        
        // 验证：库存未变
        assertThat(stockStore.getStock(productId)).isEqualTo(10);
    }
    
    /**
     * 提供目标状态（排除当前状态）
     */
    @Provide
    Arbitrary<OrderStatus> targetStatus() {
        return Arbitraries.of(OrderStatus.values());
    }
}
