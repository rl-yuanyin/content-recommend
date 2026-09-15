package com.cr.notification;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * Notification service application.
 */
@SpringBootApplication(scanBasePackages = "com.cr")
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.cr.notification.mapper")
public class CrNotificationApplication {

    /**
     * Starts the notification service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CrNotificationApplication.class, args);
    }

    /**
     * Configures the MyBatis-Plus pagination plugin.
     *
     * @return MyBatis-Plus interceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
