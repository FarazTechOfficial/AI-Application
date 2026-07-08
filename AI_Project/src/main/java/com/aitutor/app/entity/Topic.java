package com.aitutor.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "topics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic {

    @Id
    private Long id; // same id as topic_id in the Python service's topics.csv

    private String subject;
    private String name;
    private Integer difficulty;
    private Long prerequisiteId; // null = no prerequisite
}
