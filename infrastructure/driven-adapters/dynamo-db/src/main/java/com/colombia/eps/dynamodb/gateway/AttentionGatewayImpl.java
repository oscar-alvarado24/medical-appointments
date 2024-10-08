package com.colombia.eps.dynamodb.gateway;

import com.colombia.eps.dynamodb.DynamoDBTemplateAdapter;
import com.colombia.eps.dynamodb.exception.AttentionSaveException;
import com.colombia.eps.dynamodb.helper.Constants;
import com.colombia.eps.model.attention.Attention;
import com.colombia.eps.model.attention.gateways.AttentionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.util.List;

@AllArgsConstructor
@Slf4j
@Component
public class AttentionGatewayImpl implements AttentionRepository {

    private final DynamoDBTemplateAdapter templateAdapter;
    private final DynamoDbAsyncClient dynamoDbAsyncClient;
    /**
     *
     * @param patientEmail
     * @return
     */
    @Override
    public Mono<List<Attention>> getAttentions(String patientEmail) {
        QueryEnhancedRequest queryExpression = templateAdapter.generateQueryExpression(patientEmail);
        return templateAdapter.queryByIndex(queryExpression, Constants.PATIENT_EMAIL_INDEX)
                .onErrorMap(error -> {
                    log.error(Constants.MSG_FAIL_GET_ATTENTION_BY_EMAIL, error.getMessage());
                    return new AttentionSaveException(Constants.MSG_FAIL_GET_ATTENTION_BY_EMAIL, error);
                });
    }

    /**
     * @param id
     * @return
     */
    @Override
    public Mono<Attention> getAttentionById(String id) {
        return templateAdapter.getById(id)
                .onErrorMap(error -> {
                    log.error(Constants.MSG_FAIL_GET_ATTENTION_BY_ID, error.getMessage());
                    return new AttentionSaveException(Constants.MSG_FAIL_GET_ATTENTION_BY_ID, error);
                });
    }

    /**
     * @param attention
     * @return
     */
    @Override
    public Mono<String> saveAttention(Attention attention) {
        return templateAdapter.save(attention, dynamoDbAsyncClient)
                .map(savedAttention -> Constants.SAVE_SUCCESSFULLY)
                .onErrorMap(error -> {
                    log.error(Constants.MSG_SAVE_FAILED, error.getMessage());
                    return new AttentionSaveException(Constants.MSG_SAVE_FAILED, error);
                });
    }
}
