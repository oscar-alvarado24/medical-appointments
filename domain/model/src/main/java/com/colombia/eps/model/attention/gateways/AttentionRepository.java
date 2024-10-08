package com.colombia.eps.model.attention.gateways;

import com.colombia.eps.model.attention.Attention;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AttentionRepository {
    Mono<String> saveAttention(Attention attention);
    Mono<List<Attention>> getAttentions(String patientEmail);
    Mono<Attention> getAttentionById(String id);
}
