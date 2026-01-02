package com.cqu.marketplace.order.config;

import com.cqu.marketplace.common.Result;
import com.cqu.marketplace.common.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Feign 错误解码器
 * 根据 HTTP 状态码分流处理
 */
@Slf4j
@Component
public class FeignErrorDecoder implements ErrorDecoder {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        int status = response.status();
        String message = extractMessage(response);
        
        log.warn("Feign调用失败: method={}, status={}, message={}", methodKey, status, message);
        
        return switch (status) {
            case 404 -> BusinessException.notFound(message != null ? message : "资源不存在");
            case 409 -> BusinessException.conflict(message != null ? message : "业务冲突");
            case 503 -> new BusinessException(503, "服务暂时不可用");
            default -> new BusinessException(status, message != null ? message : "服务调用失败");
        };
    }
    
    private String extractMessage(Response response) {
        try {
            if (response.body() == null) {
                return null;
            }
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            Result<?> result = objectMapper.readValue(body, Result.class);
            return result.getMessage();
        } catch (Exception e) {
            log.debug("解析Feign响应失败", e);
            return null;
        }
    }
}
