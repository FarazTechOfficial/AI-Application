package com.aitutor.app.mapper;

import com.aitutor.app.dto.*;
import com.aitutor.app.entity.Interaction;
import com.aitutor.app.entity.Student;
import com.aitutor.app.entity.Topic;
import com.aitutor.app.util.ScoreClassifier;
import org.springframework.stereotype.Component;

@Component
public class Mapper {

    public StudentDto toDto(Student s) {
        return new StudentDto(s.getId(), s.getName(), s.getEmail(), s.getGradeLevel());
    }

    public Student toEntity(StudentCreateRequest req) {
        return Student.builder().name(req.getName()).email(req.getEmail()).gradeLevel(req.getGradeLevel()).build();
    }

    public TopicDto toDto(Topic t) {
        return new TopicDto(t.getId(), t.getSubject(), t.getName(), t.getDifficulty(), t.getPrerequisiteId());
    }

    public Interaction toEntity(Long studentId, InteractionRequestDto req) {
        return Interaction.builder()
                .studentId(studentId)
                .topicId(req.getTopicId())
                .score(req.getScore())
                .attempts(req.getAttempts() == null ? 1 : req.getAttempts())
                .build();
    }

    public InteractionResponseDto toDto(Interaction i) {
        return new InteractionResponseDto(
                i.getId(), i.getStudentId(), i.getTopicId(), i.getScore(), i.getAttempts(),
                ScoreClassifier.classify(i.getScore()).name(), i.getRecordedAt()
        );
    }

    public MlInteractionPayload toMlPayload(Interaction i) {
        return new MlInteractionPayload(i.getTopicId(), i.getScore(), i.getAttempts());
    }
}
