package com.cqu.marketplace.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 评价服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.cqu.marketplace")
@EnableFeignClients
public class ReviewServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApplication.class, args);
    }
}
