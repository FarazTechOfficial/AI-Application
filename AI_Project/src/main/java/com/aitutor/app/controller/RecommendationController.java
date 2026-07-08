package com.aitutor.app.controller;

import com.aitutor.app.dto.RecommendationDto;
import com.aitutor.app.dto.WeakTopicDto;
import com.aitutor.app.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDto>> getRecommendations(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "5") int topN) {
        return ResponseEntity.ok(recommendationService.getRecommendations(studentId, topN));
    }

    @GetMapping("/weak-topics")
    public ResponseEntity<List<WeakTopicDto>> getWeakTopics(@PathVariable Long studentId) {
        return ResponseEntity.ok(recommendationService.getWeakTopics(studentId));
    }
}
