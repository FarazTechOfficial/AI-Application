package com.aitutor.app.service;

import com.aitutor.app.dto.RecommendationDto;
import com.aitutor.app.dto.WeakTopicDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final MlServiceClient mlServiceClient;

    public List<RecommendationDto> getRecommendations(Long studentId, int topN) {
        return mlServiceClient.fetchRecommendations(studentId, topN);
    }

    public String getRecommendationsNarrative(Long studentId, int topN) {
        return mlServiceClient.fetchRecommendationsNarrative(studentId, topN);
    }

    public List<WeakTopicDto> getWeakTopics(Long studentId) {
        return mlServiceClient.fetchWeakTopics(studentId);
    }
}