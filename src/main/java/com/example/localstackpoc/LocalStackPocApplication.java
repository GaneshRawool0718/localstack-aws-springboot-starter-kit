package com.example.localstackpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the LocalStack POC application.
 * Demonstrates using AWS services (S3, SQS, DynamoDB, SNS) locally via
 * LocalStack with environment-driven configuration for zero-code switching
 * between local and production environments.
 */
@SpringBootApplication
public class LocalStackPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalStackPocApplication.class, args);
    }
}
