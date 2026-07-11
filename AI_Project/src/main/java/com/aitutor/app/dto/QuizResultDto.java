package com.aitutor.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private int correctCount;
    private int totalQuestions;
    private double score;
    private String status;
}
