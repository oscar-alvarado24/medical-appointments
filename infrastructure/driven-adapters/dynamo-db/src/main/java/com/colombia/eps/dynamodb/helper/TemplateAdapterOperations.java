package com.colombia.eps.dynamodb.helper;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.lang.reflect.ParameterizedType;
import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@Component
public abstract class TemplateAdapterOperations<E, K, V> {
    private final Class<V> dataClass;
    private final Function<V, E> toEntityFn;
    protected ObjectMapper mapper;
    private final DynamoDbAsyncTable<V> table;
    private final DynamoDbAsyncIndex<V> tableByIndex;

    @SuppressWarnings("unchecked")
    protected TemplateAdapterOperations(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                                        ObjectMapper mapper,
                                        Function<V, E> toEntityFn,
                                        String tableName,
                                        String... index) {
        this.toEntityFn = toEntityFn;
        this.mapper = mapper;
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.dataClass = (Class<V>) genericSuperclass.getActualTypeArguments()[2];
        table = dynamoDbEnhancedAsyncClient.table(tableName, TableSchema.fromBean(dataClass));
        tableByIndex = index.length > 0 ? table.index(index[0]) : null;
    }

    public Mono<E> save(E model, DynamoDbAsyncClient dynamoDbAsyncClient) {
        return tableExists(dynamoDbAsyncClient)
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return createTable(model, dynamoDbAsyncClient);  // Usa el asyncClient para crear la tabla
                    }
                    return saveInTable(model);
                });

    }

    public Mono<E> getById(K id) {
        return Mono.fromFuture(table.getItem(Key.builder()
                        .partitionValue(AttributeValue.builder().s((String) id).build())
                        .build()))
                .map(this::toModel);
    }

    public Mono<List<E>> queryByIndex(QueryEnhancedRequest queryExpression, String... index) {
        DynamoDbAsyncIndex<V> queryIndex = index.length > 0 ? table.index(index[0]) : tableByIndex;

        SdkPublisher<Page<V>> pagePublisher = queryIndex.query(queryExpression);
        return listOfModel(pagePublisher);
    }


    protected Mono<List<E>> listOfModel(SdkPublisher<Page<V>> pagePublisher) {
        return Mono.from(pagePublisher).map(page -> page.items().stream().map(this::toModel).toList());
    }

    private Mono<Boolean> tableExists(DynamoDbAsyncClient asyncClient) {
        return Mono.fromFuture(() ->
                        asyncClient.describeTable(DescribeTableRequest.builder()
                                .tableName(Constants.TABLE_NAME)
                                .build()))
                .map(response -> response.table().tableStatusAsString().equals("ACTIVE"))
                .onErrorResume(ResourceNotFoundException.class, ex -> Mono.just(false));
    }

    private Mono<E> createTable(E model, DynamoDbAsyncClient asyncClient) {
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(Constants.TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("id") // Clave primaria de partición
                                .keyType(KeyType.HASH) // HASH para clave primaria
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("id")
                                .attributeType(ScalarAttributeType.S) // S para String
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("patientEmail")
                                .attributeType(ScalarAttributeType.S) // S para el índice secundario
                                .build()
                )
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(Constants.PATIENT_EMAIL_INDEX) // Nombre del índice
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("patientEmail") // Clave del índice secundario
                                                .keyType(KeyType.HASH) // HASH para el índice
                                                .build()
                                )
                                .projection(p -> p.projectionType("ALL")) // Proyección de todos los atributos
                                .build()
                )
                .billingMode(BillingMode.PAY_PER_REQUEST) // Modo de facturación
                .build();

        return Mono.fromFuture(() ->
                asyncClient.createTable(createTableRequest)
        ).then(saveWithRetry(model, Duration.ofSeconds(6L)));
    }

    private Mono<E> saveWithRetry(E model, Duration initialDelay) {
        return Mono.defer(() -> saveInTable(model))
                .retryWhen(Retry.backoff(3, initialDelay)
                        .filter(ResourceNotFoundException.class::isInstance)
                );
    }

    private Mono<E> saveInTable(E model) {
        return Mono.fromFuture(table.putItem(toEntity(model))).thenReturn(model);
    }

    protected V toEntity(E model) {
        return mapper.map(model, dataClass);
    }

    protected E toModel(V data) {
        return data != null ? toEntityFn.apply(data) : null;
    }
}