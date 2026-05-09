package br.com.petflow.petflow_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskLevelResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Integer minScore;
    private Integer maxScore;
}