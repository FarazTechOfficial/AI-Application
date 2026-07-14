package com.aitutor.app.service;

import com.aitutor.app.dto.MlInteractionPayload;
import com.aitutor.app.dto.NarrativeDto;
import com.aitutor.app.dto.RecommendationDto;
import com.aitutor.app.dto.WeakTopicDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class MlServiceClient {

    private final WebClient webClient;

    public MlServiceClient(WebClient mlServiceWebClient) {
        this.webClient = mlServiceWebClient;
    }

    public List<RecommendationDto> fetchRecommendations(Long studentId, int topN) {
        return webClient.get()
                .uri(b -> b.path("/students/{id}/recommendations").queryParam("top_n", topN).build(studentId))
                .retrieve()
                .bodyToFlux(RecommendationDto.class)
                .collectList()
                .block();
    }

    public String fetchRecommendationsNarrative(Long studentId, int topN) {
        NarrativeDto response = webClient.get()
                .uri(b -> b.path("/students/{id}/recommendations-narrative").queryParam("top_n", topN).build(studentId))
                .retrieve()
                .bodyToMono(NarrativeDto.class)
                .block();
        return response == null ? null : response.getNarrative();
    }

    public List<WeakTopicDto> fetchWeakTopics(Long studentId) {
        return webClient.get()
                .uri("/students/{id}/weak-topics", studentId)
                .retrieve()
                .bodyToFlux(WeakTopicDto.class)
                .collectList()
                .block();
    }

    public void pushInteraction(Long studentId, MlInteractionPayload payload) {
        webClient.post()
                .uri("/students/{id}/interactions", studentId)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}