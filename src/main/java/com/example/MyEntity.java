package com.example;


import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Introspected
@DynamoDbImmutable(builder = MyEntity.MyEntityBuilder.class)
@Value
@Builder
public class MyEntity {
    public static final String NAME_INDEX = "name";

    @lombok.NonNull
    @Getter(onMethod_ = @DynamoDbPartitionKey)
    String id;

    @lombok.NonNull
    @Getter(onMethod_ = @DynamoDbSecondaryPartitionKey(indexNames = NAME_INDEX))
    String name;
}
