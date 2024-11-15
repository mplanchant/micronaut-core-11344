package com.example;

import jakarta.inject.Singleton;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@Singleton
public class MyEntityRepository {

    private final DynamoDbTable<MyEntity> myTable;

    public MyEntityRepository(DynamoDbTable<MyEntity> myTable) {
        this.myTable = myTable;
    }

    public void persist(MyEntity myEntity) {
        myTable.putItem(myEntity);
    }
}
