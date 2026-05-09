package br.com.petflow.petflow_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardActionResponseDTO {

    private Long id;
    private String name;
    private Integer pointsValue;
    private String description;
}