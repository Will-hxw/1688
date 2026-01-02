package com.cqu.marketplace.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqu.marketplace.review.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评价Mapper
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
