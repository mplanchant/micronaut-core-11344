package com.example;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.CreateTableEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.DYNAMODB;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MyEntityRepositoryTest {

    @Inject
    private MyEntityRepository myEntityRepository;

    private static final LocalStackContainer LOCAL_STACK_CONTAINER =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest")).withServices(DYNAMODB);

    @Inject
    private DynamoDbTable<MyEntity> myTable;

    @Inject
    private DynamoDbClient dynamoDbClient;

    @Singleton
    public static class DynamoDbClientBuilderListener implements BeanCreatedEventListener<DynamoDbClientBuilder> {

        @Override
        public DynamoDbClientBuilder onCreated(BeanCreatedEvent<DynamoDbClientBuilder> event) {
            if (!LOCAL_STACK_CONTAINER.isRunning()) {
                LOCAL_STACK_CONTAINER.start();
            }

            return event.getBean().endpointOverride(LOCAL_STACK_CONTAINER.getEndpointOverride(DYNAMODB))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create("dummy", "dummy")
                    ));
        }
    }

    @BeforeEach
    void setUp() {
        // Should match table created in infrastructure/dynamodb.tf
        if (!tableExists()) {
            myTable.createTable(
                    CreateTableEnhancedRequest.builder()
                            .globalSecondaryIndices(
                                    EnhancedGlobalSecondaryIndex.builder()
                                            .indexName(MyEntity.NAME_INDEX)
                                            .projection(p -> p.projectionType(ProjectionType.ALL))
                                            .build())
                            .build());

            dynamoDbClient.waiter().waitUntilTableExists(b -> b.tableName(myTable.tableName()));
        }
    }

    @Test
    void retrieving_by_commerce_id_behaves_as_expected_when_customer_exists() {
        var customer = MyEntity.builder()
                .id("f106e3f5-2902-4a63-95a5-6933143c8f1f")
                .name("Bob")
                .build();

        myEntityRepository.persist(customer);
    }

    @AfterEach
    void tearDown() {
        // Delete table after each test
        try {
            myTable.deleteTable();
        } catch (ResourceNotFoundException e) {
            // nothing to do
        }
    }

    private boolean tableExists() {
        try {
            myTable.describeTable();
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}
