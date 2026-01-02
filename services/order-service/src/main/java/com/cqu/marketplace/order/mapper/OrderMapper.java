package com.cqu.marketplace.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqu.marketplace.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 订单Mapper
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    
    /**
     * 原子更新订单状态
     * 使用条件更新确保状态机正确性
     * 
     * @param orderId 订单ID
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @return 更新行数
     */
    @Update("UPDATE `order` SET status = #{toStatus}, updated_at = NOW() " +
            "WHERE id = #{orderId} AND status = #{fromStatus}")
    int updateStatusAtomic(@Param("orderId") Long orderId,
                          @Param("fromStatus") String fromStatus,
                          @Param("toStatus") String toStatus);
    
    /**
     * 原子更新订单状态（带取消方和取消时间）
     * 
     * @param orderId 订单ID
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @param canceledBy 取消方
     * @return 更新行数
     */
    @Update("UPDATE `order` SET status = #{toStatus}, canceled_by = #{canceledBy}, " +
            "canceled_at = NOW(), updated_at = NOW() " +
            "WHERE id = #{orderId} AND status = #{fromStatus}")
    int updateStatusWithCanceledBy(@Param("orderId") Long orderId,
                                   @Param("fromStatus") String fromStatus,
                                   @Param("toStatus") String toStatus,
                                   @Param("canceledBy") String canceledBy);
}
