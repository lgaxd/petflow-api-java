package br.com.petflow.petflow_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Resposta com dados do pet")
public class PetResponseDTO {

    @Schema(description = "ID do pet", example = "1")
    private Long id;

    @Schema(description = "Nome do pet", example = "Rex")
    private String name;

    @Schema(description = "Raça do pet", example = "Labrador Retriever")
    private String breed;

    @Schema(description = "Data de nascimento", example = "2020-05-20")
    private LocalDate birthDate;

    @Schema(description = "Peso em kg", example = "25.5")
    private BigDecimal weight;

    @Schema(description = "Espécie", example = "Cão")
    private String species;

    @Schema(description = "Data de cadastro", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "ID do tutor responsável", example = "10")
    private Long tutorId;

    @Schema(description = "Nome do tutor", example = "João Silva")
    private String tutorName;
}