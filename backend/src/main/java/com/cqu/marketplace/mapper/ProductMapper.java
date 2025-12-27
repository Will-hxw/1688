package com.cqu.marketplace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqu.marketplace.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 商品Mapper
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 原子更新商品状态（用于下单时锁定库存）
     * @param productId 商品ID
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @return 更新行数
     */
    @Update("UPDATE product SET status = #{toStatus}, updated_at = NOW() " +
            "WHERE id = #{productId} AND status = #{fromStatus}")
    int updateStatusAtomic(@Param("productId") Long productId, 
                           @Param("fromStatus") String fromStatus, 
                           @Param("toStatus") String toStatus);
    
    /**
     * 原子扣减库存（库存-1，若库存变为0则状态变为SOLD）
     * @param productId 商品ID
     * @return 更新行数（0表示库存不足或商品不可购买）
     */
    @Update("UPDATE product SET stock = stock - 1, " +
            "status = CASE WHEN stock - 1 = 0 THEN 'SOLD' ELSE status END, " +
            "updated_at = NOW() " +
            "WHERE id = #{productId} AND stock > 0 AND status = 'ON_SALE'")
    int decrementStock(@Param("productId") Long productId);
    
    /**
     * 原子增加库存（库存+1，若商品状态为SOLD则恢复为ON_SALE）
     * @param productId 商品ID
     * @return 更新行数
     */
    @Update("UPDATE product SET stock = stock + 1, " +
            "status = 'ON_SALE', " +
            "updated_at = NOW() " +
            "WHERE id = #{productId} AND status IN ('ON_SALE', 'SOLD')")
    int incrementStock(@Param("productId") Long productId);
}
