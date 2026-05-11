package br.com.petflow.petflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados do evento de saúde do pet")
public class HealthEventResponseDTO {

    @Schema(description = "ID do evento", example = "1")
    private Long id;

    @Schema(description = "Descrição do evento", example = "Vacinação contra raiva")
    private String description;

    @Schema(description = "Data do evento", example = "2024-03-15")
    private LocalDate eventDate;

    @Schema(description = "Status do evento", example = "REALIZADO", allowableValues = {"AGENDADO", "REALIZADO", "CANCELADO"})
    private String status;

    @Schema(description = "Data de registro", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID do pet", example = "10")
    private Long petId;

    @Schema(description = "Nome do pet", example = "Rex")
    private String petName;

    @Schema(description = "Tipo do evento", example = "VACCINE", allowableValues = {"VACCINE", "EXAM", "CONSULTATION", "SURGERY"})
    private String eventType;

    @Schema(description = "ID da clínica (opcional)", example = "5")
    private Long clinicId;

    @Schema(description = "Nome da clínica", example = "PetCare Veterinária")
    private String clinicName;
}