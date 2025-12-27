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
}
