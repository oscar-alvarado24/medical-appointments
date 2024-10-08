package com.colombia.eps.dynamodb.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String PATIENT_EMAIL_INDEX = "patient_email_index";
    public static final String TABLE_NAME = "attention";
    public static final String SAVE_SUCCESSFULLY = "Atencion guardada correctamente";
    public static final String MSG_SAVE_FAILED = "Fallo el proceso de guardar la atencion";
    public static final String DYNAMO_ROL = "DYNAMO_ROL";
    public static final String ROLE_SESSION_NAME_DYNAMO =  "dynamo-conn";
    public static final String MSG_FAIL_GET_ATTENTION_BY_ID = "Fallo el proceso de obtener la atencion por id: {}";
    public static final String MSG_FAIL_GET_ATTENTION_BY_EMAIL = "Fallo el proceso de obtener la atencion por email: {}";
}

