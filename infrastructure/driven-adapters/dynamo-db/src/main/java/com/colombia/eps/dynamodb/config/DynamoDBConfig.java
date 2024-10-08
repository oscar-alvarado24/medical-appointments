package com.colombia.eps.dynamodb.config;

import com.colombia.eps.dynamodb.helper.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import static com.colombia.eps.library.GenerateSession.generateSession;

@Configuration
@Slf4j
public class DynamoDBConfig {


    @Bean
    @Profile({"test"})
    public DynamoDbAsyncClient amazonDynamoDBConfigTest(MetricPublisher publisher) {

        String dynamoRole = System.getenv(Constants.DYNAMO_ROL);
        StaticCredentialsProvider credential = generateSession(dynamoRole, Constants.ROLE_SESSION_NAME_DYNAMO);
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(credential)
                .region(Region.US_EAST_1)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addMetricPublisher(publisher)
                        .build())
                .build();
    }
    @Bean
    @Profile({"local"})
    public DynamoDbAsyncClient amazonDynamoDB(@Value("${aws.region}") String region,
                                              MetricPublisher publisher) {

                String dynamoRole = System.getenv(Constants.DYNAMO_ROL);
        StaticCredentialsProvider credential = generateSession(dynamoRole, Constants.ROLE_SESSION_NAME_DYNAMO);
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(credential)
                .region(Region.of(region))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .addMetricPublisher(publisher)
                        .build())
                .build();
    }


    @Bean
    @Profile({"dev", "cer", "pdn"})
    public DynamoDbAsyncClient amazonDynamoDBAsync(MetricPublisher publisher, @Value("${aws.region}") String region) {
        return DynamoDbAsyncClient.builder()
                .credentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .region(Region.of(region))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .build();
    }

    @Bean
    public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient(DynamoDbAsyncClient client) {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(client)
                .build();
    }
}
