package com.cr.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Gateway service application.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class CrGatewayApplication {

    /**
     * Starts the gateway service.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(CrGatewayApplication.class, args);
    }
}
