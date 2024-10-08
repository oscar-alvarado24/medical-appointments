package com.colombia.eps.api.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String ENDPOINT_GET_BY_EMAIL = "/api/attention/email/{patientEmail}";
    public static final String ENDPOINT_SAVE_ATTENTION = "/api/attention/save";
    public static final String ENDPOINT_GET_BY_ID = "/api/attention/id/{id}";
}
