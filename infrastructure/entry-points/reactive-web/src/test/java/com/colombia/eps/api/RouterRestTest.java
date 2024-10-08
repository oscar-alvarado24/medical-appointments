package com.colombia.eps.api;

import com.colombia.eps.api.helper.Constants;
import com.colombia.eps.model.attention.Attention;
import com.colombia.eps.usecase.attention.AttentionUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {RouterRest.class, Handler.class})
@WebFluxTest
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private AttentionUseCase attentionUseCase;
    private Attention attention;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        attention = new Attention();
        attention.setId("2024-10-08-7:20-email@email.com");
        attention.setPatientEmail("email@email.com");
        attention.setDoctorName("Pedro Perez");
        attention.setReasonConsult("dolor abdominal");
        attention.setDiagnostic("gastitris");
    }
    @Test
    void testListenGETUseCase() {
        List<Attention> mockAttentions = List.of(attention);
        when(attentionUseCase.getAttentions("email@email.com")).thenReturn(Mono.just(mockAttentions));

        webTestClient.get()
                .uri(Constants.ENDPOINT_GET_BY_EMAIL, attention.getPatientEmail())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Attention.class)
                .hasSize(1)
                .consumeWith(response -> {
                    List<Attention> responseBody = response.getResponseBody();
                    Assertions.assertThat(responseBody).isNotNull();
                    Assertions.assertThat(responseBody).hasSize(1);
                    Assertions.assertThat(responseBody.get(0).getPatientEmail()).isEqualTo(attention.getPatientEmail());
                });
        verify(attentionUseCase).getAttentions(attention.getPatientEmail());
    }

    @Test
    void testListenPOSTUseCase() {
        when(attentionUseCase.saveAttention(any(Attention.class)))
                .thenReturn(Mono.just("Success"));

        webTestClient.post()
                .uri(Constants.ENDPOINT_SAVE_ATTENTION)
                .contentType(MediaType.APPLICATION_JSON)  // Cambia a application/json
                .bodyValue(attention)  // Envía el objeto Attention
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_PLAIN)
                .expectBody(String.class)
                .isEqualTo("Success");
    }

    @Test
    void testListenGetByIdUseCase() {
        when(attentionUseCase.getAttentionById("2024-10-08-7:20-email@email.com")).thenReturn(Mono.just(attention));
        webTestClient.get()
                .uri(Constants.ENDPOINT_GET_BY_ID,"2024-10-08-7:20-email@email.com")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo(attention.getId())
                .jsonPath("$.patientEmail").isEqualTo(attention.getPatientEmail());
    }
}
