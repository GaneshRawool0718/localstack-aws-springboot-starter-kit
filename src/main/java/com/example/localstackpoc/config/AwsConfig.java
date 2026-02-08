package com.example.localstackpoc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

/**
 * Configures AWS SDK v2 client beans with environment-driven settings.
 *
 * The key switching mechanism: when {@code aws.endpoint-url} is set (non-blank),
 * all clients point to that endpoint (LocalStack). When it is empty or absent,
 * clients use the default AWS endpoints (production).
 *
 * No code changes are needed to switch environments — only environment variables.
 */
@Configuration
public class AwsConfig {

    private static final Logger log = LoggerFactory.getLogger(AwsConfig.class);

    @Value("${aws.region}")
    private String region;

    @Value("${aws.endpoint-url}")
    private String endpointUrl;

    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());

        // LocalStack requires path-style access for S3 (bucket name in the path, not subdomain)
        if (isEndpointOverridden()) {
            builder.endpointOverride(URI.create(endpointUrl))
                   .forcePathStyle(true);
            log.info("S3Client configured with endpoint override: {} (path-style enabled)", endpointUrl);
        }

        return builder.build();
    }

    @Bean
    public SqsClient sqsClient() {
        var builder = SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());

        if (isEndpointOverridden()) {
            builder.endpointOverride(URI.create(endpointUrl));
            log.info("SqsClient configured with endpoint override: {}", endpointUrl);
        }

        return builder.build();
    }

    @Bean
    public DynamoDbClient dynamoDbClient() {
        var builder = DynamoDbClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());

        if (isEndpointOverridden()) {
            builder.endpointOverride(URI.create(endpointUrl));
            log.info("DynamoDbClient configured with endpoint override: {}", endpointUrl);
        }

        return builder.build();
    }

    @Bean
    public SnsClient snsClient() {
        var builder = SnsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider());

        if (isEndpointOverridden()) {
            builder.endpointOverride(URI.create(endpointUrl));
            log.info("SnsClient configured with endpoint override: {}", endpointUrl);
        }

        return builder.build();
    }

    /**
     * Returns true when an endpoint URL override is configured (LocalStack mode).
     * When false, SDK clients connect to real AWS endpoints.
     */
    private boolean isEndpointOverridden() {
        return endpointUrl != null && !endpointUrl.isBlank();
    }

    /**
     * Creates a static credentials provider from environment-configured values.
     * LocalStack accepts any credentials (defaults to "test"/"test").
     * For real AWS, set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY to IAM credentials.
     */
    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
        );
    }
}
