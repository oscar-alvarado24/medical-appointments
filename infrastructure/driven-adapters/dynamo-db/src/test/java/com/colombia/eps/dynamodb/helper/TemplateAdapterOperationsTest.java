package com.colombia.eps.dynamodb.helper;

import com.colombia.eps.dynamodb.AttentionEntity;
import com.colombia.eps.dynamodb.DynamoDBTemplateAdapter;
import com.colombia.eps.dynamodb.config.DynamoDBConfig;
import com.colombia.eps.dynamodb.gateway.AttentionGatewayImpl;
import com.colombia.eps.model.attention.Attention;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {DynamoDBTemplateAdapter.class, DynamoDBConfig.class})
@ActiveProfiles("test")
class TemplateAdapterOperationsTest {

    @MockBean
    private MetricPublisher metricPublisher;

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Mock
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    @MockBean
    private ObjectMapper mapper;

    @Mock
    private DynamoDbAsyncTable<AttentionEntity> customerTable;

    @Mock
    private DynamoDbAsyncIndex<AttentionEntity> index;
    @Mock
    private SdkPublisher<Page<AttentionEntity>> mockPublisher;

    private AttentionEntity attentionEntity;
    private Attention attention;
    private DynamoDBTemplateAdapter dynamoDBTemplateAdapter;
    private AttentionGatewayImpl attentionGateway;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        attentionEntity = new AttentionEntity();
        attentionEntity.setId("2024-10-08-7:20-email@email.com");
        attentionEntity.setPatientEmail("email@email.com");
        attentionEntity.setDoctorName("Pedro Perez");
        attentionEntity.setReasonConsult("dolor abdominal");
        attentionEntity.setDiagnostic("gastitris");

        attention = new Attention();
        attention.setId("2024-10-08-7:20-email@email.com");
        attention.setPatientEmail("email@email.com");
        attention.setDoctorName("Pedro Perez");
        attention.setReasonConsult("dolor abdominal");
        attention.setDiagnostic("gastitris");

        when(dynamoDbEnhancedAsyncClient.<AttentionEntity>table(
                anyString(),
                any()
        )).thenReturn(customerTable);

        // Mock para tableExists
        when(dynamoDbAsyncClient.describeTable(any(DescribeTableRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        DescribeTableResponse.builder()
                                .table(TableDescription.builder()
                                        .tableStatus(TableStatus.ACTIVE)
                                        .build())
                                .build()));



        // Mocks para el mapper
        when(mapper.map(attention, AttentionEntity.class)).thenReturn(attentionEntity);
        when(mapper.map(attentionEntity, Attention.class)).thenReturn(attention);
        dynamoDBTemplateAdapter =
                new DynamoDBTemplateAdapter(dynamoDbEnhancedAsyncClient, mapper);
        attentionGateway= new AttentionGatewayImpl(dynamoDBTemplateAdapter,dynamoDbAsyncClient);
    }

    @Test
    void modelEntityPropertiesMustNotBeNull() {
        AttentionEntity attentionEntityUnderTest = new AttentionEntity("2024-10-08-7:20-email@email.com","email@email.com","Pedro Perez","dolor abdominal","gastitris");

        assertNotNull(attentionEntityUnderTest.getId());
        assertNotNull(attentionEntityUnderTest.getPatientEmail());
        assertNotNull(attentionEntityUnderTest.getDoctorName());
        assertNotNull(attentionEntityUnderTest.getReasonConsult());
        assertNotNull(attentionEntityUnderTest.getDiagnostic());
    }

    @Test
    void testSave() {
        // Given

        // When & Then
        doReturn(CompletableFuture.completedFuture(attention))
                .when(customerTable)
                .putItem(any(AttentionEntity.class));


        // Then
        StepVerifier.create(dynamoDBTemplateAdapter.save(attention,dynamoDbAsyncClient))
                .expectNext(attention)
                .verifyComplete();
    }

    @Test
    void testGetById() {
        String id = "2024-10-08-7:20-email@email.com";

        when(customerTable.getItem(
                Key.builder().partitionValue(AttributeValue.builder().s(id).build()).build()))
                .thenReturn(CompletableFuture.completedFuture(attentionEntity));

        StepVerifier.create(dynamoDBTemplateAdapter.getById(id))
                .expectNext(attention)
                .verifyComplete();
    }
    @Test
    void testGetByEmail() {
        String email = "email@email.com";
        Page<AttentionEntity> mockPage = Page.builder(AttentionEntity.class)
                .items(List.of(attentionEntity))
                .build();

        this.mockPublisher = SdkPublisher.adapt(Flux.just(mockPage));
        QueryEnhancedRequest queryExpression = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(email).build()))
                .build();

        when(customerTable.index(Constants.PATIENT_EMAIL_INDEX)).thenReturn(index);
        when(index.query(queryExpression)).thenReturn(mockPublisher);

        StepVerifier.create(attentionGateway.getAttentions(email))
                .expectSubscription()
                .expectNext(List.of(attention))
                .verifyComplete();

    }
}