package br.com.petflow.petflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados do plano de saúde pet")
public class PlanResponseDTO {

    @Schema(description = "ID do plano", example = "1")
    private Long id;

    @Schema(description = "Nome do plano", example = "Plano Premium")
    private String name;

    @Schema(description = "Descrição do plano", example = "Cobertura completa com consultas e exames")
    private String description;

    @Schema(description = "Preço mensal", example = "89.90")
    private BigDecimal price;

    @Schema(description = "Duração em dias", example = "365")
    private Integer durationDays;

    @Schema(description = "Pontos por evento realizado", example = "50")
    private Integer pointsPerEvent;

    @Schema(description = "ID da clínica associada", example = "5")
    private Long clinicId;

    @Schema(description = "Nome da clínica", example = "PetCare Veterinária")
    private String clinicName;
}