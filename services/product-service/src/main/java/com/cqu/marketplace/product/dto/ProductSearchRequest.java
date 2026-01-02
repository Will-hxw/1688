package com.cqu.marketplace.product.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品搜索请求DTO
 */
@Data
public class ProductSearchRequest {
    
    /** 关键词（搜索名称和描述） */
    private String keyword;
    
    /** 分类 */
    private String category;
    
    /** 最低价格 */
    private BigDecimal minPrice;
    
    /** 最高价格 */
    private BigDecimal maxPrice;
    
    /** 排序字段（仅允许price/createdAt） */
    @Pattern(regexp = "^(price|createdAt)$", message = "排序字段仅允许price或createdAt")
    private String sortBy = "createdAt";
    
    /** 排序方向（仅允许asc/desc） */
    @Pattern(regexp = "^(asc|desc)$", message = "排序方向仅允许asc或desc")
    private String sortOrder = "desc";
    
    /** 页码 */
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;
    
    /** 每页大小 */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 100, message = "每页大小最大为100")
    private Integer pageSize = 10;
}
