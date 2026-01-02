package com.cqu.marketplace.review.client;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.review.vo.ProductInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Product Service Feign 客户端
 */
@FeignClient(name = "product-service", url = "${services.product.url}")
public interface ProductClient {
    
    /**
     * 获取商品信息
     */
    @GetMapping("/internal/products/{id}")
    Result<ProductInfo> getProductInfo(@PathVariable("id") Long productId);
}
