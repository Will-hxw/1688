package com.cqu.marketplace.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 分页结果
 * 格式: {page, pageSize, total, list}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    
    /** 当前页码 */
    private Integer page;
    
    /** 每页大小 */
    private Integer pageSize;
    
    /** 总记录数 */
    private Long total;
    
    /** 数据列表 */
    private List<T> list;
    
    /**
     * 构建分页结果
     */
    public static <T> PageResult<T> of(Integer page, Integer pageSize, Long total, List<T> list) {
        return new PageResult<>(page, pageSize, total, list);
    }
    
    /**
     * 从MyBatis-Plus的Page对象构建
     */
    public static <T> PageResult<T> from(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageResult<>(
            (int) page.getCurrent(),
            (int) page.getSize(),
            page.getTotal(),
            page.getRecords()
        );
    }
}
