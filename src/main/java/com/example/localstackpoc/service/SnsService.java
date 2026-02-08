package com.example.localstackpoc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
import software.amazon.awssdk.services.sns.model.Topic;

import jakarta.annotation.PostConstruct;

/**
 * Service layer for SNS operations: publish notifications and manage subscriptions.
 * Resolves the topic ARN from the topic name at startup with a lazy fallback.
 */
@Service
public class SnsService {

    private static final Logger log = LoggerFactory.getLogger(SnsService.class);

    private final SnsClient snsClient;
    private final String topicName;
    private String topicArn;

    public SnsService(SnsClient snsClient,
                      @Value("${aws.sns.topic-name}") String topicName) {
        this.snsClient = snsClient;
        this.topicName = topicName;
    }

    /**
     * Resolves the topic ARN by listing existing topics and matching by name.
     */
    @PostConstruct
    public void init() {
        try {
            ListTopicsResponse response = snsClient.listTopics();
            topicArn = response.topics().stream()
                    .map(Topic::topicArn)
                    .filter(arn -> arn.endsWith(":" + topicName))
                    .findFirst()
                    .orElse(null);

            if (topicArn != null) {
                log.info("Resolved SNS topic ARN: {}", topicArn);
            } else {
                log.warn("SNS topic '{}' not found — will resolve on first use", topicName);
            }
        } catch (Exception e) {
            log.warn("Could not resolve SNS topic at startup: {}", e.getMessage());
        }
    }

    /**
     * Publishes a message to the configured SNS topic.
     *
     * @param subject the notification subject
     * @param message the notification message body
     * @return the SNS-assigned message ID
     */
    public String publish(String subject, String message) {
        ensureTopicArn();
        PublishResponse response = snsClient.publish(PublishRequest.builder()
                .topicArn(topicArn)
                .subject(subject)
                .message(message)
                .build());
        log.info("Published message to topic '{}', messageId: {}", topicName, response.messageId());
        return response.messageId();
    }

    /**
     * Subscribes an email address to the topic.
     *
     * @param email the email address to subscribe
     * @return the subscription ARN
     */
    public String subscribeEmail(String email) {
        ensureTopicArn();
        SubscribeResponse response = snsClient.subscribe(SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("email")
                .endpoint(email)
                .build());
        log.info("Subscribed email '{}' to topic '{}'", email, topicName);
        return response.subscriptionArn();
    }

    /**
     * Subscribes an SQS queue to the topic (fan-out pattern).
     *
     * @param queueArn the ARN of the SQS queue to subscribe
     * @return the subscription ARN
     */
    public String subscribeSqs(String queueArn) {
        ensureTopicArn();
        SubscribeResponse response = snsClient.subscribe(SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("sqs")
                .endpoint(queueArn)
                .build());
        log.info("Subscribed SQS queue '{}' to topic '{}'", queueArn, topicName);
        return response.subscriptionArn();
    }

    /**
     * Lazy fallback: creates the topic if not found (idempotent — returns existing ARN if it exists).
     */
    private void ensureTopicArn() {
        if (topicArn == null) {
            CreateTopicResponse response = snsClient.createTopic(
                    CreateTopicRequest.builder().name(topicName).build());
            topicArn = response.topicArn();
            log.info("Lazily resolved/created SNS topic ARN: {}", topicArn);
        }
    }
}
