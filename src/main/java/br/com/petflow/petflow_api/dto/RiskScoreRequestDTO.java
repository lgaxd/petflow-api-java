package br.com.petflow.petflow_api.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskScoreRequestDTO {

    @NotNull(message = "O score é obrigatório")
    @PositiveOrZero(message = "O score não pode ser negativo")
    private Integer score;

    @NotNull(message = "O ID do pet é obrigatório")
    private Long petId;

    @NotNull(message = "O ID do nível de risco é obrigatório")
    private Long riskLevelId;
}