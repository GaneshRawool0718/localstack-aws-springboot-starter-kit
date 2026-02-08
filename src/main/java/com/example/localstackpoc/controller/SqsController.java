package com.example.localstackpoc.controller;

import com.example.localstackpoc.model.MessageRequest;
import com.example.localstackpoc.service.SqsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Map;

/**
 * REST controller for SQS messaging operations.
 * Provides endpoints for sending and receiving messages from the SQS queue.
 */
@RestController
@RequestMapping("/api/sqs")
public class SqsController {

    private final SqsService sqsService;

    public SqsController(SqsService sqsService) {
        this.sqsService = sqsService;
    }

    /**
     * Send a message to the SQS queue.
     * Example: curl -X POST http://localhost:8080/api/sqs/send
     *   -H "Content-Type: application/json"
     *   -d '{"body": "Hello from SQS!"}'
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody MessageRequest request) {
        String messageId = sqsService.sendMessage(request.body());
        return ResponseEntity.ok(Map.of(
                "messageId", messageId,
                "status", "sent"
        ));
    }

    /**
     * Receive messages from the SQS queue (long-polls for up to 5 seconds).
     * Example: curl http://localhost:8080/api/sqs/receive
     */
    @GetMapping("/receive")
    public ResponseEntity<List<Map<String, String>>> receiveMessages() {
        List<Message> messages = sqsService.receiveMessages();
        // Map SDK Message objects to plain maps to avoid leaking SDK types in the API response
        List<Map<String, String>> result = messages.stream()
                .map(m -> Map.of(
                        "messageId", m.messageId(),
                        "body", m.body()
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}
