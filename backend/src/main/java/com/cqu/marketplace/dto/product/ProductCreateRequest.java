package com.cqu.marketplace.dto.product;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品创建请求DTO
 */
@Data
public class ProductCreateRequest {
    
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 100, message = "商品名称不能超过100字符")
    private String name;
    
    @Size(max = 2000, message = "商品描述不能超过2000字符")
    private String description;
    
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    @DecimalMax(value = "999999.99", message = "价格不能超过999999.99")
    private BigDecimal price;
    
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;
    
    @Size(max = 50, message = "分类不能超过50字符")
    private String category;
}
