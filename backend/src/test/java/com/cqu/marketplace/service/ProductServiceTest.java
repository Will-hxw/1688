package com.cqu.marketplace.service;

import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.ProductStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.dto.product.ProductCreateRequest;
import com.cqu.marketplace.dto.product.ProductSearchRequest;
import com.cqu.marketplace.dto.product.ProductUpdateRequest;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.impl.ProductServiceImpl;
import com.cqu.marketplace.vo.product.ProductVO;
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 商品服务测试
 * 验证: Property 3, 4, 4.1, 5, 6, 7
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private UserMapper userMapper;
    
    @InjectMocks
    private ProductServiceImpl productService;
    
    private Product testProduct;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setSellerId(1L);
        testProduct.setName("测试商品");
        testProduct.setDescription("测试描述");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setImageUrl("/uploads/test.jpg");
        testProduct.setCategory("数码");
        testProduct.setStatus(ProductStatus.ON_SALE);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("测试用户");
    }
    
    @Test
    @DisplayName("创建商品成功 - 状态为ON_SALE - 验证Property 3")
    void createProduct_Success_StatusOnSale() {
        // 准备
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("新商品");
        request.setDescription("描述");
        request.setPrice(new BigDecimal("100"));
        request.setImageUrl("/uploads/new.jpg");
        request.setCategory("书籍");
        
        when(productMapper.insert(any())).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(1L);
            // 验证状态为ON_SALE
            assertEquals(ProductStatus.ON_SALE, p.getStatus());
            return 1;
        });
        
        // 执行
        Long productId = productService.createProduct(1L, request);
        
        // 验证
        assertNotNull(productId);
        verify(productMapper).insert(any(Product.class));
    }
    
    @Test
    @DisplayName("编辑商品成功 - 所有者且ON_SALE状态 - 验证Property 4")
    void updateProduct_Success_OwnerAndOnSale() {
        // 准备
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("更新名称");
        request.setPrice(new BigDecimal("199.99"));
        
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        when(productMapper.updateById(any())).thenReturn(1);
        
        // 执行
        assertDoesNotThrow(() -> productService.updateProduct(1L, 1L, request));
        
        // 验证
        verify(productMapper).updateById(any(Product.class));
    }
    
    @Test
    @DisplayName("编辑商品失败 - 非所有者 - 验证Property 4")
    void updateProduct_Fail_NotOwner() {
        // 准备
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("更新名称");
        
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> productService.updateProduct(1L, 2L, request)); // 用户ID=2，非所有者
        assertEquals(403, exception.getCode());
    }
    
    @Test
    @DisplayName("编辑商品失败 - SOLD状态 - 验证Property 4")
    void updateProduct_Fail_SoldStatus() {
        // 准备
        testProduct.setStatus(ProductStatus.SOLD);
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("更新名称");
        
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> productService.updateProduct(1L, 1L, request));
        assertEquals(409, exception.getCode());
    }
    
    @Test
    @DisplayName("删除商品成功 - ON_SALE状态 - 验证Property 4.1")
    void deleteProduct_Success_OnSaleStatus() {
        // 准备
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        when(productMapper.updateById(any())).thenReturn(1);
        
        // 执行
        assertDoesNotThrow(() -> productService.deleteProduct(1L, 1L));
        
        // 验证状态变为DELETED
        verify(productMapper).updateById(argThat(p -> 
            ((Product) p).getStatus() == ProductStatus.DELETED));
    }
    
    @Test
    @DisplayName("删除商品失败 - SOLD状态 - 验证Property 4.1")
    void deleteProduct_Fail_SoldStatus() {
        // 准备
        testProduct.setStatus(ProductStatus.SOLD);
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> productService.deleteProduct(1L, 1L));
        assertEquals(409, exception.getCode());
    }
    
    @Test
    @DisplayName("删除商品失败 - 非所有者 - 验证Property 4.1")
    void deleteProduct_Fail_NotOwner() {
        // 准备
        when(productMapper.selectById(1L)).thenReturn(testProduct);
        
        // 执行 & 验证
        BusinessException exception = assertThrows(BusinessException.class,
            () -> productService.deleteProduct(1L, 2L));
        assertEquals(403, exception.getCode());
    }
}
