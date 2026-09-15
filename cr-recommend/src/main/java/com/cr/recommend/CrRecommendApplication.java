package com.cr.recommend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Recommendation service application.
 */
@SpringBootApplication(scanBasePackages = "com.cr")
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@MapperScan("com.cr.recommend.mapper")
public class CrRecommendApplication {

    /**
     * Starts the recommendation service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CrRecommendApplication.class, args);
    }
}
