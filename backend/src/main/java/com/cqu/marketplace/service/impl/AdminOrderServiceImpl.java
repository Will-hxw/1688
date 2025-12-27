package com.cqu.marketplace.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cqu.marketplace.common.PageResult;
import com.cqu.marketplace.common.enums.OrderStatus;
import com.cqu.marketplace.common.exception.BusinessException;
import com.cqu.marketplace.entity.Order;
import com.cqu.marketplace.entity.Product;
import com.cqu.marketplace.entity.User;
import com.cqu.marketplace.mapper.OrderMapper;
import com.cqu.marketplace.mapper.ProductMapper;
import com.cqu.marketplace.mapper.UserMapper;
import com.cqu.marketplace.service.AdminOrderService;
import com.cqu.marketplace.vo.order.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderServiceImpl implements AdminOrderService {
    
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    
    @Override
    public PageResult<OrderVO> listOrders(Integer page, Integer pageSize) {
        Page<Order> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Order::getCreatedAt);
        
        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        
        List<OrderVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return PageResult.of(page, pageSize, result.getTotal(), voList);
    }
    
    @Override
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        
        // 验证状态转换是否合法
        if (!isValidTransition(order.getStatus(), newStatus)) {
            throw new BusinessException(409, "非法的状态转换: " + order.getStatus() + " -> " + newStatus);
        }
        
        order.setStatus(newStatus);
        orderMapper.updateById(order);
        
        log.info("管理员修改订单状态: orderId={}, newStatus={}", orderId, newStatus);
    }
    
    /**
     * 验证状态转换是否合法（遵循状态机）
     */
    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        // 定义合法的状态转换
        return switch (from) {
            case CREATED -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELED;
            case SHIPPED -> to == OrderStatus.RECEIVED || to == OrderStatus.CANCELED;
            case RECEIVED -> to == OrderStatus.REVIEWED;
            case CANCELED, REVIEWED -> false; // 终态不能转换
        };
    }
    
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setProductId(order.getProductId());
        vo.setPrice(order.getPrice());
        vo.setStatus(order.getStatus().name());
        vo.setCreatedAt(order.getCreatedAt());
        
        if (order.getCanceledBy() != null) {
            vo.setCanceledBy(order.getCanceledBy().name());
        }
        
        // 获取买家信息
        User buyer = userMapper.selectById(order.getBuyerId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }
        
        // 获取卖家信息
        User seller = userMapper.selectById(order.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        
        // 获取商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImageUrl());
        }
        
        return vo;
    }
}
