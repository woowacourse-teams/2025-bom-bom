package me.bombom.api.v1.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class LambdaClientConfig {

    @Value("${aws.lambda.playwright.region}")
    private String region;

    @Value("${aws.lambda.playwright.access-key}")
    private String accessKey;

    @Value("${aws.lambda.playwright.secret-key}")
    private String secretKey;

    @Bean(destroyMethod = "close")
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
