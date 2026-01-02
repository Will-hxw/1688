package com.cqu.marketplace.product;

import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.product.entity.Product;
import com.cqu.marketplace.product.mapper.ProductMapper;
import com.cqu.marketplace.product.service.ProductService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 库存扣减原子性属性测试
 * 
 * Feature: microservices-migration, Property 6: 库存扣减原子性
 * Validates: Requirements 3.6
 * 
 * 属性：对于任意并发的库存扣减请求，最终库存数量应等于初始库存减去成功扣减的次数，且不会出现负库存。
 */
@SpringBootTest
@ActiveProfiles("test")
public class StockAtomicityPropertyTest {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductService productService;
    
    /**
     * Property 6: 库存扣减原子性
     * 
     * 使用 JUnit 测试验证并发库存扣减的原子性
     * 对于任意初始库存和并发扣减请求数，最终库存应等于 max(0, 初始库存 - 成功扣减次数)
     * 且成功扣减次数不超过初始库存
     */
    @Test
    void stockDecrementIsAtomic() throws Exception {
        // 测试多组参数组合
        int[][] testCases = {
            {1, 5},    // 库存1，5个并发请求
            {5, 10},   // 库存5，10个并发请求
            {10, 20},  // 库存10，20个并发请求
            {20, 50},  // 库存20，50个并发请求
            {50, 100}, // 库存50，100个并发请求
        };
        
        for (int[] testCase : testCases) {
            int initialStock = testCase[0];
            int concurrentRequests = testCase[1];
            
            // 创建测试商品
            Product product = createTestProduct(initialStock);
            Long productId = product.getId();
            
            // 并发扣减库存
            ExecutorService executor = Executors.newFixedThreadPool(Math.min(concurrentRequests, 20));
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(concurrentRequests);
            AtomicInteger successCount = new AtomicInteger(0);
            
            for (int i = 0; i < concurrentRequests; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await(); // 等待所有线程就绪后同时开始
                        boolean success = productService.decrementStock(productId);
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
            Product updatedProduct = productMapper.selectById(productId);
            int finalStock = updatedProduct.getStock();
            int actualSuccessCount = successCount.get();
            
            // 属性1：成功扣减次数不超过初始库存
            assertThat(actualSuccessCount)
                .as("成功扣减次数不应超过初始库存 (initialStock=%d, requests=%d)", initialStock, concurrentRequests)
                .isLessThanOrEqualTo(initialStock);
            
            // 属性2：最终库存 = 初始库存 - 成功扣减次数
            assertThat(finalStock)
                .as("最终库存应等于初始库存减去成功扣减次数 (initialStock=%d, successCount=%d)", initialStock, actualSuccessCount)
                .isEqualTo(initialStock - actualSuccessCount);
            
            // 属性3：库存不为负
            assertThat(finalStock)
                .as("库存不应为负数")
                .isGreaterThanOrEqualTo(0);
            
            // 属性4：如果库存为0，状态应为SOLD
            if (finalStock == 0) {
                assertThat(updatedProduct.getStatus())
                    .as("库存为0时状态应为SOLD")
                    .isEqualTo(ProductStatus.SOLD);
            }
            
            // 清理测试数据
            productMapper.deleteById(productId);
        }
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
