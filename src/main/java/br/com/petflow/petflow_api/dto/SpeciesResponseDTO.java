package br.com.petflow.petflow_api.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeciesResponseDTO {

    private Long id;
    private String name;
    private String description;
}