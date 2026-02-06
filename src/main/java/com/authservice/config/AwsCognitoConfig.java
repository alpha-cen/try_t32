package com.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

/**
 * AWS Cognito Configuration
 * Configures AWS Cognito client and related properties
 */
@Configuration
public class AwsCognitoConfig {

    @Value("${aws.cognito.region:us-east-1}")
    private String region;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Value("${aws.cognito.client-secret:}")
    private String clientSecret;

    @Value("${aws.cognito.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public String getUserPoolId() {
        return userPoolId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRegion() {
        return region;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }
}
