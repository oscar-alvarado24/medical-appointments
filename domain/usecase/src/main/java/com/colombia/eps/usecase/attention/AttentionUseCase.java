package com.colombia.eps.usecase.attention;

import com.colombia.eps.model.attention.Attention;
import com.colombia.eps.model.attention.gateways.AttentionRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;


@RequiredArgsConstructor
public class AttentionUseCase {
    private final AttentionRepository attentionRepository;

    public Mono<String> saveAttention(Attention attention) {
        return attentionRepository.saveAttention(attention);
    }
    public Mono<List<Attention>> getAttentions(String patientEmail) {return attentionRepository.getAttentions(patientEmail);}
    public Mono<Attention> getAttentionById(String id) {return attentionRepository.getAttentionById(id);}
}

