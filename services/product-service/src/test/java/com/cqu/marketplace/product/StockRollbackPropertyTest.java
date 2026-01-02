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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 库存扣减回滚一致性属性测试
 * 
 * Feature: microservices-migration, Property 7: 库存扣减回滚一致性
 * Validates: Requirements 3.8
 * 
 * 属性：对于任意库存扣减后执行回滚操作，库存数量应恢复到扣减前的值。
 */
@SpringBootTest
@ActiveProfiles("test")
public class StockRollbackPropertyTest {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private ProductService productService;
    
    /**
     * Property 7: 库存扣减回滚一致性
     * 
     * 测试多组参数组合，验证扣减后回滚能正确恢复库存
     */
    @Test
    void stockRollbackRestoresOriginalValue() {
        int[] initialStocks = {1, 5, 10, 20, 50, 100};
        
        for (int initialStock : initialStocks) {
            Product product = createTestProduct(initialStock);
            Long productId = product.getId();
            
            boolean decrementSuccess = productService.decrementStock(productId);
            assertThat(decrementSuccess)
                .as("初始库存%d时扣减应成功", initialStock)
                .isTrue();
            
            Product afterDecrement = productMapper.selectById(productId);
            assertThat(afterDecrement.getStock())
                .as("扣减后库存应为%d", initialStock - 1)
                .isEqualTo(initialStock - 1);
            
            boolean incrementSuccess = productService.incrementStock(productId);
            assertThat(incrementSuccess).as("回滚应成功").isTrue();
            
            Product afterRollback = productMapper.selectById(productId);
            assertThat(afterRollback.getStock())
                .as("回滚后库存应恢复为初始值%d", initialStock)
                .isEqualTo(initialStock);
            
            assertThat(afterRollback.getStatus())
                .as("回滚后状态应为ON_SALE")
                .isEqualTo(ProductStatus.ON_SALE);
            
            productMapper.deleteById(productId);
        }
    }
    
    @Test
    void stockRollbackFromSoldStatus() {
        Product product = createTestProduct(1);
        Long productId = product.getId();
        
        boolean decrementSuccess = productService.decrementStock(productId);
        assertThat(decrementSuccess).isTrue();
        
        Product afterDecrement = productMapper.selectById(productId);
        assertThat(afterDecrement.getStock()).isEqualTo(0);
        assertThat(afterDecrement.getStatus()).isEqualTo(ProductStatus.SOLD);
        
        boolean incrementSuccess = productService.incrementStock(productId);
        assertThat(incrementSuccess).isTrue();
        
        Product afterRollback = productMapper.selectById(productId);
        assertThat(afterRollback.getStock()).as("回滚后库存应恢复为1").isEqualTo(1);
        assertThat(afterRollback.getStatus())
            .as("回滚后状态应从SOLD恢复为ON_SALE")
            .isEqualTo(ProductStatus.ON_SALE);
        
        productMapper.deleteById(productId);
    }
    
    @Test
    void multipleDecrementAndRollback() {
        int initialStock = 10;
        int decrementCount = 5;
        
        Product product = createTestProduct(initialStock);
        Long productId = product.getId();
        
        for (int i = 0; i < decrementCount; i++) {
            boolean success = productService.decrementStock(productId);
            assertThat(success).as("第%d次扣减应成功", i + 1).isTrue();
        }
        
        Product afterDecrements = productMapper.selectById(productId);
        assertThat(afterDecrements.getStock())
            .as("扣减%d次后库存应为%d", decrementCount, initialStock - decrementCount)
            .isEqualTo(initialStock - decrementCount);
        
        for (int i = 0; i < decrementCount; i++) {
            boolean success = productService.incrementStock(productId);
            assertThat(success).as("第%d次回滚应成功", i + 1).isTrue();
        }
        
        Product afterRollbacks = productMapper.selectById(productId);
        assertThat(afterRollbacks.getStock())
            .as("回滚%d次后库存应恢复为初始值%d", decrementCount, initialStock)
            .isEqualTo(initialStock);
        
        productMapper.deleteById(productId);
    }
    
    @Test
    void deletedProductCannotRollback() {
        Product product = createTestProduct(5);
        Long productId = product.getId();
        
        product.setStatus(ProductStatus.DELETED);
        productMapper.updateById(product);
        
        boolean incrementSuccess = productService.incrementStock(productId);
        
        assertThat(incrementSuccess)
            .as("DELETED状态商品不应允许回滚库存")
            .isFalse();
        
        Product afterAttempt = productMapper.selectById(productId);
        assertThat(afterAttempt.getStock()).as("库存应保持不变").isEqualTo(5);
        
        productMapper.deleteById(productId);
    }
    
    private Product createTestProduct(int stock) {
        Product product = new Product();
        product.setSellerId(1L);
        product.setName("测试商品-回滚-" + System.currentTimeMillis());
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
