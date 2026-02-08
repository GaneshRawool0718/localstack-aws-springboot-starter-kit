package com.example.localstackpoc.model;

/**
 * Represents a DynamoDB item with a partition key (id), name, and description.
 * Uses a Java record for immutable, concise DTO representation.
 */
public record Item(String id, String name, String description) {
}
