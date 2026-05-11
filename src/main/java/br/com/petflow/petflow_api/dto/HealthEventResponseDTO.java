package br.com.petflow.petflow_api.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthEventResponseDTO {

    private Long id;
    private String description;
    private LocalDate eventDate;
    private String status;
    private LocalDateTime createdAt;
    private Long petId;
    private String petName;
    private String eventType;
    private Long clinicId;
    private String clinicName;
}