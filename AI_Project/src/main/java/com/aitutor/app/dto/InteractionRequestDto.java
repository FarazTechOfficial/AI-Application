package com.aitutor.app.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionRequestDto {

    @NotNull
    private Long topicId;

    @NotNull
    @DecimalMin("0")
    @DecimalMax("100")
    private Double score;

    private Integer attempts;
}
