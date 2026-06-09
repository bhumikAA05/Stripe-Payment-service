package com.stripe.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Base64;

@Configuration
public class HttpServiceEngine {

    @Value("${processing.service.url}")
    private String processingServiceUrl;

    @Value("${processing.service.username}")
    private String username;

    @Value("${processing.service.password}")
    private String password;

    @Bean
    public RestClient processingRestClient() {
        String credentials = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());

        return RestClient.builder()
                .baseUrl(processingServiceUrl)
                .defaultHeader("Authorization", "Basic " + credentials)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
