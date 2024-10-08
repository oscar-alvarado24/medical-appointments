package com.colombia.eps.api;

import com.colombia.eps.model.attention.Attention;
import com.colombia.eps.usecase.attention.AttentionUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class Handler {
private  final AttentionUseCase attentionUseCase;

    public Mono<ServerResponse> saveAttention(ServerRequest request) {
        return request.bodyToMono(Attention.class)
                .flatMap(this::validateAttention)
                .flatMap(attention -> attentionUseCase.saveAttention(attention)
                        .flatMap(result -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue(result))
                        .onErrorResume(e -> ServerResponse.badRequest()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue(e.getMessage()))
                );
    }

    private Mono<Attention> validateAttention(Attention attention) {
        if (attention.getPatientEmail() == null || attention.getPatientEmail().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Patient email is required"));
        }
        return Mono.just(attention);
    }
    public Mono<ServerResponse> getAttention(ServerRequest request) {
        String patientEmail = request.pathVariable("patientEmail");
        return attentionUseCase.getAttentions(patientEmail)
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue("Error processing request: " + e.getMessage()));
    }

    public Mono<ServerResponse> getAttentionById(ServerRequest serverRequest) {
        String attentionId = serverRequest.pathVariable("id");
        return attentionUseCase.getAttentionById(attentionId)
                .flatMap(result -> ServerResponse.ok().bodyValue(result))
                .onErrorResume(e -> ServerResponse.badRequest().bodyValue("Error processing request: " + e.getMessage()));
    }
}
