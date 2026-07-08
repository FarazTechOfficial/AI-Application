package com.aitutor.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteractionResponseDto {
    private Long id;
    private Long studentId;
    private Long topicId;
    private Double score;
    private Integer attempts;
    private String status;       // MASTERED / IN_PROGRESS / WEAK - from ScoreClassifier
    private LocalDateTime recordedAt;
}
