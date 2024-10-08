package com.colombia.eps.dynamodb;

import com.colombia.eps.dynamodb.helper.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttentionEntity {

    private String id;
    private String patientEmail;
    @Getter
    private String doctorName;
    @Getter
    private String reasonConsult;
    @Getter
    private String diagnostic;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
    public String getId() {
        return id;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {Constants.PATIENT_EMAIL_INDEX})
    @DynamoDbAttribute("patientEmail")
    public String getPatientEmail(){
        return patientEmail;
    }
}
