package com.cqu.marketplace.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 评价创建请求DTO
 */
@Data
public class ReviewCreateRequest {
    
    @NotNull(message = "订单ID不能为空")
    private Long orderId;
    
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1")
    @Max(value = 5, message = "评分最高为5")
    private Integer rating;
    
    @Size(max = 500, message = "评价内容不能超过500字符")
    private String content;
}
