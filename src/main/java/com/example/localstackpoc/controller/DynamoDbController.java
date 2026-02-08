package com.example.localstackpoc.controller;

import com.example.localstackpoc.model.Item;
import com.example.localstackpoc.service.DynamoDbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for DynamoDB item operations.
 * Provides endpoints for creating, retrieving, and listing items in the DynamoDB table.
 */
@RestController
@RequestMapping("/api/dynamodb")
public class DynamoDbController {

    private final DynamoDbService dynamoDbService;

    public DynamoDbController(DynamoDbService dynamoDbService) {
        this.dynamoDbService = dynamoDbService;
    }

    /**
     * Create or overwrite an item in the DynamoDB table.
     * Example: curl -X POST http://localhost:8080/api/dynamodb/items
     *   -H "Content-Type: application/json"
     *   -d '{"id": "1", "name": "Test", "description": "A test item"}'
     */
    @PostMapping("/items")
    public ResponseEntity<Item> putItem(@RequestBody Item item) {
        return ResponseEntity.ok(dynamoDbService.putItem(item));
    }

    /**
     * Retrieve an item by its partition key (id).
     * Example: curl http://localhost:8080/api/dynamodb/items/1
     */
    @GetMapping("/items/{id}")
    public ResponseEntity<Item> getItem(@PathVariable String id) {
        Item item = dynamoDbService.getItem(id);
        if (item == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(item);
    }

    /**
     * List all items in the DynamoDB table.
     * Example: curl http://localhost:8080/api/dynamodb/items
     */
    @GetMapping("/items")
    public ResponseEntity<List<Item>> listItems() {
        return ResponseEntity.ok(dynamoDbService.listItems());
    }
}
