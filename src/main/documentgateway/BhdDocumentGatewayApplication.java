package com.bhd.documentgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories(basePackages = "com.bhd.documentgateway.infrastructure.persistence")
public class BhdDocumentGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BhdDocumentGatewayApplication.class, args);
    }
}
