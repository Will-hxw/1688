package com.cqu.marketplace.product;

import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.product.entity.Product;
import com.cqu.marketplace.product.mapper.ProductMapper;
import com.cqu.marketplace.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 库存回滚一致性属性测试
 * 
 * Feature: microservices-migration, Property 7: 库存扣减回滚一致性
 * Validates: Requirements 3.8
 * 
 * 属性：对于任意库存扣减后的回滚操作，库存应正确恢复，且商品状态应从SOLD恢复为ON_SALE。
 */
@SpringBootTest
@ActiveProfiles("test")
public class StockRollbackConsistencyPropertyTest {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductService productService;
    
    /**
     * Property 7: 库存回滚一致性
     * 
     * 测试场景：
     * 1. 创建商品，扣减库存至0（状态变为SOLD）
     * 2. 执行回滚操作
     * 3. 验证库存恢复，状态恢复为ON_SALE
     */
    @Test
    void stockRollbackRestoresStockAndStatus() throws Exception {
        // 测试多组参数组合
        int[][] testCases = {
            {1, 1},    // 库存1，扣减1次后回滚1次
            {5, 5},    // 库存5，扣减5次后回滚5次
            {10, 5},   // 库存10，扣减10次后回滚5次
            {20, 10},  // 库存20，扣减20次后回滚10次
        };
        
        for (int[] testCase : testCases) {
            int initialStock = testCase[0];
            int rollbackCount = testCase[1];
            
            // 创建测试商品
            Product product = createTestProduct(initialStock);
            Long productId = product.getId();
            
            // 扣减所有库存
            for (int i = 0; i < initialStock; i++) {
                productService.decrementStock(productId);
            }
            
            // 验证库存已扣减至0，状态为SOLD
            Product afterDecrement = productMapper.selectById(productId);
            assertThat(afterDecrement.getStock())
                .as("扣减后库存应为0")
                .isEqualTo(0);
            assertThat(afterDecrement.getStatus())
                .as("扣减后状态应为SOLD")
                .isEqualTo(ProductStatus.SOLD);
            
            // 执行回滚操作
            int successRollbacks = 0;
            for (int i = 0; i < rollbackCount; i++) {
                boolean success = productService.incrementStock(productId);
                if (success) {
                    successRollbacks++;
                }
            }
            
            // 验证回滚结果
            Product afterRollback = productMapper.selectById(productId);
            
            // 属性1：回滚次数应等于请求次数（SOLD状态下回滚应成功）
            assertThat(successRollbacks)
                .as("回滚成功次数应等于请求次数 (rollbackCount=%d)", rollbackCount)
                .isEqualTo(rollbackCount);
            
            // 属性2：最终库存 = 回滚次数
            assertThat(afterRollback.getStock())
                .as("最终库存应等于回滚次数 (rollbackCount=%d)", rollbackCount)
                .isEqualTo(rollbackCount);
            
            // 属性3：回滚后状态应为ON_SALE
            assertThat(afterRollback.getStatus())
                .as("回滚后状态应为ON_SALE")
                .isEqualTo(ProductStatus.ON_SALE);
            
            // 清理测试数据
            productMapper.deleteById(productId);
        }
    }
    
    /**
     * 测试并发回滚的原子性
     * 
     * 属性：并发回滚操作应保持原子性，最终库存应等于成功回滚的次数
     */
    @Test
    void concurrentRollbackIsAtomic() throws Exception {
        int[][] testCases = {
            {10, 20},  // 扣减10次后，20个并发回滚请求
            {20, 50},  // 扣减20次后，50个并发回滚请求
        };
        
        for (int[] testCase : testCases) {
            int decrementCount = testCase[0];
            int concurrentRollbacks = testCase[1];
            
            // 创建测试商品（初始库存等于扣减次数）
            Product product = createTestProduct(decrementCount);
            Long productId = product.getId();
            
            // 扣减所有库存
            for (int i = 0; i < decrementCount; i++) {
                productService.decrementStock(productId);
            }
            
            // 验证库存已扣减至0
            Product afterDecrement = productMapper.selectById(productId);
            assertThat(afterDecrement.getStock()).isEqualTo(0);
            assertThat(afterDecrement.getStatus()).isEqualTo(ProductStatus.SOLD);
            
            // 并发回滚
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(concurrentRollbacks, 20));
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(concurrentRollbacks);
            AtomicInteger successCount = new AtomicInteger(0);
            
            for (int i = 0; i < concurrentRollbacks; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        boolean success = productService.incrementStock(productId);
                        if (success) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // 忽略异常
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }
            
            // 释放所有线程同时开始
            startLatch.countDown();
            
            // 等待所有任务完成
            doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();
            
            // 验证结果
            Product afterRollback = productMapper.selectById(productId);
            int finalStock = afterRollback.getStock();
            int actualSuccessCount = successCount.get();
            
            // 属性1：最终库存 = 成功回滚次数
            assertThat(finalStock)
                .as("最终库存应等于成功回滚次数 (successCount=%d)", actualSuccessCount)
                .isEqualTo(actualSuccessCount);
            
            // 属性2：回滚后状态应为ON_SALE（因为库存>0）
            assertThat(afterRollback.getStatus())
                .as("回滚后状态应为ON_SALE")
                .isEqualTo(ProductStatus.ON_SALE);
            
            // 清理测试数据
            productMapper.deleteById(productId);
        }
    }
    
    /**
     * 测试扣减-回滚循环的一致性
     * 
     * 属性：多次扣减和回滚后，库存应保持一致性
     */
    @Test
    void decrementRollbackCycleConsistency() throws Exception {
        int initialStock = 10;
        int cycles = 5;
        
        // 创建测试商品
        Product product = createTestProduct(initialStock);
        Long productId = product.getId();
        
        for (int cycle = 0; cycle < cycles; cycle++) {
            // 扣减一半库存
            int decrementCount = initialStock / 2;
            for (int i = 0; i < decrementCount; i++) {
                productService.decrementStock(productId);
            }
            
            // 验证扣减后库存
            Product afterDecrement = productMapper.selectById(productId);
            assertThat(afterDecrement.getStock())
                .as("第%d轮扣减后库存应为%d", cycle + 1, initialStock - decrementCount)
                .isEqualTo(initialStock - decrementCount);
            
            // 回滚扣减的库存
            for (int i = 0; i < decrementCount; i++) {
                productService.incrementStock(productId);
            }
            
            // 验证回滚后库存恢复
            Product afterRollback = productMapper.selectById(productId);
            assertThat(afterRollback.getStock())
                .as("第%d轮回滚后库存应恢复为%d", cycle + 1, initialStock)
                .isEqualTo(initialStock);
            assertThat(afterRollback.getStatus())
                .as("第%d轮回滚后状态应为ON_SALE", cycle + 1)
                .isEqualTo(ProductStatus.ON_SALE);
        }
        
        // 清理测试数据
        productMapper.deleteById(productId);
    }
    
    /**
     * 创建测试商品
     */
    private Product createTestProduct(int stock) {
        Product product = new Product();
        product.setSellerId(1L);
        product.setName("测试商品-" + System.currentTimeMillis());
        product.setDescription("测试描述");
        product.setPrice(new BigDecimal("99.99"));
        product.setImageUrl("/uploads/test.jpg");
        product.setCategory("测试分类");
        product.setStock(stock);
        product.setStatus(ProductStatus.ON_SALE);
        productMapper.insert(product);
        return product;
    }
}
