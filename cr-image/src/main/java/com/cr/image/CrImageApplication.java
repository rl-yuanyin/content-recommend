package com.cr.image;

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
 * Image service application.
 */
@SpringBootApplication(scanBasePackages = "com.cr")
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.cr.image.mapper")
public class CrImageApplication {

    /**
     * Starts the image service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CrImageApplication.class, args);
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
