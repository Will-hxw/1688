package com.cqu.marketplace.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 统一响应结果
 * 格式: {code, message, data, timestamp}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    /** 响应码，与HTTP状态码一致 */
    private Integer code;
    
    /** 响应消息 */
    private String message;
    
    /** 响应数据 */
    private T data;
    
    /** 时间戳 */
    private Long timestamp;
    
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /** 成功响应 */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "OK", data);
    }
    
    /** 成功响应（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(200, "OK", null);
    }
    
    /** 成功响应（自定义消息） */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    /** 失败响应 */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    /** 401 认证失败 */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }
    
    /** 403 权限不足 */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }
    
    /** 404 资源不存在 */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }
    
    /** 409 业务冲突 */
    public static <T> Result<T> conflict(String message) {
        return new Result<>(409, message, null);
    }
}
