package com.cqu.marketplace.product.controller.internal;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.product.dto.StockRequest;
import com.cqu.marketplace.product.entity.Product;
import com.cqu.marketplace.product.service.ProductService;
import com.cqu.marketplace.product.vo.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 内部商品API控制器
 * 仅供服务间调用，Gateway 会拦截外部对 /internal/** 的访问
 */
@Slf4j
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {
    
    private final ProductService productService;
    
    /**
     * 获取商品快照（用于订单创建）
     */
    @GetMapping("/{id}")
    public Result<ProductSnapshot> getProductSnapshot(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        
        ProductSnapshot snapshot = new ProductSnapshot();
        snapshot.setId(product.getId());
        snapshot.setSellerId(product.getSellerId());
        snapshot.setName(product.getName());
        snapshot.setPrice(product.getPrice());
        snapshot.setImageUrl(product.getImageUrl());
        snapshot.setStock(product.getStock());
        snapshot.setStatus(product.getStatus().getCode());
        
        return Result.success(snapshot);
    }
    
    /**
     * 扣减库存（原子操作）
     * @return 200 OK 或 409 Conflict
     */
    @PostMapping("/{id}/decrease-stock")
    public Result<Void> decreaseStock(
            @PathVariable Long id,
            @RequestBody(required = false) StockRequest request) {
        String orderId = request != null ? request.getOrderId() : "unknown";
        log.info("扣减库存请求: productId={}, orderId={}", id, orderId);
        
        boolean success = productService.decrementStock(id);
        if (success) {
            log.info("库存扣减成功: productId={}, orderId={}", id, orderId);
            return Result.success();
        }
        
        log.warn("库存扣减失败（库存不足或商品不可购买）: productId={}, orderId={}", id, orderId);
        throw BusinessException.conflict("库存不足或商品不可购买");
    }
    
    /**
     * 回滚库存（原子操作）
     * @return 200 OK 或 409 Conflict
     */
    @PostMapping("/{id}/increase-stock")
    public Result<Void> increaseStock(
            @PathVariable Long id,
            @RequestBody(required = false) StockRequest request) {
        String orderId = request != null ? request.getOrderId() : "unknown";
        log.info("回滚库存请求: productId={}, orderId={}", id, orderId);
        
        boolean success = productService.incrementStock(id);
        if (success) {
            log.info("库存回滚成功: productId={}, orderId={}", id, orderId);
            return Result.success();
        }
        
        log.warn("库存回滚失败: productId={}, orderId={}", id, orderId);
        throw BusinessException.conflict("库存回滚失败");
    }
}
