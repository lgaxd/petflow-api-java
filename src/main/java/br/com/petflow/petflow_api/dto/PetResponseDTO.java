package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetResponseDTO {

    private Long id;
    private String name;
    private String breed;
    private LocalDate birthDate;
    private BigDecimal weight;
    private String species;
    private LocalDateTime createdAt;
    private Long tutorId;
    private String tutorName;
}