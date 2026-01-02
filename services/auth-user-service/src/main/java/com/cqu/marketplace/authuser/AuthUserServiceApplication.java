package com.cqu.marketplace.authuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证用户服务启动类
 */
@SpringBootApplication(scanBasePackages = {
    "com.cqu.marketplace.authuser",
    "com.cqu.marketplace.common"
})
@MapperScan("com.cqu.marketplace.authuser.mapper")
public class AuthUserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AuthUserServiceApplication.class, args);
    }
}
