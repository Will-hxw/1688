package com.cqu.marketplace.order.client;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.order.dto.StockRequest;
import com.cqu.marketplace.order.vo.ProductSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Product Service Feign 客户端
 * 使用占位符支持环境变量覆盖
 */
@FeignClient(name = "product-service", url = "${services.product.url}")
public interface ProductClient {
    
    /**
     * 获取商品快照
     */
    @GetMapping("/internal/products/{id}")
    Result<ProductSnapshot> getProductSnapshot(@PathVariable("id") Long productId);
    
    /**
     * 扣减库存
     */
    @PostMapping("/internal/products/{id}/decrease-stock")
    Result<Void> decreaseStock(@PathVariable("id") Long productId,
                               @RequestBody StockRequest request);
    
    /**
     * 回滚库存
     */
    @PostMapping("/internal/products/{id}/increase-stock")
    Result<Void> increaseStock(@PathVariable("id") Long productId,
                               @RequestBody StockRequest request);
}
