package com.aitutor.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlInteractionPayload {
    @JsonProperty("topic_id") private Long topicId;
    private Double score;
    private Integer attempts;
}
