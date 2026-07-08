package com.aitutor.app.controller;

import com.aitutor.app.dto.InteractionRequestDto;
import com.aitutor.app.dto.InteractionResponseDto;
import com.aitutor.app.service.InteractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students/{studentId}/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping
    public ResponseEntity<InteractionResponseDto> recordInteraction(
            @PathVariable Long studentId,
            @Valid @RequestBody InteractionRequestDto request) {
        InteractionResponseDto response = interactionService.recordInteraction(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InteractionResponseDto>> getHistory(@PathVariable Long studentId) {
        return ResponseEntity.ok(interactionService.getHistory(studentId));
    }
}
