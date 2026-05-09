package br.com.petflow.petflow_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventTypeResponseDTO {

    private Long id;
    private String name;
    private Integer pointsReward;
    private String category;
}