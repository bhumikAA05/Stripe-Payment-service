package com.stripe.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class StripeProviderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StripeProviderServiceApplication.class, args);
    }
}
