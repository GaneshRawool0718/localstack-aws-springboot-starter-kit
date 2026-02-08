package com.example.localstackpoc.controller;

import com.example.localstackpoc.model.NotificationRequest;
import com.example.localstackpoc.service.SnsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for SNS notification operations.
 * Provides endpoints for publishing messages and managing topic subscriptions.
 */
@RestController
@RequestMapping("/api/sns")
public class SnsController {

    private final SnsService snsService;

    public SnsController(SnsService snsService) {
        this.snsService = snsService;
    }

    /**
     * Publish a notification to the SNS topic.
     * Example: curl -X POST http://localhost:8080/api/sns/publish
     *   -H "Content-Type: application/json"
     *   -d '{"subject": "Test", "message": "Hello from SNS!"}'
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, String>> publish(@RequestBody NotificationRequest request) {
        String messageId = snsService.publish(request.subject(), request.message());
        return ResponseEntity.ok(Map.of(
                "messageId", messageId,
                "status", "published"
        ));
    }

    /**
     * Subscribe an email address to the SNS topic.
     * Example: curl -X POST http://localhost:8080/api/sns/subscribe/email
     *   -H "Content-Type: application/json"
     *   -d '{"email": "user@example.com"}'
     */
    @PostMapping("/subscribe/email")
    public ResponseEntity<Map<String, String>> subscribeEmail(@RequestBody Map<String, String> request) {
        String subscriptionArn = snsService.subscribeEmail(request.get("email"));
        return ResponseEntity.ok(Map.of("subscriptionArn", subscriptionArn));
    }

    /**
     * Subscribe an SQS queue to the SNS topic (fan-out pattern).
     * Example: curl -X POST http://localhost:8080/api/sns/subscribe/sqs
     *   -H "Content-Type: application/json"
     *   -d '{"queueArn": "arn:aws:sqs:us-east-1:000000000000:poc-queue"}'
     */
    @PostMapping("/subscribe/sqs")
    public ResponseEntity<Map<String, String>> subscribeSqs(@RequestBody Map<String, String> request) {
        String subscriptionArn = snsService.subscribeSqs(request.get("queueArn"));
        return ResponseEntity.ok(Map.of("subscriptionArn", subscriptionArn));
    }
}
