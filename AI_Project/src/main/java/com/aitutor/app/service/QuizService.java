package com.aitutor.app.service;

import com.aitutor.app.dto.*;
import com.aitutor.app.entity.QuizSession;
import com.aitutor.app.entity.Student;
import com.aitutor.app.entity.Topic;
import com.aitutor.app.repository.QuizSessionRepository;
import com.aitutor.app.repository.StudentRepository;
import com.aitutor.app.repository.TopicRepository;
import com.aitutor.app.util.ScoreClassifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class QuizService {

    private final ChatClient chatClient;
    private final StudentRepository studentRepository;
    private final TopicRepository topicRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final InteractionService interactionService;
    private final ObjectMapper objectMapper;

    public QuizService(ChatClient.Builder chatClientBuilder,
                        StudentRepository studentRepository,
                        TopicRepository topicRepository,
                        QuizSessionRepository quizSessionRepository,
                        InteractionService interactionService,
                        ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.studentRepository = studentRepository;
        this.topicRepository = topicRepository;
        this.quizSessionRepository = quizSessionRepository;
        this.interactionService = interactionService;
        this.objectMapper = objectMapper;
    }

    public GenerateQuizResponse generateQuiz(Long studentId, Long topicId, int numQuestions) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + topicId));

        List<QuestionAnswerDto> questions = generateQuestionsFromGemini(student, topic, numQuestions);

        QuizSession session = QuizSession.builder()
                .studentId(studentId)
                .topicId(topicId)
                .questionsJson(toJson(questions))
                .build();
        session = quizSessionRepository.save(session);

        List<QuestionDto> studentFacing = questions.stream()
                .map(q -> new QuestionDto(q.getQuestion(), q.getOptions()))
                .toList();

        return new GenerateQuizResponse(session.getId(), topic.getName(), studentFacing);
    }

    private List<QuestionAnswerDto> generateQuestionsFromGemini(Student student, Topic topic, int numQuestions) {
        String gradeLevel = student.getGradeLevel() == null ? "middle school" : student.getGradeLevel();

        String prompt = """
                Generate %d multiple-choice questions for a %s student on the topic
                "%s" (subject: %s, difficulty %d out of 5).

                Return ONLY valid JSON - no markdown, no code fences, no commentary.
                Format exactly like this:
                [
                  {"question": "...", "options": ["...", "...", "...", "..."], "correctAnswer": "..."}
                ]
                Each question must have exactly 4 options, and correctAnswer must be
                an exact copy of one of the options.
                """.formatted(numQuestions, gradeLevel, topic.getName(), topic.getSubject(), topic.getDifficulty());

        String raw = chatClient.prompt().user(prompt).call().content();
        String cleaned = raw.trim()
                .replaceAll("(?s)```json", "")
                .replaceAll("(?s)```", "")
                .trim();

        try {
            return objectMapper.readValue(cleaned, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, QuestionAnswerDto.class));
        } catch (Exception e) {
            log.error("Could not parse Gemini's response as JSON: {}", raw, e);
            throw new IllegalStateException("Gemini returned something that wasn't valid quiz JSON - try again.", e);
        }
    }

    public QuizResultDto submitQuiz(Long quizId, SubmitAnswersRequest request) {
        QuizSession session = quizSessionRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));

        List<QuestionAnswerDto> questions = fromJson(session.getQuestionsJson());
        List<String> submitted = request.getAnswers();

        int correct = 0;
        int total = questions.size();
        for (int i = 0; i < total && i < submitted.size(); i++) {
            if (questions.get(i).getCorrectAnswer().trim().equalsIgnoreCase(submitted.get(i).trim())) {
                correct++;
            }
        }

        double score = total == 0 ? 0 : (correct * 100.0) / total;

        session.setSubmitted(true);
        quizSessionRepository.save(session);

        InteractionRequestDto interactionRequest = new InteractionRequestDto(session.getTopicId(), score, 1);
        interactionService.recordInteraction(session.getStudentId(), interactionRequest);

        return new QuizResultDto(correct, total, Math.round(score * 10) / 10.0,
                ScoreClassifier.classify(score).name());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize quiz questions", e);
        }
    }

    private List<QuestionAnswerDto> fromJson(String json) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, QuestionAnswerDto.class));
        } catch (Exception e) {
            throw new IllegalStateException("Could not read stored quiz questions", e);
        }
    }
}
