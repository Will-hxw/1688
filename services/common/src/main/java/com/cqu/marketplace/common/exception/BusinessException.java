package com.cqu.marketplace.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于处理业务逻辑中的异常情况
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /** HTTP状态码 */
    private final Integer code;
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    /** 409 业务冲突 */
    public static BusinessException conflict(String message) {
        return new BusinessException(409, message);
    }
    
    /** 404 资源不存在 */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
    
    /** 403 权限不足 */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }
    
    /** 401 认证失败 */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }
    
    /** 400 参数错误 */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }
}
