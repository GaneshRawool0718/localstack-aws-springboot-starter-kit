package com.example.localstackpoc.model;

/**
 * Request body for sending a message to an SQS queue.
 */
public record MessageRequest(String body) {
}
