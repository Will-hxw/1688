package com.cqu.marketplace.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品更新请求DTO
 */
@Data
public class ProductUpdateRequest {
    
    @Size(max = 100, message = "商品名称不能超过100字符")
    private String name;
    
    @Size(max = 2000, message = "商品描述不能超过2000字符")
    private String description;
    
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "9999999999999999.99", message = "价格不能超过9999999999999999.99")
    private BigDecimal price;
    
    private String imageUrl;
    
    @Size(max = 50, message = "分类不能超过50字符")
    private String category;
    
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 9999, message = "库存不能超过9999")
    private Integer stock;
}
