package com.example.localstackpoc.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Service layer for SQS operations: send and receive messages.
 * Resolves the queue URL from the queue name at startup with a lazy fallback
 * in case LocalStack resources are not yet created when the app boots.
 */
@Service
public class SqsService {

    private static final Logger log = LoggerFactory.getLogger(SqsService.class);

    private final SqsClient sqsClient;
    private final String queueName;
    private String queueUrl;

    public SqsService(SqsClient sqsClient,
                      @Value("${aws.sqs.queue-name}") String queueName) {
        this.sqsClient = sqsClient;
        this.queueName = queueName;
    }

    /**
     * Resolves the queue URL from the queue name at startup.
     * SQS operations require the URL, not the name.
     */
    @PostConstruct
    public void init() {
        try {
            this.queueUrl = sqsClient.getQueueUrl(
                    GetQueueUrlRequest.builder().queueName(queueName).build()
            ).queueUrl();
            log.info("Resolved SQS queue URL: {}", queueUrl);
        } catch (QueueDoesNotExistException e) {
            log.warn("SQS queue '{}' does not exist yet — will resolve on first use", queueName);
        }
    }

    /**
     * Sends a message to the configured SQS queue.
     *
     * @param messageBody the message content
     * @return the SQS-assigned message ID
     */
    public String sendMessage(String messageBody) {
        ensureQueueUrl();
        SendMessageResponse response = sqsClient.sendMessage(
                SendMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .messageBody(messageBody)
                        .build()
        );
        log.info("Sent message to queue '{}', messageId: {}", queueName, response.messageId());
        return response.messageId();
    }

    /**
     * Receives up to 10 messages from the configured SQS queue.
     * Uses long polling (5 seconds) for efficient message retrieval.
     *
     * @return list of received messages
     */
    public List<Message> receiveMessages() {
        ensureQueueUrl();
        List<Message> messages = sqsClient.receiveMessage(
                ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(5)
                        .build()
        ).messages();
        log.info("Received {} messages from queue '{}'", messages.size(), queueName);
        return messages;
    }

    /**
     * Lazy fallback: resolves the queue URL if it was not available at startup.
     */
    private void ensureQueueUrl() {
        if (queueUrl == null) {
            this.queueUrl = sqsClient.getQueueUrl(
                    GetQueueUrlRequest.builder().queueName(queueName).build()
            ).queueUrl();
            log.info("Lazily resolved SQS queue URL: {}", queueUrl);
        }
    }
}
