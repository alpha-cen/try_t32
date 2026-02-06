package com.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Application Class for Auth User Service
 * A microservice for authentication and user management using AWS Cognito
 */
@SpringBootApplication
public class AuthUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthUserServiceApplication.class, args);
    }
}
