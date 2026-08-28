package br.com.petflow.petflow_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetRiskDTO {
    private Long petId;
    private String petName;
    private Integer score;
    private String riskLevel;
    private String riskDescription;
}