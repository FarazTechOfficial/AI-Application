package com.aitutor.app.config;

import com.aitutor.app.entity.Topic;
import com.aitutor.app.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TopicRepository topicRepository;

    @Override
    public void run(String... args) {
        if (topicRepository.count() > 0) return;

        topicRepository.saveAll(List.of(
                Topic.builder().id(1L).subject("Math").name("Whole Numbers").difficulty(1).build(),
                Topic.builder().id(2L).subject("Math").name("Fractions").difficulty(2).prerequisiteId(1L).build(),
                Topic.builder().id(3L).subject("Math").name("Decimals").difficulty(2).prerequisiteId(2L).build(),
                Topic.builder().id(4L).subject("Math").name("Ratios & Proportions").difficulty(3).prerequisiteId(3L).build(),
                Topic.builder().id(5L).subject("Math").name("Linear Equations").difficulty(3).prerequisiteId(4L).build(),
                Topic.builder().id(6L).subject("Math").name("Quadratic Equations").difficulty(4).prerequisiteId(5L).build(),
                Topic.builder().id(7L).subject("Math").name("Basic Trigonometry").difficulty(4).prerequisiteId(5L).build(),
                Topic.builder().id(8L).subject("Math").name("Calculus Intro").difficulty(5).prerequisiteId(6L).build(),

                Topic.builder().id(9L).subject("Science").name("Matter & Materials").difficulty(1).build(),
                Topic.builder().id(10L).subject("Science").name("Atoms & Molecules").difficulty(2).prerequisiteId(9L).build(),
                Topic.builder().id(11L).subject("Science").name("Chemical Reactions").difficulty(3).prerequisiteId(10L).build(),
                Topic.builder().id(12L).subject("Science").name("Forces & Motion").difficulty(2).build(),
                Topic.builder().id(13L).subject("Science").name("Energy").difficulty(3).prerequisiteId(12L).build(),
                Topic.builder().id(14L).subject("Science").name("Electricity Basics").difficulty(4).prerequisiteId(13L).build(),

                Topic.builder().id(15L).subject("English").name("Grammar Basics").difficulty(1).build(),
                Topic.builder().id(16L).subject("English").name("Sentence Structure").difficulty(2).prerequisiteId(15L).build(),
                Topic.builder().id(17L).subject("English").name("Essay Writing").difficulty(3).prerequisiteId(16L).build(),
                Topic.builder().id(18L).subject("English").name("Literary Analysis").difficulty(4).prerequisiteId(17L).build()
        ));
    }
}
