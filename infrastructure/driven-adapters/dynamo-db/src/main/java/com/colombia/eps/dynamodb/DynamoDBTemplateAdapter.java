package com.colombia.eps.dynamodb;

import com.colombia.eps.dynamodb.helper.Constants;
import com.colombia.eps.dynamodb.helper.TemplateAdapterOperations;
import com.colombia.eps.model.attention.Attention;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;


@Repository
@Slf4j
public class DynamoDBTemplateAdapter extends TemplateAdapterOperations<Attention,String, AttentionEntity > {

    public DynamoDBTemplateAdapter(DynamoDbEnhancedAsyncClient connectionFactory, ObjectMapper mapper) {

        super(connectionFactory, mapper, d -> mapper.map(d, Attention.class), Constants.TABLE_NAME, Constants.PATIENT_EMAIL_INDEX);
    }


    public QueryEnhancedRequest generateQueryExpression(String valueToSearch) {
        return QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(valueToSearch).build()))
                .build();
    }
}
