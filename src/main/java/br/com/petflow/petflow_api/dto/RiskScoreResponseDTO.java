package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScoreResponseDTO {

    private Long id;
    private Integer score;
    private LocalDateTime calculatedAt;
    private Long petId;
    private String petName;
    private Long riskLevelId;
    private String riskLevelName;
}