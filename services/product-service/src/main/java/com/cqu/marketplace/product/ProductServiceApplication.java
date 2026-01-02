package com.cqu.marketplace.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 商品服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.cqu.marketplace")
@EnableFeignClients
public class ProductServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
