package com.aitutor.app.service;

import com.aitutor.app.dto.TopicDto;
import com.aitutor.app.entity.Topic;
import com.aitutor.app.mapper.Mapper;
import com.aitutor.app.repository.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final Mapper mapper;

    public List<TopicDto> getAllTopics() {
        return topicRepository.findAll().stream().map(mapper::toDto).toList();
    }

    public TopicDto getTopic(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Topic not found: " + id));
        return mapper.toDto(topic);
    }
}
