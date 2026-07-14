package com.aitutor.app.service;

import com.aitutor.app.dto.InteractionRequestDto;
import com.aitutor.app.dto.InteractionResponseDto;
import com.aitutor.app.entity.Interaction;
import com.aitutor.app.mapper.Mapper;
import com.aitutor.app.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final Mapper mapper;
    private final MlServiceClient mlServiceClient;

    public InteractionResponseDto recordInteraction(Long studentId, InteractionRequestDto request) {
        log.info("recordInteraction called: studentId={}, topicId={}, score={}, attempts={}",
                studentId, request.getTopicId(), request.getScore(), request.getAttempts());
        Interaction saved = interactionRepository.save(mapper.toEntity(studentId, request));

        try {
            mlServiceClient.pushInteraction(studentId, mapper.toMlPayload(saved));
        } catch (Exception e) {
            log.warn("ML service sync failed for interaction {}: {}", saved.getId(), e.getMessage());
        }

        return mapper.toDto(saved);
    }

    public List<InteractionResponseDto> getHistory(Long studentId) {
        return interactionRepository.findByStudentId(studentId).stream().map(mapper::toDto).toList();
    }
}
