package com.aitutor.app.controller;

import com.aitutor.app.dto.GenerateQuizResponse;
import com.aitutor.app.dto.QuizResultDto;
import com.aitutor.app.dto.SubmitAnswersRequest;
import com.aitutor.app.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students/{studentId}/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    public ResponseEntity<GenerateQuizResponse> generateQuiz(
            @PathVariable Long studentId,
            @RequestParam Long topicId,
            @RequestParam(defaultValue = "5") int numQuestions) {
        return ResponseEntity.ok(quizService.generateQuiz(studentId, topicId, numQuestions));
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizResultDto> submitQuiz(
            @PathVariable Long studentId,
            @PathVariable Long quizId,
            @Valid @RequestBody SubmitAnswersRequest request) {
        return ResponseEntity.ok(quizService.submitQuiz(quizId, request));
    }
}
