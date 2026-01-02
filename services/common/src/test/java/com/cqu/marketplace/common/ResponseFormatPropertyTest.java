package com.cqu.marketplace.common;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 响应格式一致性属性测试
 * 
 * Feature: microservices-migration
 * Property 11: 响应格式一致性
 * Validates: Requirements 9.1, 9.2
 * 
 * 验证所有 API 响应符合统一格式：
 * - Result: {code, message, data, timestamp}
 * - PageResult: {page, pageSize, total, list}
 */
class ResponseFormatPropertyTest {

    // ==================== Property 11.1: Result 格式一致性 ====================

    /**
     * Property 11.1.1: 成功响应格式
     * For any 数据对象，Result.success() 应返回包含 code=200、message="OK"、data、timestamp 的响应
     */
    @Property(tries = 100)
    void successResultShouldHaveCorrectFormat(
            @ForAll @StringLength(min = 0, max = 100) String data) {
        
        Result<String> result = Result.success(data);
        
        // 验证格式字段存在且正确
        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getData()).isEqualTo(data);
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getTimestamp()).isPositive();
        assertThat(result.getTimestamp()).isLessThanOrEqualTo(System.currentTimeMillis());
    }

    /**
     * Property 11.1.2: 错误响应格式
     * For any 错误码和消息，Result.error() 应返回包含正确 code、message、data=null、timestamp 的响应
     */
    @Property(tries = 100)
    void errorResultShouldHaveCorrectFormat(
            @ForAll @IntRange(min = 400, max = 599) int code,
            @ForAll @StringLength(min = 1, max = 200) String message) {
        
        Result<Object> result = Result.error(code, message);
        
        // 验证格式字段存在且正确
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getMessage()).isEqualTo(message);
        assertThat(result.getData()).isNull();
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.getTimestamp()).isPositive();
    }

    /**
     * Property 11.1.3: 特定错误码响应格式
     * For any 消息，unauthorized/forbidden/notFound/conflict 应返回对应的 HTTP 状态码
     */
    @Property(tries = 100)
    void specificErrorResultsShouldHaveCorrectCodes(
            @ForAll @StringLength(min = 1, max = 100) String message) {
        
        // 401 未认证
        Result<Object> unauthorized = Result.unauthorized(message);
        assertThat(unauthorized.getCode()).isEqualTo(401);
        assertThat(unauthorized.getMessage()).isEqualTo(message);
        assertThat(unauthorized.getData()).isNull();
        assertThat(unauthorized.getTimestamp()).isNotNull();
        
        // 403 权限不足
        Result<Object> forbidden = Result.forbidden(message);
        assertThat(forbidden.getCode()).isEqualTo(403);
        assertThat(forbidden.getMessage()).isEqualTo(message);
        assertThat(forbidden.getData()).isNull();
        assertThat(forbidden.getTimestamp()).isNotNull();
        
        // 404 资源不存在
        Result<Object> notFound = Result.notFound(message);
        assertThat(notFound.getCode()).isEqualTo(404);
        assertThat(notFound.getMessage()).isEqualTo(message);
        assertThat(notFound.getData()).isNull();
        assertThat(notFound.getTimestamp()).isNotNull();
        
        // 409 业务冲突
        Result<Object> conflict = Result.conflict(message);
        assertThat(conflict.getCode()).isEqualTo(409);
        assertThat(conflict.getMessage()).isEqualTo(message);
        assertThat(conflict.getData()).isNull();
        assertThat(conflict.getTimestamp()).isNotNull();
    }

    /**
     * Property 11.1.4: 时间戳单调递增
     * For any 连续创建的 Result，后创建的时间戳应大于等于先创建的
     */
    @Property(tries = 50)
    void resultTimestampsShouldBeMonotonicallyIncreasing() {
        Result<String> first = Result.success("first");
        Result<String> second = Result.success("second");
        
        assertThat(second.getTimestamp()).isGreaterThanOrEqualTo(first.getTimestamp());
    }

    // ==================== Property 11.2: PageResult 格式一致性 ====================

    /**
     * Property 11.2.1: 分页响应格式
     * For any 分页参数和数据列表，PageResult 应包含 page、pageSize、total、list 字段
     */
    @Property(tries = 100)
    void pageResultShouldHaveCorrectFormat(
            @ForAll @IntRange(min = 1, max = 1000) int page,
            @ForAll @IntRange(min = 1, max = 100) int pageSize,
            @ForAll @LongRange(min = 0, max = 10000) long total) {
        
        List<String> list = Arrays.asList("item1", "item2", "item3");
        PageResult<String> result = PageResult.of(page, pageSize, total, list);
        
        // 验证格式字段存在且正确
        assertThat(result.getPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getTotal()).isEqualTo(total);
        assertThat(result.getList()).isEqualTo(list);
    }

    /**
     * Property 11.2.2: 空列表分页响应
     * For any 分页参数，空列表的 PageResult 应正确处理
     */
    @Property(tries = 100)
    void emptyPageResultShouldHaveCorrectFormat(
            @ForAll @IntRange(min = 1, max = 1000) int page,
            @ForAll @IntRange(min = 1, max = 100) int pageSize) {
        
        List<String> emptyList = List.of();
        PageResult<String> result = PageResult.of(page, pageSize, 0L, emptyList);
        
        assertThat(result.getPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getTotal()).isEqualTo(0L);
        assertThat(result.getList()).isEmpty();
    }

    /**
     * Property 11.2.3: 分页参数边界值
     * For any 边界分页参数，PageResult 应正确处理
     */
    @Property(tries = 50)
    void pageResultShouldHandleBoundaryValues(
            @ForAll @IntRange(min = 1, max = Integer.MAX_VALUE) int page,
            @ForAll @IntRange(min = 1, max = Integer.MAX_VALUE) int pageSize,
            @ForAll @LongRange(min = 0, max = Long.MAX_VALUE) long total) {
        
        List<Integer> list = List.of(1, 2, 3);
        PageResult<Integer> result = PageResult.of(page, pageSize, total, list);
        
        // 验证边界值正确存储
        assertThat(result.getPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getTotal()).isEqualTo(total);
        assertThat(result.getList()).hasSize(3);
    }

    // ==================== Property 11.3: 响应格式不变性 ====================

    /**
     * Property 11.3.1: Result 字段不可变性
     * For any Result 对象，通过 getter 获取的值应与构造时一致
     */
    @Property(tries = 100)
    void resultFieldsShouldBeConsistent(
            @ForAll @IntRange(min = 100, max = 599) int code,
            @ForAll @StringLength(min = 1, max = 100) String message,
            @ForAll @StringLength(min = 0, max = 100) String data) {
        
        long beforeTimestamp = System.currentTimeMillis();
        Result<String> result = new Result<>(code, message, data);
        long afterTimestamp = System.currentTimeMillis();
        
        // 验证字段一致性
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getMessage()).isEqualTo(message);
        assertThat(result.getData()).isEqualTo(data);
        assertThat(result.getTimestamp()).isBetween(beforeTimestamp, afterTimestamp);
    }

    /**
     * Property 11.3.2: PageResult 字段不可变性
     * For any PageResult 对象，通过 getter 获取的值应与构造时一致
     */
    @Property(tries = 100)
    void pageResultFieldsShouldBeConsistent(
            @ForAll @IntRange(min = 1, max = 1000) int page,
            @ForAll @IntRange(min = 1, max = 100) int pageSize,
            @ForAll @LongRange(min = 0, max = 10000) long total) {
        
        List<String> list = List.of("a", "b", "c");
        PageResult<String> result = new PageResult<>(page, pageSize, total, list);
        
        // 验证字段一致性
        assertThat(result.getPage()).isEqualTo(page);
        assertThat(result.getPageSize()).isEqualTo(pageSize);
        assertThat(result.getTotal()).isEqualTo(total);
        assertThat(result.getList()).isEqualTo(list);
    }
}
