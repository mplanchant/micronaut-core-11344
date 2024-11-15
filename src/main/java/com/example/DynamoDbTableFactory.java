package com.example;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Factory
public class DynamoDbTableFactory {

    @Singleton
    public DynamoDbEnhancedClient enhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Singleton
    public DynamoDbTable<MyEntity> myTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("my-table", TableSchema.fromImmutableClass(MyEntity.class));
    }

}
