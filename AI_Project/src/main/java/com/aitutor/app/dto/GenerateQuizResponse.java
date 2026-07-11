package com.aitutor.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQuizResponse {
    private Long quizId;
    private String topicName;
    private List<QuestionDto> questions;
}
