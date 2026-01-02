package com.cqu.marketplace.order.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.order.entity.Order;
import com.cqu.marketplace.order.mapper.OrderMapper;
import com.cqu.marketplace.order.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员订单控制器
 */
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {
    
    private final OrderMapper orderMapper;
    
    /**
     * 获取订单列表（分页）
     */
    @GetMapping
    public Result<PageResult<OrderVO>> listOrders(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long buyerId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        // 权限校验
        if (!"ADMIN".equals(role)) {
            throw BusinessException.forbidden("无权访问");
        }
        
        Page<Order> pageObj = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, OrderStatus.valueOf(status));
        }
        if (buyerId != null) {
            wrapper.eq(Order::getBuyerId, buyerId);
        }
        if (sellerId != null) {
            wrapper.eq(Order::getSellerId, sellerId);
        }
        wrapper.orderByDesc(Order::getCreatedAt);
        
        Page<Order> result = orderMapper.selectPage(pageObj, wrapper);
        
        List<OrderVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return Result.success(PageResult.of(page, pageSize, result.getTotal(), voList));
    }
    
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setProductId(order.getProductId());
        vo.setProductName(order.getProductName());
        vo.setProductImage(order.getProductImage());
        vo.setPrice(order.getPrice());
        vo.setStatus(order.getStatus().getCode());
        vo.setCreatedAt(order.getCreatedAt());
        
        if (order.getCanceledBy() != null) {
            vo.setCanceledBy(order.getCanceledBy().getCode());
            vo.setCanceledAt(order.getCanceledAt());
        }
        
        return vo;
    }
}
