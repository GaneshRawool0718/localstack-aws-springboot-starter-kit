package com.example.localstackpoc.service;

import com.example.localstackpoc.model.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for DynamoDB operations: put, get, and list items.
 * Uses the low-level DynamoDbClient with AttributeValue maps to demonstrate
 * raw SDK usage (no Enhanced Client or Spring Data abstractions).
 */
@Service
public class DynamoDbService {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbService.class);

    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public DynamoDbService(DynamoDbClient dynamoDbClient,
                           @Value("${aws.dynamodb.table-name}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    /**
     * Puts (creates or overwrites) an item in the DynamoDB table.
     *
     * @param item the item to store
     * @return the stored item
     */
    public Item putItem(Item item) {
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", AttributeValue.builder().s(item.id()).build());
        itemMap.put("name", AttributeValue.builder().s(item.name()).build());
        itemMap.put("description", AttributeValue.builder().s(item.description()).build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(itemMap)
                .build());

        log.info("Put item with id '{}' into table '{}'", item.id(), tableName);
        return item;
    }

    /**
     * Retrieves an item by its partition key (id).
     *
     * @param id the partition key value
     * @return the item, or null if not found
     */
    public Item getItem(String id) {
        Map<String, AttributeValue> key = Map.of(
                "id", AttributeValue.builder().s(id).build()
        );

        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build());

        if (!response.hasItem() || response.item().isEmpty()) {
            log.info("Item with id '{}' not found in table '{}'", id, tableName);
            return null;
        }

        Map<String, AttributeValue> attrs = response.item();
        Item item = new Item(
                attrs.get("id").s(),
                getStringAttribute(attrs, "name"),
                getStringAttribute(attrs, "description")
        );
        log.info("Retrieved item with id '{}' from table '{}'", id, tableName);
        return item;
    }

    /**
     * Scans and returns all items in the DynamoDB table.
     * Note: scan reads every item — acceptable for a POC, but use Query in production.
     *
     * @return list of all items
     */
    public List<Item> listItems() {
        ScanResponse response = dynamoDbClient.scan(ScanRequest.builder()
                .tableName(tableName)
                .build());

        List<Item> items = response.items().stream()
                .map(attrs -> new Item(
                        attrs.get("id").s(),
                        getStringAttribute(attrs, "name"),
                        getStringAttribute(attrs, "description")
                ))
                .toList();

        log.info("Scanned {} items from table '{}'", items.size(), tableName);
        return items;
    }

    /**
     * Safely extracts a string attribute from a DynamoDB item map.
     * Returns an empty string if the attribute is missing.
     */
    private String getStringAttribute(Map<String, AttributeValue> attrs, String key) {
        AttributeValue value = attrs.get(key);
        return (value != null && value.s() != null) ? value.s() : "";
    }
}
