package com.aitutor.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    @JsonProperty("topic_id") private Long topicId;
    private String name;
    private String subject;
    private Double score;
    private String reason;
}
