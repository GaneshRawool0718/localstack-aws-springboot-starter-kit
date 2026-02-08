package com.example.localstackpoc.model;

/**
 * Request body for publishing a notification to an SNS topic.
 */
public record NotificationRequest(String subject, String message) {
}
